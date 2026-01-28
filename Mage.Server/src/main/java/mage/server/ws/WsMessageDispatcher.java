package mage.server.ws;

import mage.MageException;
import mage.cards.decks.DeckCardLists;
import mage.game.match.MatchOptions;
import mage.game.tournament.TournamentOptions;
import mage.interfaces.MageServer;
import mage.players.PlayerType;
import mage.players.net.UserData;
import mage.remote.SessionImpl;
import mage.server.DisconnectReason;
import mage.server.MainManagerFactory;
import mage.server.SessionManagerImpl;
import mage.server.User;
import mage.utils.MageVersion;
import mage.ws.ProtocolVersion;
import mage.ws.v1.WsProto;
import mage.ws.v1.model.ModelProto;
import mage.ws.v1.view.ViewProto;
import org.apache.log4j.Logger;
import org.jboss.remoting.callback.InvokerCallbackHandler;

import java.util.UUID;

/**
 * Minimal dispatcher for the new WS+Protobuf transport.
 *
 * Contract:
 * - Always returns exactly one ServerMessage per input client message.
 * - Never throws (errors are mapped to ServerMessage.error).
 */
public class WsMessageDispatcher {

    private static final Logger logger = Logger.getLogger(WsMessageDispatcher.class);

    private final MainManagerFactory managerFactory;
    private final SessionManagerImpl sessionManager;
    private final MageServer mageServer;
    private final boolean detailsMode;

    private final InvokerCallbackHandler noopCallbackHandler = callback -> {
        // WS transport does not use JBoss callbacks.
        // Existing Session code may try to send messages; those will be ignored for now.
    };

    public WsMessageDispatcher(MainManagerFactory managerFactory, MageServer mageServer, boolean detailsMode) {
        this.managerFactory = managerFactory;
        this.sessionManager = (SessionManagerImpl) managerFactory.sessionManager();
        this.mageServer = mageServer;
        this.detailsMode = detailsMode;
    }

    private static final class MissingSessionException extends RuntimeException {

        private MissingSessionException(String message) {
            super(message);
        }
    }

