
package mage.game;

import mage.cards.decks.DeckCardInfo;
import mage.cards.decks.DeckValidator;
import mage.collectors.DataCollectorServices;
import mage.constants.MatchBufferTime;
import mage.constants.TableState;
import mage.game.draft.Draft;
import mage.game.draft.DraftOptions;
import mage.game.events.Listener;
import mage.game.events.TableEvent;
import mage.game.events.TableEventSource;
import mage.game.match.Match;
import mage.game.match.MatchOptions;
import mage.game.match.MatchPlayer;
import mage.game.mulligan.MulliganType;
import mage.game.result.ResultProtos.TableProto;
import mage.game.tournament.Tournament;
import mage.game.tournament.TournamentPlayer;
import mage.players.Player;
import mage.players.PlayerType;
import mage.ws.v1.view.ViewProto;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author BetaSteward_at_googlemail.com, JayDi85
 */
public class Table implements Serializable {

    private final static AtomicInteger GLOBAL_INDEX = new AtomicInteger();

    private final UUID tableId;
    private final Integer tableIndex; // for better logs and history
    private final UUID roomId;
    private final String name;
    private final String controllerName;
    private final String gameType;
    private final Date createTime;
    private Seat[] seats;
    private final int numSeats;
    private boolean isTournament;

    private boolean tournamentSubTable; // must assign by setTournamentSubTable only
    private UUID parentTableId = null; // original tourney table

    private DeckValidator validator;
    private TableState state;
    private Match match;
    private Tournament tournament;
    private final TableRecorder recorder;
    private final Set<String> bannedUsernames;
    private final boolean isPlaneChase;

    @FunctionalInterface
    public interface TableRecorder {

        void record(Table table);
    }

    protected TableEventSource tableEventSource = new TableEventSource();

    public Table(UUID roomId, String gameType, String name, String controllerName, DeckValidator validator, List<PlayerType> playerTypes, TableRecorder recorder, Tournament tournament, Set<String> bannedUsernames, boolean isPlaneChase) {
        this(roomId, gameType, name, controllerName, validator, playerTypes, recorder, bannedUsernames, isPlaneChase);
        this.tournament = tournament;
        this.isTournament = true;
        setState(TableState.WAITING);

        DataCollectorServices.getInstance().onTableStart(this);
    }

    public Table(UUID roomId, String gameType, String name, String controllerName, DeckValidator validator, List<PlayerType> playerTypes, TableRecorder recorder, Match match, Set<String> bannedUsernames, boolean isPlaneChase) {
        this(roomId, gameType, name, controllerName, validator, playerTypes, recorder, bannedUsernames, isPlaneChase);
        this.match = match;
        this.match.setTableId(this.getId());
        this.isTournament = false;
        setState(TableState.WAITING);

        DataCollectorServices.getInstance().onTableStart(this);
    }

    protected Table(UUID roomId, String gameType, String name, String controllerName, DeckValidator validator, List<PlayerType> playerTypes, TableRecorder recorder, Set<String> bannedUsernames, boolean isPlaneChase) {
        this.tableId = UUID.randomUUID();
        this.tableIndex = GLOBAL_INDEX.incrementAndGet();
        this.roomId = roomId;
        this.numSeats = playerTypes.size();
        this.gameType = gameType;
        this.name = name;
        this.controllerName = controllerName;
        this.createTime = new Date();
        createSeats(playerTypes);
        this.validator = validator;
        this.recorder = recorder;
        this.bannedUsernames = new HashSet<>(bannedUsernames);
        this.isPlaneChase = isPlaneChase;
    }

    private void createSeats(List<PlayerType> playerTypes) {
        int i = 0;
        seats = new Seat[numSeats];
        for (PlayerType playerType : playerTypes) {
            seats[i] = new Seat(playerType);
            i++;
        }
    }

    public UUID getId() {
        return tableId;
    }

    public Integer getTableIndex() {
        return tableIndex;
    }

