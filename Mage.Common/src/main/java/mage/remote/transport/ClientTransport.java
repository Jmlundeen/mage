package mage.remote.transport;

import mage.cards.decks.DeckCardLists;
import mage.game.match.MatchOptions;
import mage.game.tournament.TournamentOptions;
import mage.interfaces.ServerState;
import mage.players.PlayerType;
import mage.players.net.UserData;
import mage.remote.Connection;
import mage.utils.MageVersion;
import mage.view.MatchView;
import mage.view.RoomUsersView;
import mage.view.TableView;
import mage.ws.v1.view.ViewProto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Minimal transport abstraction for incremental client migration.
 */
public interface ClientTransport {

    void connect(Connection connection, String sessionId) throws Exception;

    void disconnect(String sessionId);

    boolean isConnected();

    AuthResult auth(String sessionId, String userName, String password) throws Exception;

    boolean ping(String sessionId, String clientTimeMillis) throws Exception;

    List<TableView> lobbyGetTables(String sessionId, UUID roomId) throws Exception;

    List<MatchView> getFinishedMatches(String sessionId, UUID roomId) throws Exception;

    RoomUsersView getRoomUsers(String sessionId, UUID roomId) throws Exception;

    GetLobbyInfoResult lobbyGetInfo(String sessionId, UUID roomId, boolean includeFinishedMatches, boolean includeRoomUsers) throws Exception;

    UUID getMainRoomId(String sessionId) throws Exception;

    boolean joinChat(String sessionId, UUID chatId);

    boolean leaveChat(String sessionId, UUID chatId);

    ServerState getServerState(String sessionId) throws Exception;

    void sendUserData(String sessionId, UserData userData, MageVersion clientVersion, String userIdStr) throws Exception;

    List<String> getServerMessages(String sessionId) throws Exception;

    UUID getRoomChatId(String sessionId, UUID roomId) throws Exception;

    TableView createTable(String sessionId, UUID roomId, MatchOptions matchOptions);

    TableView createTournamentTable(String sessionId, UUID roomId, TournamentOptions tournamentOptions);

    Boolean joinTable(String sessionId, UUID roomId, UUID tableId, String playerName, PlayerType playerType, int skill, DeckCardLists deckList, String password);

    Boolean removeTable(String sessionId, UUID roomId, UUID tableId);

    Optional<UUID> getTableChatId(String sessionId, UUID tableId);

    Optional<TableView> getTable(String sessionId, UUID roomId, UUID tableId);

    ViewProto.TournamentView getTournament(String sessionId, UUID tournamentId);

    boolean isTableOwner(String sessionId, UUID roomId, UUID tableId);

    boolean watchTable(String sessionId, UUID roomId, UUID tableId);

    boolean watchTournamentTable(String sessionId, UUID tableId);

    Boolean joinTournamentTable(String sessionId, UUID roomId, UUID tableId, String playerName, PlayerType playerType, int skill, DeckCardLists deckList, String password);
}