    public WsProto.ServerMessage handle(WsProto.ClientMessage msg) {
        String requestId = msg.getRequestId();
        String sessionId = msg.getSessionId();

        if (msg.getProtocolVersion().isEmpty() || !ProtocolVersion.equalsStrict(msg.getProtocolVersion())) {
            return error(requestId, sessionId, WsProto.ErrorCode.INVALID_PROTOCOL_VERSION,
                    "Invalid protocolVersion. ServerProtocol=" + ProtocolVersion.getVersion());
        }

        if (requestId.trim().isEmpty()) {
            return error("", sessionId, WsProto.ErrorCode.MISSING_REQUEST_ID, "Missing requestId");
        }

        try {
            return switch (msg.getPayloadCase()) {
                case DISCONNECT -> disconnect(requestId, sessionId, msg.getDisconnect());
                case PING -> ping(requestId, sessionId, msg.getPing());
                case AUTH -> auth(requestId, sessionId, msg.getAuth());
                case MAIN_ROOM_ID_REQUEST -> getMainRoomId(requestId, sessionId, msg.getMainRoomIdRequest());
                case ROOM_CHAT_ID_REQUEST -> getChatRoomId(requestId, sessionId, msg.getRoomChatIdRequest());
                case JOIN_CHAT_REQUEST -> joinChat(requestId, sessionId, msg.getJoinChatRequest());
                case LEAVE_CHAT_REQUEST -> leaveChat(requestId, sessionId, msg.getLeaveChatRequest());
                case SERVER_STATE_REQUEST -> getServerState(requestId, sessionId);
                case USER_DATA -> setUserData(sessionId, msg.getUserData());
                case TABLE_REQUEST -> getTables(requestId, sessionId, msg.getTableRequest());
                case FINISHED_MATCHES_REQUEST -> getFinishedMatches(requestId, sessionId, msg.getFinishedMatchesRequest());
                case ROOM_USERS_REQUEST -> getRoomUsers(requestId, sessionId, msg.getRoomUsersRequest());
                case LOBBY_INFO_REQUEST -> getLobbyInfo(requestId, sessionId, msg.getLobbyInfoRequest());
                case PROMOTION_MESSAGES_REQUEST -> getPromotionMessages(requestId, sessionId);
                case CREATE_TABLE_REQUEST -> createTable(requestId, sessionId, msg.getCreateTableRequest());
                case CREATE_TOURNAMENT_REQUEST -> createTournamentTable(requestId, sessionId, msg.getCreateTournamentRequest());
                case JOIN_TABLE_REQUEST -> joinTable(requestId, sessionId, msg.getJoinTableRequest());
                case REMOVE_TABLE_REQUEST -> removeTable(requestId, sessionId, msg.getRemoveTableRequest());
                case TABLE_CHAT_ID_REQUEST -> getTableChatId(requestId, sessionId, msg.getTableChatIdRequest());
                case GAME_CHAT_ID_REQUEST -> getGameChatId(requestId, sessionId, msg.getGameChatIdRequest());
                case TOURNAMENT_CHAT_ID_REQUEST -> getTournamentChatId(requestId, sessionId, msg.getTournamentChatIdRequest());
                case SEND_CHAT_MESSAGE_REQUEST -> sendChatMessage(requestId, sessionId, msg.getSendChatMessageRequest());
                case SEND_BROADCAST_MESSAGE_REQUEST -> sendBroadcastMessage(requestId, sessionId, msg.getSendBroadcastMessageRequest());
                case SEND_PLAYER_DATA_REQUEST -> sendPlayerData(requestId, sessionId, msg.getSendPlayerDataRequest());
                case IS_TABLE_OWNER_REQUEST -> getIsTableOwner(requestId, sessionId, msg.getIsTableOwnerRequest());
                default -> error(requestId, sessionId, WsProto.ErrorCode.UNKNOWN_MESSAGE_TYPE, "Unknown message type");
            };
        } catch (MissingSessionException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MISSING_SESSION_ID, e.getMessage());
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            logger.error("WS dispatch error", e);
            return error(requestId, sessionId, WsProto.ErrorCode.SERVER_ERROR, "Server error");
        }
    }

    private WsProto.ServerMessage disconnect(String requestId, String sessionId, WsProto.Disconnect msg) {
        requireSession(sessionId);
        DisconnectReason reason = DisconnectReason.DisconnectedByUser;
        if (SessionImpl.KEEP_MY_OLD_SESSION.equals(sessionId)) {
            // Special case: client wants to disconnect but keep tables (e.g. for reconnect before inactive timeout)
            reason = DisconnectReason.DisconnectedByUserButKeepTables;
        }
        managerFactory.sessionManager().disconnect(sessionId, reason, true);
        return null; // No response expected for disconnect
    }

    private WsProto.ServerMessage ping(String requestId, String sessionId, WsProto.PingRequest req) {
        requireSession(sessionId);
        try {
            if (mageServer.ping(sessionId, req.getLastPingInfo())) {
                return WsProto.ServerMessage.newBuilder()
                        .setProtocolVersion(ProtocolVersion.getVersion())
                        .setRequestId(requestId)
                        .setSessionId(sessionId)
                        .setAck(WsProto.Ack.getDefaultInstance())
                        .build();
            }
            else {
                return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Ping rejected by server");
            }
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Ping failed: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage auth(String requestId, String sessionId, WsProto.AuthRequest req) {
        // For now: implement a minimal login call path.
        // Ensure a server-side session exists for that sessionId.
        if (sessionManager.getSession(sessionId).isEmpty()) {
            sessionManager.createSession(sessionId, noopCallbackHandler);
        }

        boolean ok;
        try {
            ok = sessionManager.connectUser(sessionId, "", req.getUserName(), req.getPassword(), "ws", detailsMode);
        } catch (Exception e) {
            logger.warn("Auth failed", e);
            ok = false;
        }

        if (!ok) {
            return error(requestId, sessionId, WsProto.ErrorCode.AUTH_FAILED, "Auth failed");
        }

        return WsProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId)
                .setSessionId(sessionId)
                .setAuth(WsProto.AuthResponse.newBuilder()
                        .setOk(true)
                        .setMessage("OK")
                        .build())
                .build();
    }

    private WsProto.ServerMessage getMainRoomId(String requestId, String sessionId, WsProto.MainRoomIdRequest req) {
        requireSession(sessionId);
        UUID mainRoomId;
        try {
            mainRoomId = mageServer.serverGetMainRoomId();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.SERVER_ERROR, "Could not get main room ID");
        }
        return WsProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId)
                .setSessionId(sessionId)
                .setUuidResponse(WsProto.UUIDResponse.newBuilder()
                        .setUuid(mainRoomId.toString())
                        .build())
                .build();
    }

    private WsProto.ServerMessage getChatRoomId(String requestId, String sessionId, WsProto.RoomChatIdRequest roomChatIdRequest) {
        requireSession(sessionId);
        UUID roomId = UUID.fromString(roomChatIdRequest.getRoomId());
        UUID chatRoomId;
        try {
            chatRoomId = mageServer.chatFindByRoom(roomId);
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.SERVER_ERROR, "Could not get chat room ID");
        }
        return WsProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId)
                .setSessionId(sessionId)
                .setUuidResponse(WsProto.UUIDResponse.newBuilder()
                        .setUuid(chatRoomId.toString())
                        .build())
                .build();
    }

    private WsProto.ServerMessage getTables(String requestId, String sessionId, WsProto.TablesRequest req) throws MageException {
        requireSession(sessionId);
        UUID roomId = req.getRoomId().isEmpty() ? null : UUID.fromString(req.getRoomId());
        if (roomId == null) {
            return error(requestId, sessionId, WsProto.ErrorCode.SERVER_ERROR, "Missing roomId");
        }

        WsProto.TablesResponse.Builder builder = WsProto.TablesResponse.newBuilder();
        builder.addAllTables(mageServer.roomGetAllTables(roomId));

        return WsProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId)
                .setSessionId(sessionId)
                .setLobbyGetTables(builder.build())
                .build();
    }

    private WsProto.ServerMessage getFinishedMatches(String requestId, String sessionId, WsProto.FinishedMatchesRequest req) throws MageException {
        requireSession(sessionId);
        UUID roomId = req.getRoomId().isEmpty() ? null : UUID.fromString(req.getRoomId());
        if (roomId == null) {
            return error(requestId, sessionId, WsProto.ErrorCode.SERVER_ERROR, "Missing roomId");
        }

        WsProto.FinishedMatchesResponse.Builder builder = WsProto.FinishedMatchesResponse.newBuilder();
        builder.addAllFinishedMatches(mageServer.roomGetFinishedMatches(roomId));

        return WsProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId)
                .setSessionId(sessionId)
                .setFinishedMatchesResponse(builder.build())
                .build();
    }

    private WsProto.ServerMessage getRoomUsers(String requestId, String sessionId, WsProto.RoomUsersRequest req) throws MageException {
        requireSession(sessionId);
        UUID roomId = req.getRoomId().isEmpty() ? null : UUID.fromString(req.getRoomId());
        if (roomId == null) {
            return error(requestId, sessionId, WsProto.ErrorCode.SERVER_ERROR, "Missing roomId");
        }

        WsProto.RoomUsersResponse.Builder builder = WsProto.RoomUsersResponse.newBuilder();
        builder.setRoomUsers(mageServer.roomGetUsers(roomId));

        return WsProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId)
                .setSessionId(sessionId)
                .setRoomUsersResponse(builder.build())
                .build();
    }

    private WsProto.ServerMessage getLobbyInfo(String requestId, String sessionId, WsProto.LobbyInfoRequest req) throws MageException {
        requireSession(sessionId);
        UUID roomId = req.getRoomId().isEmpty() ? null : UUID.fromString(req.getRoomId());
        if (roomId == null) {
            return error(requestId, sessionId, WsProto.ErrorCode.SERVER_ERROR, "Missing roomId");
        }

        WsProto.LobbyInfoResponse.Builder builder = WsProto.LobbyInfoResponse.newBuilder();
        builder.addAllTables(mageServer.roomGetAllTables(roomId));
        builder.addAllFinishedMatches(mageServer.roomGetFinishedMatches(roomId));
        builder.setRoomUsers(mageServer.roomGetUsers(roomId));

        return WsProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId)
                .setSessionId(sessionId)
                .setLobbyGetInfo(builder.build())
                .build();
    }

    private WsProto.ServerMessage joinChat(String requestId, String sessionId, WsProto.JoinChatRequest joinChatRequest) {
        requireSession(sessionId);
        UUID chatId = UUID.fromString(joinChatRequest.getChatId());
        try {
            User user = managerFactory.userManager().getUserBySessionId(sessionId).orElse(null);
            if (user == null) {
                return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "User not found for session");
            }
            mageServer.chatJoin(chatId, sessionId, user.getName());
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not join chat: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage leaveChat(String requestId, String sessionId, WsProto.LeaveChatRequest leaveChatRequest) {
        requireSession(sessionId);
        try {
            UUID chatId = UUID.fromString(leaveChatRequest.getChatId());
            mageServer.chatLeave(chatId, sessionId);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not leave chat: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage getServerState(String requestId, String sessionId) {
        requireSession(sessionId);
        ModelProto.ServerState serverState;
        try {
            serverState = mageServer.getServerState().toProto();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.SERVER_ERROR, "Could not get server state");
        }
        return WsProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId)
                .setSessionId(sessionId)
                .setServerStateResponse(WsProto.ServerStateResponse.newBuilder()
                        .setServerState(serverState)
                        .build())
                .build();
    }

    private WsProto.ServerMessage setUserData(String sessionId, WsProto.UserData msg) throws MageException {
        requireSession(sessionId);
        UserData userData = UserData.fromProto(msg.getUserData());
        MageVersion clientVersion = MageVersion.fromProto(msg.getMageVersion());
        managerFactory.sessionManager().setUserData(sessionId, userData, clientVersion.toString(), msg.getUserIdStr());
        return null; // No response expected for setUserData
    }

    private WsProto.ServerMessage getPromotionMessages(String requestId, String sessionId) {
        requireSession(sessionId);
        WsProto.PromotionMessagesResponse.Builder builder = WsProto.PromotionMessagesResponse.newBuilder();
        try {
            builder.addAllMessages(mageServer.serverGetPromotionMessages(sessionId));
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.SERVER_ERROR, "Could not get promotion messages");
        }
        return WsProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId)
                .setSessionId(sessionId)
                .setPromotionMessagesResponse(builder.build())
                .build();
    }

    private WsProto.ServerMessage createTable(String requestId, String sessionId, WsProto.CreateTableRequest createTableRequest) {
        requireSession(sessionId);
        try {
            ViewProto.TableView resultView = mageServer.roomCreateTable(sessionId, UUID.fromString(createTableRequest.getRoomId()), MatchOptions.fromProto(createTableRequest.getMatchOptions()));
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setTableViewResponse(WsProto.TableViewResponse.newBuilder()
                            .setTableView(resultView)
                            .build())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.SERVER_ERROR, "Could not create table: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage createTournamentTable(String requestId, String sessionId, WsProto.CreateTournamentTableRequest createTournamentTableRequest) {
        requireSession(sessionId);
        try {
            ViewProto.TableView resultView = mageServer.roomCreateTournament(sessionId, UUID.fromString(createTournamentTableRequest.getRoomId()), TournamentOptions.fromProto(createTournamentTableRequest.getTournamentOptions()));
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setTableViewResponse(WsProto.TableViewResponse.newBuilder()
                            .setTableView(resultView)
                            .build())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.SERVER_ERROR, "Could not create tournament table: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage joinTable(String requestId, String sessionId, WsProto.JoinTableRequest joinTableRequest) {
        requireSession(sessionId);
        try {
            boolean result = mageServer.roomJoinTable(sessionId,
                    UUID.fromString(joinTableRequest.getRoomId()),
                    UUID.fromString(joinTableRequest.getTableId()),
                    joinTableRequest.getPlayerName(),
                    PlayerType.getByDescription(joinTableRequest.getPlayerType()),
                    joinTableRequest.getAiSkill(),
                    DeckCardLists.fromProto(joinTableRequest.getDeckCardLists()),
                    joinTableRequest.getPassword()
            );
            if (!result) {
                return error(requestId, sessionId, WsProto.ErrorCode.SERVER_ERROR, "Could not join table");
            }
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.SERVER_ERROR, "Could not join table: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage removeTable(String requestId, String sessionId, WsProto.RemoveTableRequest removeTableRequest) {
        requireSession(sessionId);
        try {
            mageServer.tableRemove(sessionId, UUID.fromString(removeTableRequest.getRoomId()), UUID.fromString(removeTableRequest.getTableId()));
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.SERVER_ERROR, "Could not remove table: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage getTableChatId(String requestId, String sessionId, WsProto.TableChatIdRequest tableChatIdRequest) {
        requireSession(sessionId);
        try {
            UUID chatId = mageServer.chatFindByTable(UUID.fromString(tableChatIdRequest.getTableId()));
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setUuidResponse(WsProto.UUIDResponse.newBuilder()
                            .setUuid(chatId.toString())
                            .build())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.SERVER_ERROR, "Could not get table chat ID: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage getGameChatId(String requestId, String sessionId, WsProto.GameChatIdRequest gameChatIdRequest) {
        requireSession(sessionId);
        try {
            UUID chatId = mageServer.chatFindByGame(UUID.fromString(gameChatIdRequest.getGameId()));
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setUuidResponse(WsProto.UUIDResponse.newBuilder()
                            .setUuid(chatId == null ? "" : chatId.toString())
                            .build())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.SERVER_ERROR, "Could not get game chat ID: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage getTournamentChatId(String requestId, String sessionId, WsProto.TournamentChatIdRequest tournamentChatIdRequest) {
        requireSession(sessionId);
        try {
            UUID chatId = mageServer.chatFindByTournament(UUID.fromString(tournamentChatIdRequest.getTournamentId()));
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setUuidResponse(WsProto.UUIDResponse.newBuilder()
                            .setUuid(chatId == null ? "" : chatId.toString())
                            .build())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.SERVER_ERROR, "Could not get tournament chat ID: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage sendChatMessage(String requestId, String sessionId, WsProto.SendChatMessageRequest sendChatMessageRequest) {
        requireSession(sessionId);
        try {
            UUID chatId = UUID.fromString(sendChatMessageRequest.getChatId());
            String message = sendChatMessageRequest.getMessage();

            // Get username from session
            User user = managerFactory.userManager().getUserBySessionId(sessionId).orElse(null);
            if (user == null) {
                return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "User not found for session");
            }

            mageServer.chatSendMessage(chatId, user.getName(), message);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not send chat message: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage sendBroadcastMessage(String requestId, String sessionId, WsProto.SendBroadcastMessageRequest sendBroadcastMessageRequest) {
        requireSession(sessionId);
        try {
            String message = sendBroadcastMessageRequest.getMessage();
            mageServer.adminSendBroadcastMessage(sessionId, message);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not send broadcast message: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage sendPlayerData(String requestId, String sessionId, WsProto.SendPlayerDataRequest sendPlayerDataRequest) {
        requireSession(sessionId);
        try {
            UUID gameId = UUID.fromString(sendPlayerDataRequest.getGameId());

            // Handle different data types using oneof
            switch (sendPlayerDataRequest.getDataCase()) {
                case UUID_DATA:
                    UUID uuidData = sendPlayerDataRequest.getUuidData().isEmpty() ? null : UUID.fromString(sendPlayerDataRequest.getUuidData());
                    mageServer.sendPlayerUUID(gameId, sessionId, uuidData);
                    break;

                case BOOLEAN_DATA:
                    Boolean booleanData = sendPlayerDataRequest.getBooleanData();
                    mageServer.sendPlayerBoolean(gameId, sessionId, booleanData);
                    break;

                case INTEGER_DATA:
                    Integer integerData = sendPlayerDataRequest.getIntegerData();
                    mageServer.sendPlayerInteger(gameId, sessionId, integerData);
                    break;

                case STRING_DATA:
                    String stringData = sendPlayerDataRequest.getStringData();
                    mageServer.sendPlayerString(gameId, sessionId, stringData);
                    break;

                case DATA_NOT_SET:
                    return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "No player data provided");
            }

            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not send player data: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage getIsTableOwner(String requestId, String sessionId, WsProto.IsTableOwnerRequest isTableOwnerRequest) {
        requireSession(sessionId);
        try {
            boolean isOwner = mageServer.tableIsOwner(sessionId, UUID.fromString(isTableOwnerRequest.getRoomId()), UUID.fromString(isTableOwnerRequest.getTableId()));
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setBoolean(isOwner)
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.SERVER_ERROR, "Could not determine table owner status: " + e.getMessage());
        }
    }

    private static WsProto.ServerMessage error(String requestId, String sessionId, WsProto.ErrorCode code, String message) {
        return WsProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId == null ? "" : requestId)
                .setSessionId(sessionId == null ? "" : sessionId)
                .setError(WsProto.Error.newBuilder()
                        .setCode(code)
                        .setMessage(message == null ? "" : message)
                        .build())
                .build();
    }

    private static void requireSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new MissingSessionException("Missing sessionId");
        }
    }
}