    public UUID getParentTableId() {
        return parentTableId;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void initGame() {
        setState(TableState.DUELING);
    }

    public void initTournament() {
        setState(TableState.DUELING);
        tournament.setStepStartTime(new Date());
    }

    public void endTournament() {
        setState(TableState.FINISHED);
    }

    public void initDraft(Draft draft) {
        setState(TableState.DRAFTING);
        tournament.setStepStartTime(new Date());
        draft.setTableId(this.getId());
    }

    public void construct() {
        setState(TableState.CONSTRUCTING);
        tournament.setStepStartTime(new Date());
    }

    /**
     * All activities of the table end (only replay of games (if active) and
     * display tournament results)
     */
    public void closeTable() {
        if (getState() != TableState.WAITING && getState() != TableState.READY_TO_START) {
            setState(TableState.FINISHED); // otherwise the table can be removed completely
        }
        this.validator = null;

        DataCollectorServices.getInstance().onTableEnd(this);
    }

    /**
     * Complete remove of the table, release all objects
     */
    public void cleanUp() {
        if (match != null) {
            match.cleanUpOnMatchEnd(false, false);
        }
    }

    public String getGameType() {
        return gameType;
    }

    public String getDeckType() {
        if (validator != null) {
            return validator.getName();
        }
        return "<deck type missing>";
    }

    public Date getCreateTime() {
        return new Date(createTime.getTime());
    }

    public boolean isTournament() {
        return this.isTournament;
    }

    public UUID joinTable(Player player, Seat seat) throws GameException {
        if (seat.getPlayer() != null) {
            throw new GameException("Seat is occupied.");
        }
        seat.setPlayer(player);
        if (isReady()) {
            setState(TableState.READY_TO_START);
        }
        return seat.getPlayer().getId();
    }

    private boolean isReady() {
        for (int i = 0; i < numSeats; i++) {
            if (seats[i].getPlayer() == null) {
                return false;
            }
        }
        return true;
    }

    public Seat[] getSeats() {
        return seats;
    }

    public int getNumberOfSeats() {
        return numSeats;
    }

    public Seat getNextAvailableSeat(PlayerType playerType) {
        for (int i = 0; i < numSeats; i++) {
            if (seats[i].getPlayer() == null && seats[i].getPlayerType() == (playerType)) {
                return seats[i];
            }
        }
        return null;
    }

    public boolean allSeatsAreOccupied() {
        for (int i = 0; i < numSeats; i++) {
            if (seats[i].getPlayer() == null) {
                return false;
            }
        }
        return true;
    }

    public void leaveNotStartedTable(UUID playerId) {
        for (int i = 0; i < numSeats; i++) {
            Player player = seats[i].getPlayer();
            if (player != null && player.getId().equals(playerId)) {
                seats[i].setPlayer(null);
                if (getState() == TableState.READY_TO_START) {
                    setState(TableState.WAITING);
                }
                break;
            }
        }
    }

    final public void setState(TableState state) {
        this.state = state;
        if (isTournament()) {
            getTournament().setTournamentState(state.toString());
        }
        if (state == TableState.FINISHED) {
            this.recorder.record(this);
        }
    }

    public TableState getState() {
        return state;
    }

    public DeckValidator getValidator() {
        return this.validator;
    }

    public void sideboard() {
        setState(TableState.SIDEBOARDING);
    }

    public String getName() {
        return this.name;
    }

    public void addTableEventListener(Listener<TableEvent> listener) {
        tableEventSource.addListener(listener);
    }

    public Match getMatch() {
        return match;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public String getControllerName() {
        return controllerName;
    }

    public boolean isTournamentSubTable() {
        return tournamentSubTable;
    }

    public void setTournamentSubTable(UUID parentTableId) {
        this.tournamentSubTable = true;
        this.parentTableId = parentTableId;
    }

    public Date getStartTime() {
        if (isTournament) {
            return tournament.getStartTime();
        } else {
            return match.getStartTime();
        }
    }

    public Date getEndTime() {
        if (isTournament) {
            return tournament.getEndTime();
        } else {
            return match.getEndTime();
        }
    }

    public boolean userIsBanned(String username) {
        return bannedUsernames.contains(username);
    }

    public TableProto toProto() {
        TableProto.Builder builder = TableProto.newBuilder();
        if (this.isTournament()) {
            builder.getTourneyBuilder().mergeFrom(this.getTournament().toProto());
        } else {
            builder.getMatchBuilder().mergeFrom(this.getMatch().toProto());
        }
        return builder.setGameType(this.getGameType())
                .setName(this.getName())
                .setGameType(this.getGameType())
                .setDeckType(this.getDeckType())
                .setControllerName(this.getControllerName())
                .setStartTimeMs(this.getStartTime() != null ? this.getStartTime().getTime() : 0L)
                .setEndTimeMs(this.getEndTime() != null ? this.getEndTime().getTime() : 0L)
                .build();
    }

    public ViewProto.TableView toProtoView() {
        ViewProto.TableView.Builder tableView = ViewProto.TableView.newBuilder();
        tableView.setTableId(this.getId().toString());
        tableView.setGameType(this.getGameType());
        tableView.setTableName(this.getName());
        tableView.setControllerName(this.getControllerName());

        tableView.setTableState(this.getState().toProto());
        tableView.setIsTournament(this.isTournament());

        if (this.getState() == TableState.WAITING
                || this.getState() == TableState.READY_TO_START
                || this.getState() == TableState.STARTING) {
            tableView.setCreateTimeMillis(this.getCreateTime() != null ? this.getCreateTime().getTime() : 0L);
        } else {
            if (this.isTournament()) {
                tableView.setCreateTimeMillis(this.getTournament() != null && this.getTournament().getStartTime() != null
                        ? this.getTournament().getStartTime().getTime() : 0L);
            } else {
                tableView.setCreateTimeMillis(this.getMatch() != null && this.getMatch().getStartTime() != null
                        ? this.getMatch().getStartTime().getTime() : 0L);
            }
        }

        if (!this.isTournament()) {
            // MATCH
            String seatsInfo = "" + this.getMatch().getPlayers().size() + '/' + this.getSeats().length;
            tableView.setSeatsInfo(seatsInfo);

            if (this.getState() == TableState.WAITING || this.getState() == TableState.READY_TO_START) {
                tableView.setTableStateText(this.getState().toString() + " (" + seatsInfo + ')');
            } else {
                tableView.setTableStateText(this.getState().toString());
            }

            for (Game game : this.getMatch().getGames()) {
                tableView.addGames(game.getId().toString());
            }

            StringBuilder sb = new StringBuilder();
            StringBuilder sbScore = new StringBuilder();
            for (MatchPlayer matchPlayer : this.getMatch().getPlayers()) {
                if (matchPlayer.getPlayer() == null) {
                    sb.append(", ").append("[unknown]");
                    sbScore.append('-').append(matchPlayer.getWins());
                } else if (!matchPlayer.getName().equals(this.getControllerName())) {
                    sb.append(", ").append(matchPlayer.getName());
                    sbScore.append('-').append(matchPlayer.getWins());
                } else {
                    sbScore.insert(0, matchPlayer.getWins()).insert(0, " Score: ");
                }
            }
            if (this.getMatch().getDraws() > 0) {
                sbScore.append(" Draws: ").append(this.getMatch().getDraws());
            }
            tableView.setControllerName(this.getControllerName() + sb);

            tableView.setDeckType(this.getDeckType());

            StringBuilder infoTextShort = new StringBuilder();
            StringBuilder infoTextLong = new StringBuilder();
            if (this.getMatch().getGames().isEmpty()) {
                infoTextShort.append("Wins: ").append(this.getMatch().getWinsNeeded());
                infoTextLong.append("Wins required: ").append(this.getMatch().getWinsNeeded())
                        .append(" (Best of ").append(this.getMatch().getWinsNeeded() * 2 - 1).append(")");
                buildMatchOptionsTextShared(this.getMatch().getOptions(), infoTextShort, infoTextLong);
            } else {
                infoTextShort.append("Wins: ").append(this.getMatch().getWinsNeeded()).append(sbScore);
                infoTextLong.append("Wins required: ").append(this.getMatch().getWinsNeeded()).append(sbScore);
            }
            infoTextLong.append("<br>Seats: ").append(seatsInfo);
            if (this.getMatch().getOptions().isSpectatorsAllowed()) {
                infoTextShort.append(", SP");
                infoTextLong.append("<br>Spectators allowed (SP)");
            }
            infoTextLong.append("<br>Game type: ").append(this.getGameType());
            infoTextLong.append("<br>Deck type: ").append(this.getDeckType());
            if (this.getNumberOfSeats() > 3) {
                infoTextShort.append(", Rng: ").append(this.getMatch().getOptions().getRange().toString());
                infoTextLong.append("<br>Range of Influence: ").append(this.getMatch().getOptions().getRange().toString());
            }

            tableView.setAdditionalInfoShort(infoTextShort.toString());
            tableView.setAdditionalInfoFull(infoTextLong.toString());

            tableView.setSkillLevel(this.getMatch().getOptions().getSkillLevel().toProto());
            tableView.setQuitRatio(Integer.toString(this.getMatch().getOptions().getQuitRatio()));
            tableView.setMinimumRating(Integer.toString(this.getMatch().getOptions().getMinimumRating()));
            tableView.setRated(this.getMatch().getOptions().isRated());
            tableView.setIsPasswordProtected(!this.getMatch().getOptions().getPassword().isEmpty());
            tableView.setSpectatorsAllowed(this.getMatch().getOptions().isSpectatorsAllowed());
        } else {
            // TOURNAMENT
            String gameType = tableView.getGameType();
            if (this.getTournament().getOptions().getNumberRounds() > 0) {
                gameType = gameType + ' ' + this.getTournament().getOptions().getNumberRounds() + " Rounds";
            } else if (this.getTournament().getOptions().getMatchOptions().isSingleGameTourney()) {
                gameType = gameType + " Single Game";
            }
            tableView.setGameType(gameType);

            StringBuilder sb1 = new StringBuilder();
            for (TournamentPlayer tp : this.getTournament().getPlayers()) {
                if (!tp.getPlayer().getName().equals(this.getControllerName())) {
                    sb1.append(", ").append(tp.getPlayer().getName());
                }
            }
            tableView.setControllerName(this.getControllerName() + sb1);

            String seatsInfo = this.getTournament().getPlayers().size() + "/" + this.getNumberOfSeats();
            tableView.setSeatsInfo(seatsInfo);

            for (Seat seat : this.getSeats()) {
                tableView.addSeats(seat.toProto());
            }

            StringBuilder infoTextShort = new StringBuilder();
            StringBuilder infoTextLong = new StringBuilder();
            StringBuilder stateText = new StringBuilder(this.getState().toString());

            infoTextShort.append("Wins: ").append(this.getTournament().getOptions().getMatchOptions().getWinsNeeded());
            infoTextLong.append("Wins required: ").append(this.getTournament().getOptions().getMatchOptions().getWinsNeeded())
                    .append(" (Best of ").append(this.getTournament().getOptions().getMatchOptions().getWinsNeeded() * 2 - 1).append(")");
            infoTextLong.append("<br>Seats: ").append(seatsInfo);

            switch (this.getState()) {
                case WAITING:
                case READY_TO_START:
                case STARTING:
                    if (TableState.WAITING.equals(this.getState())) {
                        stateText.append(" (").append(this.getTournament().getPlayers().size()).append('/').append(this.getNumberOfSeats()).append(')');
                    }
                    buildMatchOptionsTextShared(this.getTournament().getOptions().getMatchOptions(), infoTextShort, infoTextLong);
                    if (this.getTournament().getTournamentType().isLimited()) {
                        infoTextShort.append(", Constr.: ")
                                .append(this.getTournament().getOptions().getLimitedOptions().getConstructionTime() / 60).append("m");
                        infoTextLong.append("<br>Construction time: ")
                                .append(this.getTournament().getOptions().getLimitedOptions().getConstructionTime() / 60).append(" Minutes");
                    }
                    if (this.getTournament().getOptions().getLimitedOptions() instanceof DraftOptions) {
                        DraftOptions draftOptions = (DraftOptions) this.getTournament().getOptions().getLimitedOptions();
                        infoTextShort.append(", Pick time: ").append(draftOptions.getTiming().getShortName());
                        infoTextLong.append("<br>Pick time: ").append(draftOptions.getTiming().getName());
                    }
                    if (this.getTournament().getOptions().getMatchOptions().isSingleGameTourney()) {
                        infoTextShort.append(", 1 GAME");
                        infoTextLong.append("<br>Single Game with all players (1 GAME)");
                    }
                    if (this.getTournament().getOptions().isWatchingAllowed()) {
                        infoTextShort.append(", SP");
                        infoTextLong.append("<br>Spectators allowed (SP)");
                    }
                    infoTextLong.append("<br>Game type: ").append(this.getGameType());
                    infoTextLong.append("<br>Deck type: ").append(this.getDeckType());
                    if (!this.getTournament().getBoosterInfo().isEmpty()) {
                        infoTextLong.append("<br>Boosters: ").append(this.getTournament().getBoosterInfo());
                    }
                    break;
                case DUELING:
                    stateText.append(" Round: ").append(this.getTournament().getRounds().size());
                    break;
                case DRAFTING:
                    Draft draft = this.getTournament().getDraft();
                    if (draft != null) {
                        stateText.append(' ').append(draft.getBoosterNum()).append('/').append(draft.getCardNum());
                    }
                    break;
                default:
            }

            tableView.setAdditionalInfoShort(infoTextShort.toString());
            tableView.setAdditionalInfoFull(infoTextLong.toString());
            tableView.setTableStateText(stateText.toString());

            tableView.setDeckType(this.getDeckType() + ' ' + this.getTournament().getBoosterInfo());
            tableView.setSkillLevel(this.getTournament().getOptions().getMatchOptions().getSkillLevel().toProto());
            tableView.setQuitRatio(Integer.toString(this.getTournament().getOptions().getQuitRatio()));
            tableView.setMinimumRating(Integer.toString(this.getTournament().getOptions().getMinimumRating()));
            tableView.setRated(this.getTournament().getOptions().getMatchOptions().isRated());
            tableView.setIsPasswordProtected(!this.getTournament().getOptions().getPassword().isEmpty());
            tableView.setSpectatorsAllowed(this.getTournament().getOptions().isWatchingAllowed());
        }

        return tableView.build();
    }

    private void buildMatchOptionsTextShared(MatchOptions options, StringBuilder shortBuilder, StringBuilder longBuilder) {
        longBuilder.append("<br>Time: ").append(options.getMatchTimeLimit().toString());
        shortBuilder.append(", Time: ").append(options.getMatchTimeLimit().getShortName());
        if (options.getMatchBufferTime() != MatchBufferTime.NONE) {
            shortBuilder.append("(+").append(options.getMatchBufferTime().getShortName()).append(")");
            longBuilder.append("<br>Buffer time: ").append(options.getMatchBufferTime().toString());
        }
        int customOptions = 0;
        if (options.getMulliganType() != MulliganType.GAME_DEFAULT) {
            longBuilder.append("<br>Mulligan: \"").append(options.getMulliganType().toString()).append("\"");
            customOptions += 1;
        }
        if (options.getFreeMulligans() > 0) {
            longBuilder.append("<br>Free Mulligans: ").append(options.getFreeMulligans());
            customOptions += 1;
        }
        if (options.isCustomStartLifeEnabled()) {
            longBuilder.append("<br>Starting Life: ").append(options.getCustomStartLife());
            customOptions += 1;
        }
        if (options.isCustomStartHandSizeEnabled()) {
            longBuilder.append("<br>Starting Hand Size: ").append(options.getCustomStartHandSize());
            customOptions += 1;
        }
        if (options.isPlaneChase()) {
            longBuilder.append("<br>Planechase");
            customOptions += 1;
        }
        if (!(options.getPerPlayerEmblemCards().isEmpty())
                || !(options.getGlobalEmblemCards().isEmpty())) {
            longBuilder.append("<br>Emblem cards:");
            for (DeckCardInfo card : options.getPerPlayerEmblemCards()) {
                longBuilder.append("<br>* <b>").append(card.getCardName()).append("</b> (per player)");
            }
            for (DeckCardInfo card : options.getGlobalEmblemCards()) {
                longBuilder.append("<br>* <b>").append(card.getCardName()).append("</b> (global)");
            }
            customOptions += 1;
        }
        if (customOptions > 0) {
            shortBuilder.append(", Custom options (").append(customOptions).append(")");
        }
        if (options.isRollbackTurnsAllowed()) {
            shortBuilder.append(", RB");
            longBuilder.append("<br>Rollbacks allowed (RB)");
        }
    }

    public ViewProto.MatchView getMatchView() {
        ViewProto.MatchView.Builder matchView = ViewProto.MatchView.newBuilder();
        matchView.setTableId(this.getId().toString());
        matchView.setIsTournament(this.isTournament());

        if (this.isTournament()) {
            initTournamentTable(matchView, this);
        } else {
            initMatchTable(matchView, this);
        }
        return matchView.build();
    }

    private void initTournamentTable(ViewProto.MatchView.Builder matchView, Table table) {
        matchView.setMatchId(table.getTournament().getId().toString());
        matchView.setMatchName(table.getName());
        String gameType = table.getGameType();
        if (table.getTournament().getOptions().getNumberRounds() > 0) {
            gameType = gameType + ' ' + table.getTournament().getOptions().getNumberRounds() + " Rounds";
        }
        matchView.setGameType(gameType);
        StringBuilder sbDeckType = new StringBuilder(table.getDeckType());
        if (!table.getTournament().getBoosterInfo().isEmpty()) {
            sbDeckType.append(' ').append(table.getTournament().getBoosterInfo());
        }
        if (table.getName() != null && !table.getName().isEmpty()) {
            sbDeckType.append(table.getDeckType()).append(" [").append(table.getName()).append(']');
        }
        matchView.setDeckType(sbDeckType.toString());
        StringBuilder sb1 = new StringBuilder();
        for (TournamentPlayer tPlayer : table.getTournament().getPlayers()) {
            sb1.append(tPlayer.getPlayer().getName()).append(" (").append(tPlayer.getPoints()).append(" P.) ");
        }
        matchView.setPlayers(sb1.toString());
        StringBuilder sb2 = new StringBuilder();
        if (!table.getTournament().getRounds().isEmpty()) {
            for (TournamentPlayer tPlayer : table.getTournament().getPlayers()) {
                sb2.append(tPlayer.getPlayer().getName()).append(": ").append(tPlayer.getResults()).append(' ');
            }
        } else if (table.getTournament().getOptions().getMatchOptions().isSingleGameTourney()) {
            sb2.append("Started single game");
        } else {
            sb2.append("Canceled");
        }
        matchView.setResult(sb2.toString());
        matchView.setStartTimeMillis(table.getTournament().getStartTime().getTime());
        matchView.setEndTimeMillis(table.getTournament().getEndTime().getTime());
        matchView.setRated(table.getTournament().getOptions().getMatchOptions().isRated());
        matchView.setReplayAvailable(false);
    }

    private void initMatchTable(ViewProto.MatchView.Builder matchView, Table table) {
        Match match = table.getMatch();
        matchView.setMatchId(match.getId().toString());
        matchView.setMatchName(match.getName());
        matchView.setGameType(match.getOptions().getGameType());

        if (table.getName() != null && !table.getName().isEmpty()) {
            matchView.setDeckType(match.getOptions().getDeckType() + "[" + table.getName() + "]");
        } else {
            matchView.setDeckType(match.getOptions().getDeckType());
        }

        for (Game game : match.getGames()) {
            matchView.addGames(game.getId().toString());
        }
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        for (MatchPlayer matchPlayer: match.getPlayers()) {
            sb1.append(matchPlayer.getName());
            if(matchPlayer.hasQuit()) {
                if (matchPlayer.getPlayer().hasTimerTimeout()) {
                    sb1.append(" [timer] ");
                } else if (matchPlayer.getPlayer().hasIdleTimeout()) {
                    sb1.append(" [idle] ");
                } else {
                    sb1.append(" [quit] ");
                }
            }
            int lostGames = match.getNumGames() - (matchPlayer.getWins() + match.getDraws());
            sb1.append(", ");
            sb2.append(matchPlayer.getName()).append(" [");
            sb2.append(matchPlayer.getWins()).append('-');
            if (match.getDraws() > 0) {
                sb2.append(match.getDraws()).append('-');
            }
            sb2.append(lostGames).append("], ");
        }
        if (sb1.length() > 2) {
            matchView.setPlayers(sb1.substring(0, sb1.length() - 2));
            matchView.setResult(sb2.substring(0, sb2.length() - 2));
        } else {
            matchView.setPlayers("[no players]");
            matchView.setResult("");
        }
        matchView.setStartTimeMillis(match.getStartTime().getTime());
        matchView.setEndTimeMillis(match.getEndTime().getTime());
        matchView.setReplayAvailable(match.isReplayAvailable());
        matchView.setRated(match.getOptions().isRated());
    }
}
