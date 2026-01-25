package mage.remote.transport;

import mage.interfaces.ServerState;
import mage.players.net.UserData;
import mage.remote.Connection;
import mage.utils.MageVersion;
import mage.ws.v1.view.ViewProto;

import java.util.List;
import java.util.UUID;

/**
 * Minimal transport abstraction for incremental client migration.
 */
public interface ClientTransport {

    void connect(Connection connection) throws Exception;

    void disconnect();

    boolean isConnected();

    AuthResult auth(String sessionId, String userName, String password) throws Exception;

    boolean ping(String sessionId, String clientTimeMillis) throws Exception;

    List<ViewProto.TableView> lobbyGetTables(String sessionId, UUID roomId) throws Exception;

    List<ViewProto.MatchView> getFinishedMatches(String sessionId, UUID roomId) throws Exception;

    ViewProto.RoomUsersView getRoomUsers(String sessionId, UUID roomId) throws Exception;

    GetLobbyInfoResult lobbyGetInfo(String sessionId, UUID roomId, boolean includeFinishedMatches, boolean includeRoomUsers) throws Exception;

    UUID getMainRoomId(String sessionId) throws Exception;

    boolean joinChat(String sessionId, UUID chatId);

    boolean leaveChat(String sessionId, UUID chatId);

    ServerState getServerState(String sessionId) throws Exception;

    void sendUserData(String sessionId, UserData userData, MageVersion clientVersion, String userIdStr) throws Exception;

    List<String> getServerMessages(String sessionId) throws Exception;

    UUID getRoomChatId(String sessionId, UUID roomId) throws Exception;
}
