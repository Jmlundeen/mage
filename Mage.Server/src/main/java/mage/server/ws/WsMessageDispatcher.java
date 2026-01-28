package mage.server.ws;

import mage.MageException;
import mage.cards.decks.DeckCardLists;
import mage.constants.ManaType;
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
import mage.view.DraftPickView;
import mage.view.TournamentView;
import mage.ws.ProtocolVersion;
import mage.ws.v1.WsProto;
import mage.ws.v1.model.ModelProto;
import mage.ws.v1.view.ViewProto;
import org.apache.log4j.Logger;
import org.jboss.remoting.callback.InvokerCallbackHandler;

import java.util.HashSet;
import java.util.Set;
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
                case QUIT_MATCH_REQUEST -> quitMatch(requestId, sessionId, msg.getQuitMatchRequest());
                case QUIT_TOURNAMENT_REQUEST -> quitTournament(requestId, sessionId, msg.getQuitTournamentRequest());
                case QUIT_DRAFT_REQUEST -> quitDraft(requestId, sessionId, msg.getQuitDraftRequest());
                case IS_TABLE_OWNER_REQUEST -> getIsTableOwner(requestId, sessionId, msg.getIsTableOwnerRequest());
                case SUBMIT_DECK_REQUEST -> submitDeck(requestId, sessionId, msg.getSubmitDeckRequest());
                case UPDATE_DECK_REQUEST -> updateDeck(requestId, sessionId, msg.getUpdateDeckRequest());
                case SET_BOOSTER_LOADED_REQUEST -> setBoosterLoaded(requestId, sessionId, msg.getSetBoosterLoadedRequest());
                case SEND_CARD_PICK_REQUEST -> sendCardPick(requestId, sessionId, msg.getSendCardPickRequest());
                case SEND_CARD_MARK_REQUEST -> sendCardMark(requestId, sessionId, msg.getSendCardMarkRequest());
                case SEND_PLAYER_ACTION_REQUEST -> sendPlayerAction(requestId, sessionId, msg.getSendPlayerActionRequest());
                case JOIN_GAME_REQUEST -> joinGame(requestId, sessionId, msg.getJoinGameRequest());
                case ADMIN_REMOVE_TABLE_REQUEST -> adminRemoveTable(requestId, sessionId, msg.getAdminRemoveTableRequest());
                case JOIN_DRAFT_REQUEST -> joinDraft(requestId, sessionId, msg.getJoinDraftRequest());
                case JOIN_TOURNAMENT_REQUEST -> joinTournament(requestId, sessionId, msg.getJoinTournamentRequest());
                case LEAVE_TABLE_REQUEST -> leaveTable(requestId, sessionId, msg.getLeaveTableRequest());
                case SWAP_SEATS_REQUEST -> swapSeats(requestId, sessionId, msg.getSwapSeatsRequest());
                case START_MATCH_REQUEST -> startMatch(requestId, sessionId, msg.getStartMatchRequest());
                case START_TOURNAMENT_REQUEST -> startTournament(requestId, sessionId, msg.getStartTournamentRequest());
                case WATCH_GAME_REQUEST -> watchGame(requestId, sessionId, msg.getWatchGameRequest());
                case STOP_WATCHING_REQUEST -> stopWatching(requestId, sessionId, msg.getStopWatchingRequest());
                case REPLAY_GAME_REQUEST -> replayGame(requestId, sessionId, msg.getReplayGameRequest());
                case START_REPLAY_REQUEST -> startReplay(requestId, sessionId, msg.getStartReplayRequest());
                case STOP_REPLAY_REQUEST -> stopReplay(requestId, sessionId, msg.getStopReplayRequest());
                case NEXT_PLAY_REQUEST -> nextPlay(requestId, sessionId, msg.getNextPlayRequest());
                case PREVIOUS_PLAY_REQUEST -> previousPlay(requestId, sessionId, msg.getPreviousPlayRequest());
                case SKIP_FORWARD_REQUEST -> skipForward(requestId, sessionId, msg.getSkipForwardRequest());
                case GET_TABLE_REQUEST -> getTable(requestId, sessionId, msg.getGetTableRequest());
                case GET_TOURNAMENT_REQUEST -> getTournament(requestId, sessionId, msg.getGetTournamentRequest());
                case WATCH_TABLE_REQUEST -> watchTable(requestId, sessionId, msg.getWatchTableRequest());
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

                case MANA_TYPE_DATA:
                    UUID playerId = sendPlayerDataRequest.getPlayerId().isEmpty() ? null : UUID.fromString(sendPlayerDataRequest.getPlayerId());
                    ManaType manaType = ManaType.valueOf(sendPlayerDataRequest.getManaTypeData());
                    mageServer.sendPlayerManaType(gameId, playerId, sessionId, manaType);
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

    private WsProto.ServerMessage quitMatch(String requestId, String sessionId, WsProto.QuitMatchRequest quitMatchRequest) {
        requireSession(sessionId);
        try {
            UUID gameId = UUID.fromString(quitMatchRequest.getGameId());
            mageServer.matchQuit(gameId, sessionId);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not quit match: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage quitTournament(String requestId, String sessionId, WsProto.QuitTournamentRequest quitTournamentRequest) {
        requireSession(sessionId);
        try {
            UUID tournamentId = UUID.fromString(quitTournamentRequest.getTournamentId());
            mageServer.tournamentQuit(tournamentId, sessionId);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not quit tournament: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage quitDraft(String requestId, String sessionId, WsProto.QuitDraftRequest quitDraftRequest) {
        requireSession(sessionId);
        try {
            UUID draftId = UUID.fromString(quitDraftRequest.getDraftId());
            mageServer.draftQuit(draftId, sessionId);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not quit draft: " + e.getMessage());
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

    private WsProto.ServerMessage submitDeck(String requestId, String sessionId, WsProto.SubmitDeckRequest submitDeckRequest) {
        requireSession(sessionId);
        try {
            UUID tableId = UUID.fromString(submitDeckRequest.getTableId());
            DeckCardLists deckCardLists = DeckCardLists.fromProto(submitDeckRequest.getDeckCardLists());
            boolean result = mageServer.deckSubmit(sessionId, tableId, deckCardLists);
            if (!result) {
                return error(requestId, sessionId, WsProto.ErrorCode.SERVER_ERROR, "Could not submit deck");
            }
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not submit deck: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage updateDeck(String requestId, String sessionId, WsProto.UpdateDeckRequest updateDeckRequest) {
        requireSession(sessionId);
        try {
            UUID tableId = UUID.fromString(updateDeckRequest.getTableId());
            DeckCardLists deckCardLists = DeckCardLists.fromProto(updateDeckRequest.getDeckCardLists());
            mageServer.deckSave(sessionId, tableId, deckCardLists);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not update deck: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage setBoosterLoaded(String requestId, String sessionId, WsProto.SetBoosterLoadedRequest setBoosterLoadedRequest) {
        requireSession(sessionId);
        try {
            UUID draftId = UUID.fromString(setBoosterLoadedRequest.getDraftId());
            mageServer.draftSetBoosterLoaded(draftId, sessionId);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not set booster loaded: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage sendCardPick(String requestId, String sessionId, WsProto.SendCardPickRequest sendCardPickRequest) {
        requireSession(sessionId);
        try {
            UUID draftId = UUID.fromString(sendCardPickRequest.getDraftId());
            UUID cardId = UUID.fromString(sendCardPickRequest.getCardId());

            // Convert hiddenCards from list of strings to Set<UUID>
            Set<UUID> hiddenCards = new HashSet<>();
            for (String hiddenCardStr : sendCardPickRequest.getHiddenCardsList()) {
                if (!hiddenCardStr.isEmpty()) {
                    hiddenCards.add(UUID.fromString(hiddenCardStr));
                }
            }

            DraftPickView draftPickView = mageServer.sendDraftCardPick(draftId, sessionId, cardId, hiddenCards);

            if (draftPickView == null) {
                return WsProto.ServerMessage.newBuilder()
                        .setProtocolVersion(ProtocolVersion.getVersion())
                        .setRequestId(requestId)
                        .setSessionId(sessionId)
                        .setDraftPickViewResponse(WsProto.DraftPickViewResponse.newBuilder()
                                .build())
                        .build();
            }

            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setDraftPickViewResponse(WsProto.DraftPickViewResponse.newBuilder()
                            .setDraftPickView(draftPickView.toProto())
                            .build())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not send card pick: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage sendCardMark(String requestId, String sessionId, WsProto.SendCardMarkRequest sendCardMarkRequest) {
        requireSession(sessionId);
        try {
            UUID draftId = UUID.fromString(sendCardMarkRequest.getDraftId());
            UUID cardId = UUID.fromString(sendCardMarkRequest.getCardId());
            mageServer.sendDraftCardMark(draftId, sessionId, cardId);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not send card mark: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage sendPlayerAction(String requestId, String sessionId, WsProto.SendPlayerActionRequest sendPlayerActionRequest) {
        requireSession(sessionId);
        try {
            String playerActionStr = sendPlayerActionRequest.getPlayerAction();
            UUID gameId = UUID.fromString(sendPlayerActionRequest.getGameId());

            // Convert string to PlayerAction enum
            mage.constants.PlayerAction playerAction = mage.constants.PlayerAction.valueOf(playerActionStr);

            // Extract typed data from oneof field
            Object data = null;
            switch (sendPlayerActionRequest.getDataCase()) {
                case INTEGERDATA:
                    data = sendPlayerActionRequest.getIntegerData();
                    break;
                case UUIDDATA:
                    String uuidStr = sendPlayerActionRequest.getUuidData();
                    if (!uuidStr.isEmpty()) {
                        data = UUID.fromString(uuidStr);
                    }
                    break;
                case STRINGDATA:
                    data = sendPlayerActionRequest.getStringData();
                    break;
                case DATA_NOT_SET:
                    // data remains null
                    break;
            }

            mageServer.sendPlayerAction(playerAction, gameId, sessionId, data);

            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not send player action: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Invalid player action: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage joinGame(String requestId, String sessionId, WsProto.JoinGameRequest joinGameRequest) {
        requireSession(sessionId);
        try {
            UUID gameId = UUID.fromString(joinGameRequest.getGameId());
            mageServer.gameJoin(gameId, sessionId);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not join game: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage adminRemoveTable(String requestId, String sessionId, WsProto.AdminRemoveTableRequest adminRemoveTableRequest) {
        requireSession(sessionId);
        try {
            UUID tableId = UUID.fromString(adminRemoveTableRequest.getTableId());
            mageServer.adminTableRemove(sessionId, tableId);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not remove table: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage joinDraft(String requestId, String sessionId, WsProto.JoinDraftRequest joinDraftRequest) {
        requireSession(sessionId);
        try {
            UUID draftId = UUID.fromString(joinDraftRequest.getDraftId());
            mageServer.draftJoin(draftId, sessionId);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not join draft: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage joinTournament(String requestId, String sessionId, WsProto.JoinTournamentRequest joinTournamentRequest) {
        requireSession(sessionId);
        try {
            UUID tournamentId = UUID.fromString(joinTournamentRequest.getTournamentId());
            mageServer.tournamentJoin(tournamentId, sessionId);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not join tournament: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage leaveTable(String requestId, String sessionId, WsProto.LeaveTableRequest leaveTableRequest) {
        requireSession(sessionId);
        try {
            UUID roomId = UUID.fromString(leaveTableRequest.getRoomId());
            UUID tableId = UUID.fromString(leaveTableRequest.getTableId());
            boolean result = mageServer.roomLeaveTableOrTournament(sessionId, roomId, tableId);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setBoolean(result)
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not leave table: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage swapSeats(String requestId, String sessionId, WsProto.SwapSeatsRequest swapSeatsRequest) {
        requireSession(sessionId);
        try {
            UUID roomId = UUID.fromString(swapSeatsRequest.getRoomId());
            UUID tableId = UUID.fromString(swapSeatsRequest.getTableId());
            int seatNum1 = swapSeatsRequest.getSeatNum1();
            int seatNum2 = swapSeatsRequest.getSeatNum2();
            mageServer.tableSwapSeats(sessionId, roomId, tableId, seatNum1, seatNum2);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not swap seats: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage startMatch(String requestId, String sessionId, WsProto.StartMatchRequest startMatchRequest) {
        requireSession(sessionId);
        try {
            UUID roomId = UUID.fromString(startMatchRequest.getRoomId());
            UUID tableId = UUID.fromString(startMatchRequest.getTableId());
            boolean result = mageServer.matchStart(sessionId, roomId, tableId);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setBoolean(result)
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not start match: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage startTournament(String requestId, String sessionId, WsProto.StartTournamentRequest startTournamentRequest) {
        requireSession(sessionId);
        try {
            UUID roomId = UUID.fromString(startTournamentRequest.getRoomId());
            UUID tableId = UUID.fromString(startTournamentRequest.getTableId());
            boolean result = mageServer.tournamentStart(sessionId, roomId, tableId);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setBoolean(result)
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not start tournament: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage watchGame(String requestId, String sessionId, WsProto.WatchGameRequest watchGameRequest) {
        requireSession(sessionId);
        try {
            UUID gameId = UUID.fromString(watchGameRequest.getGameId());
            boolean result = mageServer.gameWatchStart(gameId, sessionId);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setBoolean(result)
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not watch game: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage stopWatching(String requestId, String sessionId, WsProto.StopWatchingRequest stopWatchingRequest) {
        requireSession(sessionId);
        try {
            UUID gameId = UUID.fromString(stopWatchingRequest.getGameId());
            mageServer.gameWatchStop(gameId, sessionId);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not stop watching: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage replayGame(String requestId, String sessionId, WsProto.ReplayGameRequest replayGameRequest) {
        requireSession(sessionId);
        try {
            UUID gameId = UUID.fromString(replayGameRequest.getGameId());
            mageServer.replayInit(gameId, sessionId);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not replay game: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage startReplay(String requestId, String sessionId, WsProto.StartReplayRequest startReplayRequest) {
        requireSession(sessionId);
        try {
            UUID gameId = UUID.fromString(startReplayRequest.getGameId());
            mageServer.replayStart(gameId, sessionId);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not start replay: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage stopReplay(String requestId, String sessionId, WsProto.StopReplayRequest stopReplayRequest) {
        requireSession(sessionId);
        try {
            UUID gameId = UUID.fromString(stopReplayRequest.getGameId());
            mageServer.replayStop(gameId, sessionId);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not stop replay: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage nextPlay(String requestId, String sessionId, WsProto.NextPlayRequest nextPlayRequest) {
        requireSession(sessionId);
        try {
            UUID gameId = UUID.fromString(nextPlayRequest.getGameId());
            mageServer.replayNext(gameId, sessionId);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not move to next play: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage previousPlay(String requestId, String sessionId, WsProto.PreviousPlayRequest previousPlayRequest) {
        requireSession(sessionId);
        try {
            UUID gameId = UUID.fromString(previousPlayRequest.getGameId());
            mageServer.replayPrevious(gameId, sessionId);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not move to previous play: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage skipForward(String requestId, String sessionId, WsProto.SkipForwardRequest skipForwardRequest) {
        requireSession(sessionId);
        try {
            UUID gameId = UUID.fromString(skipForwardRequest.getGameId());
            int moves = skipForwardRequest.getMoves();
            mageServer.replaySkipForward(gameId, sessionId, moves);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(WsProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not skip forward: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage getTable(String requestId, String sessionId, WsProto.GetTableRequest getTableRequest) {
        requireSession(sessionId);
        try {
            UUID roomId = UUID.fromString(getTableRequest.getRoomId());
            UUID tableId = UUID.fromString(getTableRequest.getTableId());
            ViewProto.TableView tableView = mageServer.roomGetTableById(roomId, tableId);
            if (tableView == null) {
                return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Table not found");
            }
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setTableViewResponse(WsProto.TableViewResponse.newBuilder()
                            .setTableView(tableView)
                            .build())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not get table: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage getTournament(String requestId, String sessionId, WsProto.GetTournamentRequest getTournamentRequest) {
        requireSession(sessionId);
        try {
            UUID tournamentId = UUID.fromString(getTournamentRequest.getTournamentId());
            TournamentView tournamentView = mageServer.tournamentFindById(tournamentId);
            if (tournamentView == null) {
                return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Tournament not found");
            }
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setTournamentViewResponse(WsProto.TournamentViewResponse.newBuilder()
                            .setTournamentView(tournamentView.toProto())
                            .build())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not get tournament: " + e.getMessage());
        }
    }

    private WsProto.ServerMessage watchTable(String requestId, String sessionId, WsProto.WatchTableRequest watchTableRequest) {
        requireSession(sessionId);
        try {
            UUID roomId = UUID.fromString(watchTableRequest.getRoomId());
            UUID tableId = UUID.fromString(watchTableRequest.getTableId());
            boolean result = mageServer.roomWatchTable(sessionId, roomId, tableId);
            return WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setBoolean(result)
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MAGE_EXCEPTION, "Could not watch table: " + e.getMessage());
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
