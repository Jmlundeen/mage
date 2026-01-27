package mage.remote.transport;

import mage.MageException;
import mage.interfaces.ServerState;
import mage.interfaces.callback.ClientCallback;
import mage.players.net.UserData;
import mage.remote.Connection;
import mage.remote.WsSessionImpl;
import mage.utils.MageVersion;
import mage.ws.ProtocolVersion;
import mage.ws.v1.WsProto;
import mage.ws.v1.view.ViewProto;
import org.apache.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Dev/test WS+Protobuf transport client.
 *
 * IMPORTANT:
 * - This is a minimal implementation for the incremental migration (hello/auth/ping).
 * - Uses a real WebSocket client and binary protobuf frames.
 */
public class WsClientTransport implements ClientTransport {

    private static final Logger logger = Logger.getLogger(WsClientTransport.class);

    private static final int CONNECT_TIMEOUT_SECS = 10;
    private static final int REQUEST_TIMEOUT_SECS = 15;

    private WebSocketClient webSocketClient;
    private final WsSessionImpl wsSession;
    private final ConcurrentHashMap<String, CompletableFuture<WsProto.ServerMessage>> pending = new ConcurrentHashMap<>();
    private final LobbyEventBus lobbyEventBus;

    public WsClientTransport(WsSessionImpl wsSession) {
        this.wsSession = wsSession;
        this.lobbyEventBus = new LobbyEventBus(wsSession);
    }

