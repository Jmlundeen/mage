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
import mage.view.MatchView;
import mage.view.TableView;
import mage.view.TournamentView;
import mage.ws.MessageProto;
import mage.ws.ProtocolVersion;
import mage.ws.model.ModelProto;
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

    public MessageProto.ServerMessage handle(MessageProto.ClientMessage msg) {
        String requestId = msg.getRequestId();
        String sessionId = msg.getSessionId();

        if (msg.getProtocolVersion().isEmpty() || !ProtocolVersion.equalsStrict(msg.getProtocolVersion())) {
            return error(requestId, sessionId, MessageProto.ErrorCode.INVALID_PROTOCOL_VERSION,
                    "Invalid protocolVersion. ServerProtocol=" + ProtocolVersion.getVersion());
        }

        if (requestId.trim().isEmpty()) {
            return error("", sessionId, MessageProto.ErrorCode.MISSING_REQUEST_ID, "Missing requestId");
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
                case WATCH_TOURNAMENT_TABLE_REQUEST -> watchTournamentTable(requestId, sessionId, msg.getWatchTournamentTableRequest());
                case JOIN_TOURNAMENT_TABLE_REQUEST -> joinTournamentTable(requestId, sessionId, msg.getJoinTournamentTableRequest());
                case ADMIN_DISCONNECT_USER_REQUEST -> adminDisconnectUser(requestId, sessionId, msg.getAdminDisconnectUserRequest());
                case ADMIN_END_USER_SESSION_REQUEST -> adminEndUserSession(requestId, sessionId, msg.getAdminEndUserSessionRequest());
                case ADMIN_MUTE_USER_REQUEST -> adminMuteUser(requestId, sessionId, msg.getAdminMuteUserRequest());
                case ADMIN_ACTIVATE_USER_REQUEST -> adminActivateUser(requestId, sessionId, msg.getAdminActivateUserRequest());
                case ADMIN_TOGGLE_ACTIVATE_USER_REQUEST -> adminToggleActivateUser(requestId, sessionId, msg.getAdminToggleActivateUserRequest());
                case ADMIN_LOCK_USER_REQUEST -> adminLockUser(requestId, sessionId, msg.getAdminLockUserRequest());
                default -> error(requestId, sessionId, MessageProto.ErrorCode.UNKNOWN_MESSAGE_TYPE, "Unknown message type");
            };
        } catch (MissingSessionException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MISSING_SESSION_ID, e.getMessage());
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            logger.error("WS dispatch error", e);
            return error(requestId, sessionId, MessageProto.ErrorCode.SERVER_ERROR, "Server error");
        }
    }

    private MessageProto.ServerMessage disconnect(String requestId, String sessionId, MessageProto.Disconnect msg) {
        requireSession(sessionId);
        DisconnectReason reason = DisconnectReason.DisconnectedByUser;
        if (SessionImpl.KEEP_MY_OLD_SESSION.equals(sessionId)) {
            // Special case: client wants to disconnect but keep tables (e.g. for reconnect before inactive timeout)
            reason = DisconnectReason.DisconnectedByUserButKeepTables;
        }
        managerFactory.sessionManager().disconnect(sessionId, reason, true);
        return null; // No response expected for disconnect
    }

    private MessageProto.ServerMessage ping(String requestId, String sessionId, MessageProto.PingRequest req) {
        requireSession(sessionId);
        try {
            if (mageServer.ping(sessionId, req.getLastPingInfo())) {
                return MessageProto.ServerMessage.newBuilder()
                        .setProtocolVersion(ProtocolVersion.getVersion())
                        .setRequestId(requestId)
                        .setSessionId(sessionId)
                        .setAck(MessageProto.Ack.getDefaultInstance())
                        .build();
            }
            else {
                return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Ping rejected by server");
            }
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Ping failed: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage auth(String requestId, String sessionId, MessageProto.AuthRequest req) {
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
            return error(requestId, sessionId, MessageProto.ErrorCode.AUTH_FAILED, "Auth failed");
        }

        return MessageProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId)
                .setSessionId(sessionId)
                .setAuth(MessageProto.AuthResponse.newBuilder()
                        .setOk(true)
                        .setMessage("OK")
                        .build())
                .build();
    }

    private MessageProto.ServerMessage getMainRoomId(String requestId, String sessionId, MessageProto.MainRoomIdRequest req) {
        requireSession(sessionId);
        UUID mainRoomId;
        try {
            mainRoomId = mageServer.serverGetMainRoomId();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.SERVER_ERROR, "Could not get main room ID");
        }
        return MessageProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId)
                .setSessionId(sessionId)
                .setUuidResponse(MessageProto.UUIDResponse.newBuilder()
                        .setUuid(mainRoomId.toString())
                        .build())
                .build();
    }

    private MessageProto.ServerMessage getChatRoomId(String requestId, String sessionId, MessageProto.RoomChatIdRequest roomChatIdRequest) {
        requireSession(sessionId);
        UUID roomId = UUID.fromString(roomChatIdRequest.getRoomId());
        UUID chatRoomId;
        try {
            chatRoomId = mageServer.chatFindByRoom(roomId);
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.SERVER_ERROR, "Could not get chat room ID");
        }
        return MessageProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId)
                .setSessionId(sessionId)
                .setUuidResponse(MessageProto.UUIDResponse.newBuilder()
                        .setUuid(chatRoomId.toString())
                        .build())
                .build();
    }

    private MessageProto.ServerMessage getTables(String requestId, String sessionId, MessageProto.TablesRequest req) throws MageException {
        requireSession(sessionId);
        UUID roomId = req.getRoomId().isEmpty() ? null : UUID.fromString(req.getRoomId());
        if (roomId == null) {
            return error(requestId, sessionId, MessageProto.ErrorCode.SERVER_ERROR, "Missing roomId");
        }

        MessageProto.TablesResponse.Builder builder = MessageProto.TablesResponse.newBuilder();
        builder.addAllTables(mageServer.roomGetAllTables(roomId).stream()
                .map(TableView::toProto)
                .toList());

        return MessageProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId)
                .setSessionId(sessionId)
                .setLobbyGetTables(builder.build())
                .build();
    }

    private MessageProto.ServerMessage getFinishedMatches(String requestId, String sessionId, MessageProto.FinishedMatchesRequest req) throws MageException {
        requireSession(sessionId);
        UUID roomId = req.getRoomId().isEmpty() ? null : UUID.fromString(req.getRoomId());
        if (roomId == null) {
            return error(requestId, sessionId, MessageProto.ErrorCode.SERVER_ERROR, "Missing roomId");
        }

        MessageProto.FinishedMatchesResponse.Builder builder = MessageProto.FinishedMatchesResponse.newBuilder();
        builder.addAllFinishedMatches(mageServer.roomGetFinishedMatches(roomId).stream()
                .map(MatchView::toProto)
                .toList());

        return MessageProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId)
                .setSessionId(sessionId)
                .setFinishedMatchesResponse(builder.build())
                .build();
    }

    private MessageProto.ServerMessage getRoomUsers(String requestId, String sessionId, MessageProto.RoomUsersRequest req) throws MageException {
        requireSession(sessionId);
        UUID roomId = req.getRoomId().isEmpty() ? null : UUID.fromString(req.getRoomId());
        if (roomId == null) {
            return error(requestId, sessionId, MessageProto.ErrorCode.SERVER_ERROR, "Missing roomId");
        }

        MessageProto.RoomUsersResponse.Builder builder = MessageProto.RoomUsersResponse.newBuilder();
        builder.setRoomUsers(mageServer.roomGetUsers(roomId).toProto());

        return MessageProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId)
                .setSessionId(sessionId)
                .setRoomUsersResponse(builder.build())
                .build();
    }

    private MessageProto.ServerMessage getLobbyInfo(String requestId, String sessionId, MessageProto.LobbyInfoRequest req) throws MageException {
        requireSession(sessionId);
        UUID roomId = req.getRoomId().isEmpty() ? null : UUID.fromString(req.getRoomId());
        if (roomId == null) {
            return error(requestId, sessionId, MessageProto.ErrorCode.SERVER_ERROR, "Missing roomId");
        }

        MessageProto.LobbyInfoResponse.Builder builder = MessageProto.LobbyInfoResponse.newBuilder();
        builder.addAllTables(mageServer.roomGetAllTables(roomId).stream()
                .map(TableView::toProto)
                .toList());
        builder.addAllFinishedMatches(mageServer.roomGetFinishedMatches(roomId).stream()
                .map(MatchView::toProto)
                .toList());
        builder.setRoomUsers(mageServer.roomGetUsers(roomId).toProto());

        return MessageProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId)
                .setSessionId(sessionId)
                .setLobbyGetInfo(builder.build())
                .build();
    }

    private MessageProto.ServerMessage joinChat(String requestId, String sessionId, MessageProto.JoinChatRequest joinChatRequest) {
        requireSession(sessionId);
        UUID chatId = UUID.fromString(joinChatRequest.getChatId());
        try {
            User user = managerFactory.userManager().getUserBySessionId(sessionId).orElse(null);
            if (user == null) {
                return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "User not found for session");
            }
            mageServer.chatJoin(chatId, sessionId, user.getName());
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not join chat: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage leaveChat(String requestId, String sessionId, MessageProto.LeaveChatRequest leaveChatRequest) {
        requireSession(sessionId);
        try {
            UUID chatId = UUID.fromString(leaveChatRequest.getChatId());
            mageServer.chatLeave(chatId, sessionId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not leave chat: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage getServerState(String requestId, String sessionId) {
        requireSession(sessionId);
        ModelProto.ServerState serverState;
        try {
            serverState = mageServer.getServerState().toProto();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.SERVER_ERROR, "Could not get server state");
        }
        return MessageProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId)
                .setSessionId(sessionId)
                .setServerStateResponse(MessageProto.ServerStateResponse.newBuilder()
                        .setServerState(serverState)
                        .build())
                .build();
    }

    private MessageProto.ServerMessage setUserData(String sessionId, MessageProto.UserData msg) throws MageException {
        requireSession(sessionId);
        UserData userData = UserData.fromProto(msg.getUserData());
        MageVersion clientVersion = MageVersion.fromProto(msg.getMageVersion());
        managerFactory.sessionManager().setUserData(sessionId, userData, clientVersion.toString(), msg.getUserIdStr());
        return null; // No response expected for setUserData
    }

    private MessageProto.ServerMessage getPromotionMessages(String requestId, String sessionId) {
        requireSession(sessionId);
        MessageProto.PromotionMessagesResponse.Builder builder = MessageProto.PromotionMessagesResponse.newBuilder();
        try {
            builder.addAllMessages(mageServer.serverGetPromotionMessages(sessionId));
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.SERVER_ERROR, "Could not get promotion messages");
        }
        return MessageProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId)
                .setSessionId(sessionId)
                .setPromotionMessagesResponse(builder.build())
                .build();
    }

    private MessageProto.ServerMessage createTable(String requestId, String sessionId, MessageProto.CreateTableRequest createTableRequest) {
        requireSession(sessionId);
        try {
            TableView resultView = mageServer.roomCreateTable(sessionId, UUID.fromString(createTableRequest.getRoomId()), MatchOptions.fromProto(createTableRequest.getMatchOptions()));
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setTableViewResponse(MessageProto.TableViewResponse.newBuilder()
                            .setTableView(resultView.toProto())
                            .build())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.SERVER_ERROR, "Could not create table: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage createTournamentTable(String requestId, String sessionId, MessageProto.CreateTournamentTableRequest createTournamentTableRequest) {
        requireSession(sessionId);
        try {
            TableView resultView = mageServer.roomCreateTournament(sessionId, UUID.fromString(createTournamentTableRequest.getRoomId()), TournamentOptions.fromProto(createTournamentTableRequest.getTournamentOptions()));
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setTableViewResponse(MessageProto.TableViewResponse.newBuilder()
                            .setTableView(resultView.toProto())
                            .build())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.SERVER_ERROR, "Could not create tournament table: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage joinTable(String requestId, String sessionId, MessageProto.JoinTableRequest joinTableRequest) {
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
                return error(requestId, sessionId, MessageProto.ErrorCode.SERVER_ERROR, "Could not join table");
            }
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.SERVER_ERROR, "Could not join table: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage removeTable(String requestId, String sessionId, MessageProto.RemoveTableRequest removeTableRequest) {
        requireSession(sessionId);
        try {
            mageServer.tableRemove(sessionId, UUID.fromString(removeTableRequest.getRoomId()), UUID.fromString(removeTableRequest.getTableId()));
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.SERVER_ERROR, "Could not remove table: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage getTableChatId(String requestId, String sessionId, MessageProto.TableChatIdRequest tableChatIdRequest) {
        requireSession(sessionId);
        try {
            UUID chatId = mageServer.chatFindByTable(UUID.fromString(tableChatIdRequest.getTableId()));
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setUuidResponse(MessageProto.UUIDResponse.newBuilder()
                            .setUuid(chatId.toString())
                            .build())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.SERVER_ERROR, "Could not get table chat ID: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage getGameChatId(String requestId, String sessionId, MessageProto.GameChatIdRequest gameChatIdRequest) {
        requireSession(sessionId);
        try {
            UUID chatId = mageServer.chatFindByGame(UUID.fromString(gameChatIdRequest.getGameId()));
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setUuidResponse(MessageProto.UUIDResponse.newBuilder()
                            .setUuid(chatId == null ? "" : chatId.toString())
                            .build())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.SERVER_ERROR, "Could not get game chat ID: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage getTournamentChatId(String requestId, String sessionId, MessageProto.TournamentChatIdRequest tournamentChatIdRequest) {
        requireSession(sessionId);
        try {
            UUID chatId = mageServer.chatFindByTournament(UUID.fromString(tournamentChatIdRequest.getTournamentId()));
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setUuidResponse(MessageProto.UUIDResponse.newBuilder()
                            .setUuid(chatId == null ? "" : chatId.toString())
                            .build())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.SERVER_ERROR, "Could not get tournament chat ID: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage sendChatMessage(String requestId, String sessionId, MessageProto.SendChatMessageRequest sendChatMessageRequest) {
        requireSession(sessionId);
        try {
            UUID chatId = UUID.fromString(sendChatMessageRequest.getChatId());
            String message = sendChatMessageRequest.getMessage();

            // Get username from session
            User user = managerFactory.userManager().getUserBySessionId(sessionId).orElse(null);
            if (user == null) {
                return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "User not found for session");
            }

            mageServer.chatSendMessage(chatId, user.getName(), message);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not send chat message: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage sendBroadcastMessage(String requestId, String sessionId, MessageProto.SendBroadcastMessageRequest sendBroadcastMessageRequest) {
        requireSession(sessionId);
        try {
            String message = sendBroadcastMessageRequest.getMessage();
            mageServer.adminSendBroadcastMessage(sessionId, message);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not send broadcast message: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage sendPlayerData(String requestId, String sessionId, MessageProto.SendPlayerDataRequest sendPlayerDataRequest) {
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
                    return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "No player data provided");
            }

            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not send player data: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage quitMatch(String requestId, String sessionId, MessageProto.QuitMatchRequest quitMatchRequest) {
        requireSession(sessionId);
        try {
            UUID gameId = UUID.fromString(quitMatchRequest.getGameId());
            mageServer.matchQuit(gameId, sessionId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not quit match: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage quitTournament(String requestId, String sessionId, MessageProto.QuitTournamentRequest quitTournamentRequest) {
        requireSession(sessionId);
        try {
            UUID tournamentId = UUID.fromString(quitTournamentRequest.getTournamentId());
            mageServer.tournamentQuit(tournamentId, sessionId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not quit tournament: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage quitDraft(String requestId, String sessionId, MessageProto.QuitDraftRequest quitDraftRequest) {
        requireSession(sessionId);
        try {
            UUID draftId = UUID.fromString(quitDraftRequest.getDraftId());
            mageServer.draftQuit(draftId, sessionId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not quit draft: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage getIsTableOwner(String requestId, String sessionId, MessageProto.IsTableOwnerRequest isTableOwnerRequest) {
        requireSession(sessionId);
        try {
            boolean isOwner = mageServer.tableIsOwner(sessionId, UUID.fromString(isTableOwnerRequest.getRoomId()), UUID.fromString(isTableOwnerRequest.getTableId()));
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setBoolean(isOwner)
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.SERVER_ERROR, "Could not determine table owner status: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage submitDeck(String requestId, String sessionId, MessageProto.SubmitDeckRequest submitDeckRequest) {
        requireSession(sessionId);
        try {
            UUID tableId = UUID.fromString(submitDeckRequest.getTableId());
            DeckCardLists deckCardLists = DeckCardLists.fromProto(submitDeckRequest.getDeckCardLists());
            boolean result = mageServer.deckSubmit(sessionId, tableId, deckCardLists);
            if (!result) {
                return error(requestId, sessionId, MessageProto.ErrorCode.SERVER_ERROR, "Could not submit deck");
            }
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not submit deck: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage updateDeck(String requestId, String sessionId, MessageProto.UpdateDeckRequest updateDeckRequest) {
        requireSession(sessionId);
        try {
            UUID tableId = UUID.fromString(updateDeckRequest.getTableId());
            DeckCardLists deckCardLists = DeckCardLists.fromProto(updateDeckRequest.getDeckCardLists());
            mageServer.deckSave(sessionId, tableId, deckCardLists);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not update deck: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage setBoosterLoaded(String requestId, String sessionId, MessageProto.SetBoosterLoadedRequest setBoosterLoadedRequest) {
        requireSession(sessionId);
        try {
            UUID draftId = UUID.fromString(setBoosterLoadedRequest.getDraftId());
            mageServer.draftSetBoosterLoaded(draftId, sessionId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not set booster loaded: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage sendCardPick(String requestId, String sessionId, MessageProto.SendCardPickRequest sendCardPickRequest) {
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
                return MessageProto.ServerMessage.newBuilder()
                        .setProtocolVersion(ProtocolVersion.getVersion())
                        .setRequestId(requestId)
                        .setSessionId(sessionId)
                        .setDraftPickViewResponse(MessageProto.DraftPickViewResponse.newBuilder()
                                .build())
                        .build();
            }

            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setDraftPickViewResponse(MessageProto.DraftPickViewResponse.newBuilder()
                            .setDraftPickView(draftPickView.toProto())
                            .build())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not send card pick: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage sendCardMark(String requestId, String sessionId, MessageProto.SendCardMarkRequest sendCardMarkRequest) {
        requireSession(sessionId);
        try {
            UUID draftId = UUID.fromString(sendCardMarkRequest.getDraftId());
            UUID cardId = UUID.fromString(sendCardMarkRequest.getCardId());
            mageServer.sendDraftCardMark(draftId, sessionId, cardId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not send card mark: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage sendPlayerAction(String requestId, String sessionId, MessageProto.SendPlayerActionRequest sendPlayerActionRequest) {
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

            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not send player action: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Invalid player action: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage joinGame(String requestId, String sessionId, MessageProto.JoinGameRequest joinGameRequest) {
        requireSession(sessionId);
        try {
            UUID gameId = UUID.fromString(joinGameRequest.getGameId());
            mageServer.gameJoin(gameId, sessionId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not join game: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage adminRemoveTable(String requestId, String sessionId, MessageProto.AdminRemoveTableRequest adminRemoveTableRequest) {
        requireSession(sessionId);
        try {
            UUID tableId = UUID.fromString(adminRemoveTableRequest.getTableId());
            mageServer.adminTableRemove(sessionId, tableId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not remove table: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage joinDraft(String requestId, String sessionId, MessageProto.JoinDraftRequest joinDraftRequest) {
        requireSession(sessionId);
        try {
            UUID draftId = UUID.fromString(joinDraftRequest.getDraftId());
            mageServer.draftJoin(draftId, sessionId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not join draft: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage joinTournament(String requestId, String sessionId, MessageProto.JoinTournamentRequest joinTournamentRequest) {
        requireSession(sessionId);
        try {
            UUID tournamentId = UUID.fromString(joinTournamentRequest.getTournamentId());
            mageServer.tournamentJoin(tournamentId, sessionId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not join tournament: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage leaveTable(String requestId, String sessionId, MessageProto.LeaveTableRequest leaveTableRequest) {
        requireSession(sessionId);
        try {
            UUID roomId = UUID.fromString(leaveTableRequest.getRoomId());
            UUID tableId = UUID.fromString(leaveTableRequest.getTableId());
            boolean result = mageServer.roomLeaveTableOrTournament(sessionId, roomId, tableId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setBoolean(result)
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not leave table: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage swapSeats(String requestId, String sessionId, MessageProto.SwapSeatsRequest swapSeatsRequest) {
        requireSession(sessionId);
        try {
            UUID roomId = UUID.fromString(swapSeatsRequest.getRoomId());
            UUID tableId = UUID.fromString(swapSeatsRequest.getTableId());
            int seatNum1 = swapSeatsRequest.getSeatNum1();
            int seatNum2 = swapSeatsRequest.getSeatNum2();
            mageServer.tableSwapSeats(sessionId, roomId, tableId, seatNum1, seatNum2);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not swap seats: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage startMatch(String requestId, String sessionId, MessageProto.StartMatchRequest startMatchRequest) {
        requireSession(sessionId);
        try {
            UUID roomId = UUID.fromString(startMatchRequest.getRoomId());
            UUID tableId = UUID.fromString(startMatchRequest.getTableId());
            boolean result = mageServer.matchStart(sessionId, roomId, tableId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setBoolean(result)
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not start match: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage startTournament(String requestId, String sessionId, MessageProto.StartTournamentRequest startTournamentRequest) {
        requireSession(sessionId);
        try {
            UUID roomId = UUID.fromString(startTournamentRequest.getRoomId());
            UUID tableId = UUID.fromString(startTournamentRequest.getTableId());
            boolean result = mageServer.tournamentStart(sessionId, roomId, tableId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setBoolean(result)
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not start tournament: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage watchGame(String requestId, String sessionId, MessageProto.WatchGameRequest watchGameRequest) {
        requireSession(sessionId);
        try {
            UUID gameId = UUID.fromString(watchGameRequest.getGameId());
            boolean result = mageServer.gameWatchStart(gameId, sessionId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setBoolean(result)
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not watch game: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage stopWatching(String requestId, String sessionId, MessageProto.StopWatchingRequest stopWatchingRequest) {
        requireSession(sessionId);
        try {
            UUID gameId = UUID.fromString(stopWatchingRequest.getGameId());
            mageServer.gameWatchStop(gameId, sessionId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not stop watching: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage replayGame(String requestId, String sessionId, MessageProto.ReplayGameRequest replayGameRequest) {
        requireSession(sessionId);
        try {
            UUID gameId = UUID.fromString(replayGameRequest.getGameId());
            mageServer.replayInit(gameId, sessionId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not replay game: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage startReplay(String requestId, String sessionId, MessageProto.StartReplayRequest startReplayRequest) {
        requireSession(sessionId);
        try {
            UUID gameId = UUID.fromString(startReplayRequest.getGameId());
            mageServer.replayStart(gameId, sessionId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not start replay: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage stopReplay(String requestId, String sessionId, MessageProto.StopReplayRequest stopReplayRequest) {
        requireSession(sessionId);
        try {
            UUID gameId = UUID.fromString(stopReplayRequest.getGameId());
            mageServer.replayStop(gameId, sessionId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not stop replay: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage nextPlay(String requestId, String sessionId, MessageProto.NextPlayRequest nextPlayRequest) {
        requireSession(sessionId);
        try {
            UUID gameId = UUID.fromString(nextPlayRequest.getGameId());
            mageServer.replayNext(gameId, sessionId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not move to next play: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage previousPlay(String requestId, String sessionId, MessageProto.PreviousPlayRequest previousPlayRequest) {
        requireSession(sessionId);
        try {
            UUID gameId = UUID.fromString(previousPlayRequest.getGameId());
            mageServer.replayPrevious(gameId, sessionId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not move to previous play: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage skipForward(String requestId, String sessionId, MessageProto.SkipForwardRequest skipForwardRequest) {
        requireSession(sessionId);
        try {
            UUID gameId = UUID.fromString(skipForwardRequest.getGameId());
            int moves = skipForwardRequest.getMoves();
            mageServer.replaySkipForward(gameId, sessionId, moves);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not skip forward: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage getTable(String requestId, String sessionId, MessageProto.GetTableRequest getTableRequest) {
        requireSession(sessionId);
        try {
            UUID roomId = UUID.fromString(getTableRequest.getRoomId());
            UUID tableId = UUID.fromString(getTableRequest.getTableId());
            TableView tableView = mageServer.roomGetTableById(roomId, tableId);
            if (tableView == null) {
                return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Table not found");
            }
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setTableViewResponse(MessageProto.TableViewResponse.newBuilder()
                            .setTableView(tableView.toProto())
                            .build())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not get table: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage getTournament(String requestId, String sessionId, MessageProto.GetTournamentRequest getTournamentRequest) {
        requireSession(sessionId);
        try {
            UUID tournamentId = UUID.fromString(getTournamentRequest.getTournamentId());
            TournamentView tournamentView = mageServer.tournamentFindById(tournamentId);
            if (tournamentView == null) {
                return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Tournament not found");
            }
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setTournamentViewResponse(MessageProto.TournamentViewResponse.newBuilder()
                            .setTournamentView(tournamentView.toProto())
                            .build())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not get tournament: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage watchTable(String requestId, String sessionId, MessageProto.WatchTableRequest watchTableRequest) {
        requireSession(sessionId);
        try {
            UUID roomId = UUID.fromString(watchTableRequest.getRoomId());
            UUID tableId = UUID.fromString(watchTableRequest.getTableId());
            boolean result = mageServer.roomWatchTable(sessionId, roomId, tableId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setBoolean(result)
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not watch table: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage watchTournamentTable(String requestId, String sessionId, MessageProto.WatchTournamentTableRequest watchTournamentTableRequest) {
        requireSession(sessionId);
        try {
            UUID tableId = UUID.fromString(watchTournamentTableRequest.getTableId());
            boolean result = mageServer.roomWatchTournament(sessionId, tableId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setBoolean(result)
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not watch tournament table: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage joinTournamentTable(String requestId, String sessionId, MessageProto.JoinTournamentTableRequest joinTournamentTableRequest) {
        requireSession(sessionId);
        try {
            UUID roomId = UUID.fromString(joinTournamentTableRequest.getRoomId());
            UUID tableId = UUID.fromString(joinTournamentTableRequest.getTableId());
            String playerName = joinTournamentTableRequest.getPlayerName();
            PlayerType playerType = PlayerType.getByDescription(joinTournamentTableRequest.getPlayerType());
            int skill = joinTournamentTableRequest.getAiSkill();
            DeckCardLists deckCardLists = DeckCardLists.fromProto(joinTournamentTableRequest.getDeckCardLists());
            String password = joinTournamentTableRequest.getPassword();

            boolean result = mageServer.roomJoinTournament(sessionId, roomId, tableId, playerName, playerType, skill, deckCardLists, password);
            if (!result) {
                return error(requestId, sessionId, MessageProto.ErrorCode.SERVER_ERROR, "Could not join tournament table");
            }
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not join tournament table: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage adminDisconnectUser(String requestId, String sessionId, MessageProto.AdminDisconnectUserRequest adminDisconnectUserRequest) {
        requireSession(sessionId);
        try {
            String userSessionId = adminDisconnectUserRequest.getUserSessionId();
            mageServer.adminDisconnectUser(sessionId, userSessionId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not disconnect user: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage adminEndUserSession(String requestId, String sessionId, MessageProto.AdminEndUserSessionRequest adminEndUserSessionRequest) {
        requireSession(sessionId);
        try {
            String userSessionId = adminEndUserSessionRequest.getUserSessionId();
            mageServer.adminEndUserSession(sessionId, userSessionId);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not end user session: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage adminMuteUser(String requestId, String sessionId, MessageProto.AdminMuteUserRequest adminMuteUserRequest) {
        requireSession(sessionId);
        try {
            String userName = adminMuteUserRequest.getUserName();
            long durationMinutes = adminMuteUserRequest.getDurationMinutes();
            mageServer.adminMuteUser(sessionId, userName, durationMinutes);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not mute user: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage adminActivateUser(String requestId, String sessionId, MessageProto.AdminActivateUserRequest adminActivateUserRequest) {
        requireSession(sessionId);
        try {
            String userName = adminActivateUserRequest.getUserName();
            boolean active = adminActivateUserRequest.getActive();
            mageServer.adminActivateUser(sessionId, userName, active);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not activate user: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage adminToggleActivateUser(String requestId, String sessionId, MessageProto.AdminToggleActivateUserRequest adminToggleActivateUserRequest) {
        requireSession(sessionId);
        try {
            String userName = adminToggleActivateUserRequest.getUserName();
            mageServer.adminToggleActivateUser(sessionId, userName);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not toggle activate user: " + e.getMessage());
        }
    }

    private MessageProto.ServerMessage adminLockUser(String requestId, String sessionId, MessageProto.AdminLockUserRequest adminLockUserRequest) {
        requireSession(sessionId);
        try {
            String userName = adminLockUserRequest.getUserName();
            long durationMinutes = adminLockUserRequest.getDurationMinutes();
            mageServer.adminLockUser(sessionId, userName, durationMinutes);
            return MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(requestId)
                    .setSessionId(sessionId)
                    .setAck(MessageProto.Ack.getDefaultInstance())
                    .build();
        } catch (MageException e) {
            return error(requestId, sessionId, MessageProto.ErrorCode.MAGE_EXCEPTION, "Could not lock user: " + e.getMessage());
        }
    }

    private static MessageProto.ServerMessage error(String requestId, String sessionId, MessageProto.ErrorCode code, String message) {
        return MessageProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId == null ? "" : requestId)
                .setSessionId(sessionId == null ? "" : sessionId)
                .setError(MessageProto.Error.newBuilder()
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
