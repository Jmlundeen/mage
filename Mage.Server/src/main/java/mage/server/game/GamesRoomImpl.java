package mage.server.game;

import mage.MageException;
import mage.cards.decks.DeckCardLists;
import mage.constants.TableState;
import mage.game.GameException;
import mage.game.Table;
import mage.game.match.MatchOptions;
import mage.game.tournament.TournamentOptions;
import mage.players.PlayerType;
import mage.server.RoomImpl;
import mage.server.User;
import mage.server.managers.ManagerFactory;
import mage.server.ws.LobbyBroadcaster;
import mage.util.ThreadUtils;
import mage.util.XmageThreadFactory;
import mage.ws.v1.view.ViewProto;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author BetaSteward_at_googlemail.com, JayDi85
 */
public class GamesRoomImpl extends RoomImpl implements GamesRoom, Serializable {

    private static final Logger LOGGER = Logger.getLogger(GamesRoomImpl.class);

    private static final int MAX_FINISHED_TABLES = 25;
    private static final long FORCE_UPDATE_INTERVAL = TimeUnit.MINUTES.toMillis(5);

    // server's lobby
    private static List<ViewProto.TableView> lobbyTables = new ArrayList<>();
    private static List<ViewProto.MatchView> lobbyMatches = new ArrayList<>();
    private static ViewProto.RoomUsersView lobbyUsers = null;
    private static final ScheduledExecutorService UPDATE_LOBBY_EXECUTOR = Executors.newSingleThreadScheduledExecutor(
            new XmageThreadFactory(ThreadUtils.THREAD_PREFIX_SERVICE_LOBBY_REFRESH)
    );

    // Previous state for delta detection
    private static int previousTablesCount = 0;
    private static int previousMatchesCount = 0;
    private static int previousUsersCount = 0;
    private static long lastForceUpdateTime = System.currentTimeMillis();

    private final ManagerFactory managerFactory;
    private final ConcurrentHashMap<UUID, Table> tables = new ConcurrentHashMap<>();

    public GamesRoomImpl(ManagerFactory managerFactory) {
        super(managerFactory.chatManager());
        this.managerFactory = managerFactory;

        // update lobby's data
        UPDATE_LOBBY_EXECUTOR.scheduleAtFixedRate(() -> {
            try {
                updateLobby();
            } catch (Exception e) {
                LOGGER.fatal("Games room update error: " + e.getMessage(), e);
            }

        }, 2, 2, TimeUnit.SECONDS); // TODO: is it ok for performance?
    }

    @Override
    public List<ViewProto.TableView> getTables() {
        return lobbyTables;
    }