    @Override
    public void connect(Connection connection, String sessionId) throws Exception {
        disconnect(sessionId);

        int wsPort = connection.getPort() + 500;

        // Build WebSocket URL with sessionId query parameter if available
        String wsUrl = "ws://" + connection.getHost() + ':' + wsPort + "/ws";
        if (sessionId != null && !sessionId.isEmpty()) {
            wsUrl += "?sessionId=" + sessionId;
        }
        URI uri = new URI(wsUrl);

        webSocketClient = new WebSocketClient(uri) {

            @Override
            public void onOpen(ServerHandshake handshakedata) {
            }

            @Override
            public void onMessage(String message) {
                // server should send binary protobuf only
                logger.warn("WS text message received (ignored): " + message);
            }

            @Override
            public void onMessage(ByteBuffer bytes) {
                try {
                    byte[] data = new byte[bytes.remaining()];
                    bytes.get(data);
                    WsProto.ServerMessage msg = WsProto.ServerMessage.parseFrom(data);

                    String requestId = msg.getRequestId();
                    if (requestId.isEmpty()) {
                        // Handle push messages (no requestId means server-initiated)
                        if (msg.hasLobbyGetInfo()) {
                            WsProto.LobbyInfoResponse payload = msg.getLobbyGetInfo();
                            LobbyEvent event = new LobbyEvent(
                                    payload.getTablesList(),
                                    payload.getRoomUsers(),
                                    payload.getFinishedMatchesList()
                            );
                            lobbyEventBus.publish(event);
                        } else if(msg.hasClientCallback()) {
                            wsSession.handleCallback(ClientCallback.fromProto(msg.getClientCallback()));
                        } else {
                            logger.debug("WS push message ignored: unknown type - " + msg.getPayloadCase());
                        }
                        return;
                    }

                    CompletableFuture<WsProto.ServerMessage> future = pending.remove(requestId);
                    if (future != null) {
                        future.complete(msg);
                    } else {
                        logger.debug("WS message for unknown requestId=" + requestId + ", message=" + msg.getPayloadCase());
                    }
                } catch (Exception e) {
                    logger.error("WS binary message parse error", e);
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Exception ex = new IllegalStateException("WS closed: code=" + code + ", reason=" + reason);
                pending.forEach((k, v) -> v.completeExceptionally(ex));
                pending.clear();
            }

            @Override
            public void onError(Exception ex) {
                logger.error("WS client error", ex);
            }
        };

        ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "WsClientTransport-connect");
            t.setDaemon(true);
            return t;
        });

        try {
            Future<Boolean> future = executor.submit(() -> webSocketClient.connectBlocking());
            Boolean connected;
            try {
                connected = future.get(CONNECT_TIMEOUT_SECS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                throw new IllegalStateException("WS connect timeout after " + CONNECT_TIMEOUT_SECS + "s to " + uri, e);
            }

            if (connected == null || !connected) {
                throw new IllegalStateException("Unable to connect to WS endpoint: " + uri);
            }
        } catch (ExecutionException e) {
            throw new IllegalStateException("Unable to connect to WS endpoint: " + uri, e.getCause());
        } finally {
            executor.shutdownNow();
        }
    }

    @Override
    public void disconnect(String sessionId) {
        try {
            if (webSocketClient != null) {
                try {
                    WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                            .setProtocolVersion(ProtocolVersion.getVersion())
                            .setRequestId(newRequestId())
                            .setSessionId(sessionId == null ? "" : sessionId)
                            .setDisconnect(WsProto.Disconnect.getDefaultInstance())
                            .build();
                    sendMessage(req);
                    webSocketClient.closeBlocking();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } finally {
            webSocketClient = null;
            pending.clear();
        }
    }

    @Override
    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.isOpen();
    }

    @Override
    public AuthResult auth(String sessionId, String userName, String password) throws Exception {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setAuth(WsProto.AuthRequest.newBuilder()
                        .setUserName(userName == null ? "" : userName)
                        .setPassword(password == null ? "" : password)
                        .build())
                .build();

        WsProto.ServerMessage res = roundTrip(req);
        if (res.hasError()) {
            if (res.getError().getCode() == WsProto.ErrorCode.AUTH_FAILED) {
                return new AuthResult(false, res.getError().getMessage());
            }
            throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
        }
        if (!res.hasAuth()) {
            throw new IllegalStateException("Unexpected response type");
        }
        return new AuthResult(res.getAuth().getOk(), res.getAuth().getMessage());
    }

    @Override
    public boolean ping(String sessionId, String lastPingInfo) throws Exception {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setPing(WsProto.PingRequest.newBuilder()
                        .setLastPingInfo(lastPingInfo)
                        .build())
                .build();

        WsProto.ServerMessage res = roundTrip(req);
        if (res.hasError()) {
            throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
        }
        if (!res.hasAck()) {
            throw new IllegalStateException("Unexpected response type");
        }
        return true;
    }

    @Override
    public List<ViewProto.TableView> lobbyGetTables(String sessionId, UUID roomId) throws Exception {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setTableRequest(WsProto.TablesRequest.newBuilder()
                        .setRoomId(roomId == null ? "" : roomId.toString())
                        .build())
                .build();

        WsProto.ServerMessage res = roundTrip(req);
        if (res.hasError()) {
            throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
        }
        if (!res.hasLobbyGetTables()) {
            throw new IllegalStateException("Unexpected response type");
        }
        return res.getLobbyGetTables().getTablesList();
    }

    @Override
    public List<ViewProto.MatchView> getFinishedMatches(String sessionId, UUID roomId) throws Exception {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setFinishedMatchesRequest(WsProto.FinishedMatchesRequest.newBuilder()
                        .setRoomId(roomId == null ? "" : roomId.toString())
                        .build())
                .build();

        WsProto.ServerMessage res = roundTrip(req);
        if (res.hasError()) {
            throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
        }
        if (!res.hasFinishedMatchesResponse()) {
            throw new IllegalStateException("Unexpected response type");
        }
        return res.getFinishedMatchesResponse().getFinishedMatchesList();
    }

    @Override
    public ViewProto.RoomUsersView getRoomUsers(String sessionId, UUID roomId) throws Exception {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setRoomUsersRequest(WsProto.RoomUsersRequest.newBuilder()
                        .setRoomId(roomId == null ? "" : roomId.toString())
                        .build())
                .build();

        WsProto.ServerMessage res = roundTrip(req);
        if (res.hasError()) {
            throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
        }
        if (!res.hasRoomUsersResponse()) {
            throw new IllegalStateException("Unexpected response type");
        }
        return res.getRoomUsersResponse().getRoomUsers();
    }

    @Override
    public GetLobbyInfoResult lobbyGetInfo(String sessionId, UUID roomId, boolean includeFinishedMatches, boolean includeRoomUsers) throws Exception {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setLobbyInfoRequest(WsProto.LobbyInfoRequest.newBuilder()
                        .setRoomId(roomId == null ? "" : roomId.toString())
                        .setIncludeFinishedMatches(includeFinishedMatches)
                        .setIncludeRoomUsers(includeRoomUsers)
                        .build())
                .build();

        WsProto.ServerMessage res = roundTrip(req);
        if (res.hasError()) {
            throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
        }
        if (!res.hasLobbyGetInfo()) {
            throw new IllegalStateException("Unexpected response type");
        }

        WsProto.LobbyInfoResponse payload = res.getLobbyGetInfo();
        return new GetLobbyInfoResult(
                payload.getTablesList(),
                payload.getRoomUsers(),
                payload.getFinishedMatchesList()
        );
    }

    @Override
    public UUID getMainRoomId(String sessionId) throws Exception {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setMainRoomIdRequest(WsProto.MainRoomIdRequest.getDefaultInstance())
                .build();

        WsProto.ServerMessage res = roundTrip(req);
        if (res.hasError()) {
            throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
        }
        if (!res.hasUuidResponse()) {
            throw new IllegalStateException("Unexpected response type");
        }

        String roomIdStr = res.getUuidResponse().getUuid();
        return roomIdStr.isEmpty() ? null : UUID.fromString(roomIdStr);
    }

    @Override
    public boolean joinChat(String sessionId, UUID chatId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setJoinChatRequest(WsProto.JoinChatRequest.newBuilder()
                        .setChatId(chatId == null ? "" : chatId.toString())
                        .build())
                .build();
        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
            return res.hasAck();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean leaveChat(String sessionId, UUID chatId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setLeaveChatRequest(WsProto.LeaveChatRequest.newBuilder()
                        .setChatId(chatId == null ? "" : chatId.toString())
                        .build())
                .build();
        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
            return res.hasAck();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ServerState getServerState(String sessionId) throws Exception {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setServerStateRequest(WsProto.ServerStateRequest.getDefaultInstance())
                .build();

        WsProto.ServerMessage res = roundTrip(req);
        if (res.hasError()) {
            throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
        }
        if (!res.hasServerStateResponse()) {
            throw new IllegalStateException("Unexpected response type");
        }

        return ServerState.fromProto(res.getServerStateResponse().getServerState());
    }

    @Override
    public void sendUserData(String sessionId, UserData userData, MageVersion clientVersion, String userIdStr) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setUserData(WsProto.UserData.newBuilder()
                        .setUserData(userData.toProto())
                        .setMageVersion(clientVersion.toProto())
                        .setUserIdStr(userIdStr))
                .build();
        sendMessage(req);
    }

    @Override
    public List<String> getServerMessages(String sessionId) throws Exception {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setPromotionMessagesRequest(WsProto.PromotionMessagesRequest.getDefaultInstance())
                .build();
        WsProto.ServerMessage res = roundTrip(req);
        if (res.hasError()) {
            throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
        }
        if (!res.hasPromotionMessagesResponse()) {
            throw new IllegalStateException("Unexpected response type");
        }
        return res.getPromotionMessagesResponse().getMessagesList();
    }

    @Override
    public UUID getRoomChatId(String sessionId, UUID roomId) throws Exception {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setRoomChatIdRequest(WsProto.RoomChatIdRequest.newBuilder()
                        .setRoomId(roomId == null ? "" : roomId.toString())
                        .build())
                .build();

        WsProto.ServerMessage res = roundTrip(req);
        if (res.hasError()) {
            throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
        }
        if (!res.hasUuidResponse()) {
            throw new IllegalStateException("Unexpected response type");
        }

        String chatIdStr = res.getUuidResponse().getUuid();
        return chatIdStr.isEmpty() ? null : UUID.fromString(chatIdStr);
    }

    private WsProto.ServerMessage roundTrip(WsProto.ClientMessage req) throws Exception {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected");
        }

        CompletableFuture<WsProto.ServerMessage> future = new CompletableFuture<>();
        pending.put(req.getRequestId(), future);

        try {
            webSocketClient.send(ByteBuffer.wrap(req.toByteArray()));
            WsProto.ServerMessage res = future.get(REQUEST_TIMEOUT_SECS, TimeUnit.SECONDS);
            if (res.getPayloadCase() == WsProto.ServerMessage.PayloadCase.ERROR) {
                if (res.getError().getCode().equals(WsProto.ErrorCode.MAGE_EXCEPTION)) {
                    throw new MageException(res.getError().getMessage());
                }
                throw new Exception(res.getError().getCode() + ": " + res.getError().getMessage());
            }
            return res;
        } finally {
            pending.remove(req.getRequestId());
        }
    }

    private void sendMessage(WsProto.ClientMessage msg) {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            webSocketClient.send(ByteBuffer.wrap(msg.toByteArray()));
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        }
    }

    private static String newRequestId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Get the lobby event bus for subscribing to lobby updates.
     *
     * @return The lobby event bus
     */
    public LobbyEventBus getLobbyEventBus() {
        return lobbyEventBus;
    }
}
