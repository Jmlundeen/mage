package mage.remote.transport;

import mage.MageException;
import mage.cards.decks.DeckCardLists;
import mage.constants.ManaType;
import mage.constants.PlayerAction;
import mage.game.match.MatchOptions;
import mage.game.tournament.TournamentOptions;
import mage.interfaces.ServerState;
import mage.interfaces.callback.ClientCallback;
import mage.players.PlayerType;
import mage.players.net.UserData;
import mage.remote.Connection;
import mage.remote.WsSessionImpl;
import mage.utils.MageVersion;
import mage.view.DraftPickView;
import mage.ws.ProtocolVersion;
import mage.ws.v1.WsProto;
import mage.ws.v1.view.ViewProto;
import org.apache.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    @Override
    public ViewProto.TableView createTable(String sessionId, UUID roomId, MatchOptions matchOptions) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setCreateTableRequest(WsProto.CreateTableRequest.newBuilder()
                        .setRoomId(roomId == null ? "" : roomId.toString())
                        .setMatchOptions(matchOptions.toProto())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
            if (!res.hasTableViewResponse()) {
                throw new IllegalStateException("Unexpected response type");
            }
            return res.getTableViewResponse().getTableView();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ViewProto.TableView createTournamentTable(String sessionId, UUID roomId, TournamentOptions tournamentOptions) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setCreateTournamentRequest(WsProto.CreateTournamentTableRequest.newBuilder()
                        .setRoomId(roomId == null ? "" : roomId.toString())
                        .setTournamentOptions(tournamentOptions.toProto())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
            if (!res.hasTableViewResponse()) {
                throw new IllegalStateException("Unexpected response type");
            }
            return res.getTableViewResponse().getTableView();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean joinTable(String sessionId, UUID roomId, UUID tableId, String playerName, PlayerType playerType, int skill, DeckCardLists deckList, String password) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setJoinTableRequest(WsProto.JoinTableRequest.newBuilder()
                        .setRoomId(roomId == null ? "" : roomId.toString())
                        .setTableId(tableId == null ? "" : tableId.toString())
                        .setPlayerName(playerName == null ? "" : playerName)
                        .setPlayerType(playerType.toString())
                        .setAiSkill(skill)
                        .setDeckCardLists(deckList.toProto())
                        .setPassword(password == null ? "" : password)
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
            if (!res.hasAck()) {
                throw new IllegalStateException("Unexpected response type");
            }
            return true;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean removeTable(String sessionId, UUID roomId, UUID tableId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setRemoveTableRequest(WsProto.RemoveTableRequest.newBuilder()
                        .setRoomId(roomId == null ? "" : roomId.toString())
                        .setTableId(tableId == null ? "" : tableId.toString())
                        .build())
                .build();
        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
            if (!res.hasAck()) {
                throw new IllegalStateException("Unexpected response type");
            }
            return true;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<UUID> getTableChatId(String sessionId, UUID tableId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setTableChatIdRequest(WsProto.TableChatIdRequest.newBuilder()
                        .setTableId(tableId == null ? "" : tableId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
            if (!res.hasUuidResponse()) {
                throw new IllegalStateException("Unexpected response type");
            }

            String chatIdStr = res.getUuidResponse().getUuid();
            return chatIdStr.isEmpty() ? Optional.empty() : Optional.of(UUID.fromString(chatIdStr));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<UUID> getGameChatId(String sessionId, UUID gameId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setGameChatIdRequest(WsProto.GameChatIdRequest.newBuilder()
                        .setGameId(gameId == null ? "" : gameId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
            if (!res.hasUuidResponse()) {
                throw new IllegalStateException("Unexpected response type");
            }

            String chatIdStr = res.getUuidResponse().getUuid();
            return chatIdStr.isEmpty() ? Optional.empty() : Optional.of(UUID.fromString(chatIdStr));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<UUID> getTournamentChatId(String sessionId, UUID tournamentId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setTournamentChatIdRequest(WsProto.TournamentChatIdRequest.newBuilder()
                        .setTournamentId(tournamentId == null ? "" : tournamentId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
            if (!res.hasUuidResponse()) {
                throw new IllegalStateException("Unexpected response type");
            }

            String chatIdStr = res.getUuidResponse().getUuid();
            return chatIdStr.isEmpty() ? Optional.empty() : Optional.of(UUID.fromString(chatIdStr));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<ViewProto.TableView> getTable(String sessionId, UUID roomId, UUID tableId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setGetTableRequest(WsProto.GetTableRequest.newBuilder()
                        .setRoomId(roomId == null ? "" : roomId.toString())
                        .setTableId(tableId == null ? "" : tableId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
            if (res.hasTableViewResponse()) {
                return Optional.of(res.getTableViewResponse().getTableView());
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ViewProto.TournamentView getTournament(String sessionId, UUID tournamentId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setGetTournamentRequest(WsProto.GetTournamentRequest.newBuilder()
                        .setTournamentId(tournamentId == null ? "" : tournamentId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
            if (res.hasTournamentViewResponse()) {
                return res.getTournamentViewResponse().getTournamentView();
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isTableOwner(String sessionId, UUID roomId, UUID tableId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setIsTableOwnerRequest(WsProto.IsTableOwnerRequest.newBuilder()
                        .setRoomId(roomId == null ? "" : roomId.toString())
                        .setTableId(tableId == null ? "" : tableId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
            return res.hasBoolean() && res.getBoolean();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean watchTable(String sessionId, UUID roomId, UUID tableId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setWatchTableRequest(WsProto.WatchTableRequest.newBuilder()
                        .setRoomId(roomId == null ? "" : roomId.toString())
                        .setTableId(tableId == null ? "" : tableId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
            return res.hasBoolean() && res.getBoolean();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendChatMessage(String sessionId, UUID chatId, String userName, String message) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setSendChatMessageRequest(WsProto.SendChatMessageRequest.newBuilder()
                        .setChatId(chatId == null ? "" : chatId.toString())
                        .setMessage(message != null ? message : "")
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendBroadcastMessage(String sessionId, String message) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setSendBroadcastMessageRequest(WsProto.SendBroadcastMessageRequest.newBuilder()
                        .setMessage(message != null ? message : "")
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPlayerUUID(String sessionId, UUID gameId, UUID data) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setSendPlayerDataRequest(WsProto.SendPlayerDataRequest.newBuilder()
                        .setGameId(gameId == null ? "" : gameId.toString())
                        .setUuidData(data == null ? "" : data.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPlayerBoolean(String sessionId, UUID gameId, Boolean data) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setSendPlayerDataRequest(WsProto.SendPlayerDataRequest.newBuilder()
                        .setGameId(gameId == null ? "" : gameId.toString())
                        .setBooleanData(data != null ? data : false)
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPlayerInteger(String sessionId, UUID gameId, Integer data) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setSendPlayerDataRequest(WsProto.SendPlayerDataRequest.newBuilder()
                        .setGameId(gameId == null ? "" : gameId.toString())
                        .setIntegerData(data != null ? data : 0)
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPlayerString(String sessionId, UUID gameId, String data) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setSendPlayerDataRequest(WsProto.SendPlayerDataRequest.newBuilder()
                        .setGameId(gameId == null ? "" : gameId.toString())
                        .setStringData(data != null ? data : "")
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPlayerManaType(String sessionId, UUID gameId, UUID playerId, ManaType data) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setSendPlayerDataRequest(WsProto.SendPlayerDataRequest.newBuilder()
                        .setGameId(gameId == null ? "" : gameId.toString())
                        .setPlayerId(playerId == null ? "" : playerId.toString())
                        .setManaTypeData(data != null ? data.toString() : "")
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void quitMatch(String sessionId, UUID gameId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setQuitMatchRequest(WsProto.QuitMatchRequest.newBuilder()
                        .setGameId(gameId == null ? "" : gameId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void quitTournament(String sessionId, UUID tournamentId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setQuitTournamentRequest(WsProto.QuitTournamentRequest.newBuilder()
                        .setTournamentId(tournamentId == null ? "" : tournamentId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void quitDraft(String sessionId, UUID draftId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setQuitDraftRequest(WsProto.QuitDraftRequest.newBuilder()
                        .setDraftId(draftId == null ? "" : draftId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean submitDeck(String sessionId, UUID tableId, DeckCardLists deckCardLists) {
        // Workaround to fix Can't join table problem
        if (deckCardLists != null) {
            deckCardLists.setCardLayout(null);
            deckCardLists.setSideboardLayout(null);
        }

        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setSubmitDeckRequest(WsProto.SubmitDeckRequest.newBuilder()
                        .setTableId(tableId == null ? "" : tableId.toString())
                        .setDeckCardLists(deckCardLists != null ? deckCardLists.toProto() : mage.ws.v1.model.ModelProto.DeckCardLists.getDefaultInstance())
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

    public boolean updateDeck(String sessionId, UUID tableId, DeckCardLists deckCardLists) {
        if (deckCardLists != null) {
            deckCardLists.setCardLayout(null);
            deckCardLists.setSideboardLayout(null);
        }

        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setUpdateDeckRequest(WsProto.UpdateDeckRequest.newBuilder()
                        .setTableId(tableId == null ? "" : tableId.toString())
                        .setDeckCardLists(deckCardLists != null ? deckCardLists.toProto() : mage.ws.v1.model.ModelProto.DeckCardLists.getDefaultInstance())
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

    public boolean setBoosterLoaded(String sessionId, UUID draftId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setSetBoosterLoadedRequest(WsProto.SetBoosterLoadedRequest.newBuilder()
                        .setDraftId(draftId == null ? "" : draftId.toString())
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

    public DraftPickView sendCardPick(String sessionId, UUID draftId, UUID cardId, Set<UUID> hiddenCards) {
        WsProto.ClientMessage.Builder reqBuilder = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId);

        WsProto.SendCardPickRequest.Builder pickBuilder = WsProto.SendCardPickRequest.newBuilder()
                .setDraftId(draftId == null ? "" : draftId.toString())
                .setCardId(cardId == null ? "" : cardId.toString());

        if (hiddenCards != null) {
            for (UUID hiddenCard : hiddenCards) {
                pickBuilder.addHiddenCards(hiddenCard.toString());
            }
        }

        reqBuilder.setSendCardPickRequest(pickBuilder.build());

        try {
            WsProto.ServerMessage res = roundTrip(reqBuilder.build());
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
            if (res.hasDraftPickViewResponse()) {
                return DraftPickView.fromProto(res.getDraftPickViewResponse().getDraftPickView());
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendCardMark(String sessionId, UUID draftId, UUID cardId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setSendCardMarkRequest(WsProto.SendCardMarkRequest.newBuilder()
                        .setDraftId(draftId == null ? "" : draftId.toString())
                        .setCardId(cardId == null ? "" : cardId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPlayerAction(String sessionId, PlayerAction playerAction, UUID gameId, Object data) {
        WsProto.SendPlayerActionRequest.Builder requestBuilder = WsProto.SendPlayerActionRequest.newBuilder()
                .setPlayerAction(playerAction.toString())
                .setGameId(gameId == null ? "" : gameId.toString());

        // Handle different data types with oneof
        if (data != null) {
            switch (data) {
                case Integer i -> requestBuilder.setIntegerData(i);
                case UUID uuid -> requestBuilder.setUuidData(uuid.toString());
                case String s -> requestBuilder.setStringData(s);
                default -> {
                    // Fallback: convert to string
                    logger.warn("sendPlayerAction: Unhandled data type " + data.getClass() + ", converting to string");
                    requestBuilder.setStringData(data.toString());
                }
            }
        }
        // If data is null, leave oneof unset

        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setSendPlayerActionRequest(requestBuilder.build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void joinGame(String sessionId, UUID gameId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setJoinGameRequest(WsProto.JoinGameRequest.newBuilder()
                        .setGameId(gameId == null ? "" : gameId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void adminRemoveTable(String sessionId, UUID tableId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setAdminRemoveTableRequest(WsProto.AdminRemoveTableRequest.newBuilder()
                        .setTableId(tableId == null ? "" : tableId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void joinDraft(String sessionId, UUID draftId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setJoinDraftRequest(WsProto.JoinDraftRequest.newBuilder()
                        .setDraftId(draftId == null ? "" : draftId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void joinTournament(String sessionId, UUID tournamentId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setJoinTournamentRequest(WsProto.JoinTournamentRequest.newBuilder()
                        .setTournamentId(tournamentId == null ? "" : tournamentId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean leaveTable(String sessionId, UUID roomId, UUID tableId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setLeaveTableRequest(WsProto.LeaveTableRequest.newBuilder()
                        .setRoomId(roomId == null ? "" : roomId.toString())
                        .setTableId(tableId == null ? "" : tableId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
            return res.hasBoolean() && res.getBoolean();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void swapSeats(String sessionId, UUID roomId, UUID tableId, int seatNum1, int seatNum2) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setSwapSeatsRequest(WsProto.SwapSeatsRequest.newBuilder()
                        .setRoomId(roomId == null ? "" : roomId.toString())
                        .setTableId(tableId == null ? "" : tableId.toString())
                        .setSeatNum1(seatNum1)
                        .setSeatNum2(seatNum2)
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean startMatch(String sessionId, UUID roomId, UUID tableId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setStartMatchRequest(WsProto.StartMatchRequest.newBuilder()
                        .setRoomId(roomId == null ? "" : roomId.toString())
                        .setTableId(tableId == null ? "" : tableId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
            return res.hasBoolean() && res.getBoolean();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean startTournament(String sessionId, UUID roomId, UUID tableId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setStartTournamentRequest(WsProto.StartTournamentRequest.newBuilder()
                        .setRoomId(roomId == null ? "" : roomId.toString())
                        .setTableId(tableId == null ? "" : tableId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
            return res.hasBoolean() && res.getBoolean();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean watchGame(String sessionId, UUID gameId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setWatchGameRequest(WsProto.WatchGameRequest.newBuilder()
                        .setGameId(gameId == null ? "" : gameId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
            return res.hasBoolean() && res.getBoolean();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stopWatching(String sessionId, UUID gameId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setStopWatchingRequest(WsProto.StopWatchingRequest.newBuilder()
                        .setGameId(gameId == null ? "" : gameId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void replayGame(String sessionId, UUID gameId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setReplayGameRequest(WsProto.ReplayGameRequest.newBuilder()
                        .setGameId(gameId == null ? "" : gameId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void startReplay(String sessionId, UUID gameId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setStartReplayRequest(WsProto.StartReplayRequest.newBuilder()
                        .setGameId(gameId == null ? "" : gameId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stopReplay(String sessionId, UUID gameId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setStopReplayRequest(WsProto.StopReplayRequest.newBuilder()
                        .setGameId(gameId == null ? "" : gameId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void nextPlay(String sessionId, UUID gameId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setNextPlayRequest(WsProto.NextPlayRequest.newBuilder()
                        .setGameId(gameId == null ? "" : gameId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void previousPlay(String sessionId, UUID gameId) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setPreviousPlayRequest(WsProto.PreviousPlayRequest.newBuilder()
                        .setGameId(gameId == null ? "" : gameId.toString())
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void skipForward(String sessionId, UUID gameId, int moves) {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setSkipForwardRequest(WsProto.SkipForwardRequest.newBuilder()
                        .setGameId(gameId == null ? "" : gameId.toString())
                        .setMoves(moves)
                        .build())
                .build();

        try {
            WsProto.ServerMessage res = roundTrip(req);
            if (res.hasError()) {
                throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