    private void updateLobby() {
        // tables and matches
        List<Table> allTables = new ArrayList<>(tables.values());
        allTables.sort(new TableListSorter());
        List<ViewProto.MatchView> matchList = new ArrayList<>();
        List<ViewProto.TableView> tableList = new ArrayList<>();
        for (Table table : allTables) {
            if (table.getState() != TableState.FINISHED) {
                tableList.add(table.toProtoView());
            } else if (matchList.size() < MAX_FINISHED_TABLES) {
                matchList.add(table.getMatchView());
            } else {
                // more since 50 matches finished since this match so removeUserFromAllTablesAndChat it
                if (table.isTournament()) {
                    managerFactory.tournamentManager().removeTournament(table.getTournament().getId());
                }
                this.removeTable(table.getId());
            }
        }
        lobbyTables = tableList;
        lobbyMatches = matchList;

        // users
        List<ViewProto.UsersView> users = new ArrayList<>();
        for (User user : managerFactory.userManager().getUsers()) {
            if (user.isOnlineUser()) {
                try {
                    users.add(user.toUsersProto());
                } catch (Exception ex) {
                    LOGGER.fatal("User update exception: " + user.getName() + " - " + ex.toString(), ex);
                    users.add(user.toUsersProto());
                }
            }
        }
        users.sort((one, two) -> one.getUserName().compareToIgnoreCase(two.getUserName()));
        lobbyUsers = ViewProto.RoomUsersView.newBuilder()
                .addAllUsersView(users)
                .setNumberActiveGames(managerFactory.gameManager().getNumberActiveGames())
                .setNumberGameThreads(managerFactory.threadExecutor().getActiveThreads(managerFactory.threadExecutor().getGameExecutor()))
                .setNumberMaxGames(managerFactory.configSettings().getMaxGameThreads())
                .build();

        // Check if lobby state has changed using hash comparison
        int currentTablesCount = tableList.size();
        int currentMatchesCount = matchList.size();
        int currentUsersCount = users.size();

        boolean hasChanges = (currentTablesCount != previousTablesCount)
                          || (currentMatchesCount != previousMatchesCount)
                          || (currentUsersCount != previousUsersCount);

        if (hasChanges || (System.currentTimeMillis() - lastForceUpdateTime) >= FORCE_UPDATE_INTERVAL) {
            // Update hash values
            previousTablesCount = currentTablesCount;
            previousMatchesCount = currentMatchesCount;
            previousUsersCount = currentUsersCount;
            lastForceUpdateTime = System.currentTimeMillis();

            // Broadcast lobby update to all connected WebSocket clients
            try {
                LobbyBroadcaster.broadcastLobbyUpdate(tableList, lobbyUsers, matchList);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Lobby state changed - broadcast sent: " + tableList.size() + " tables, " +
                               matchList.size() + " matches, " + users.size() + " users");
                }
            } catch (Exception e) {
                LOGGER.error("Failed to broadcast lobby update", e);
            }
        } else {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Lobby state unchanged - skipping broadcast");
            }
        }
    }

    @Override
    public List<ViewProto.MatchView> getFinished() {
        return lobbyMatches;
    }

    @Override
    public boolean joinTable(UUID userId, UUID tableId, String name, PlayerType playerType, int skill, DeckCardLists deckList, String password) throws MageException {
        if (tables.containsKey(tableId)) {
            return managerFactory.tableManager().joinTable(userId, tableId, name, playerType, skill, deckList, password);
        } else {
            return false;
        }
    }

    @Override
    public ViewProto.TableView createTable(UUID userId, MatchOptions options) {
        Table table = managerFactory.tableManager().createTable(this.getRoomId(), userId, options);
        tables.put(table.getId(), table);
        return table.toProtoView();
    }

    @Override
    public boolean joinTournamentTable(UUID userId, UUID tableId, String name, PlayerType playerType, int skill, DeckCardLists deckList, String password) throws GameException {
        if (tables.containsKey(tableId)) {
            return managerFactory.tableManager().joinTournament(userId, tableId, name, playerType, skill, deckList, password);
        } else {
            return false;
        }
    }

    @Override
    public ViewProto.TableView createTournamentTable(UUID userId, TournamentOptions options) {
        Table table = managerFactory.tableManager().createTournamentTable(this.getRoomId(), userId, options);
        tables.put(table.getId(), table);
        return table.toProtoView();
    }

    @Override
    public Optional<ViewProto.TableView> getTable(UUID tableId) {
        if (tables.containsKey(tableId)) {
            Table table = tables.get(tableId);
            if (table != null) {
                return Optional.of(table.toProtoView());
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public void removeTable(UUID userId, UUID tableId) {
        tables.remove(tableId);
    }

    @Override
    public void removeTable(UUID tableId) {
        Table table = tables.get(tableId);
        if (table != null) {
            table.cleanUp();
            tables.remove(tableId);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Table removed: " + tableId);
            }
        }
    }

    @Override
    public void leaveTable(UUID userId, UUID tableId) {
        managerFactory.tableManager().leaveTable(userId, tableId);
    }

    @Override
    public boolean watchTable(UUID userId, UUID tableId) throws MageException {
        return managerFactory.tableManager().watchTable(userId, tableId);
    }

    @Override
    public ViewProto.RoomUsersView getRoomUsersInfo() {
        return lobbyUsers;
    }

}

/**
 * Sorts the tables for table and match view of the client room
 *
 * @author LevelX2
 */
class TableListSorter implements Comparator<Table> {

    @Override
    public int compare(Table one, Table two) {
        if (one.getState() != null && two.getState() != null) {
            if (TableState.SIDEBOARDING != one.getState() && TableState.DUELING != one.getState()) {
                if (one.getState().compareTo(two.getState()) != 0) {
                    return one.getState().compareTo(two.getState());
                }
            } else if (TableState.SIDEBOARDING != two.getState() && TableState.DUELING != two.getState()) {
                if (one.getState().compareTo(two.getState()) != 0) {
                    return one.getState().compareTo(two.getState());
                }
            }
        }
        if (two.getEndTime() != null) {
            if (one.getEndTime() == null) {
                return 1;
            } else {
                return two.getEndTime().compareTo(one.getEndTime());
            }
        } else if (one.getEndTime() != null) {
            return -1;
        }

        if (two.getStartTime() != null) {
            if (one.getStartTime() == null) {
                return 1;
            } else {
                return two.getStartTime().compareTo(one.getStartTime());
            }
        } else if (one.getStartTime() != null) {
            return -1;
        }

        if (two.getCreateTime() != null) {
            if (one.getCreateTime() == null) {
                return 1;
            } else {
                return two.getCreateTime().compareTo(one.getCreateTime());
            }
        } else if (one.getCreateTime() != null) {
            return -1;
        }
        return 0;
    }
}