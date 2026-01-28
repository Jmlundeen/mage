
package mage.view;

import mage.game.Game;
import mage.game.tournament.TournamentPairing;
import mage.util.DateFormat;
import mage.ws.v1.view.ViewProto;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author BetaSteward_at_googlemail.com
 */

public class TournamentGameView implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int roundNum;
    private final UUID matchId;
    private final UUID gameId;
    private final String state;
    private final String result;
    private final String players;
    private final UUID tableId;

    TournamentGameView(int roundNum, UUID matchId, UUID gameId, String state, String result, String players, UUID tableId) {
        this.roundNum = roundNum;
        this.matchId = matchId;
        this.gameId = gameId;
        this.state = state;
        this.result = result;
        this.players = players;
        this.tableId = tableId;
    }

    TournamentGameView(int roundNum, TournamentPairing pair, Game game) {
        this.roundNum = roundNum;
        this.matchId = pair.getMatch().getId();
        this.gameId = game.getId();
        this.players = pair.getPlayer1().getPlayer().getName() + " - " + pair.getPlayer2().getPlayer().getName();
        String duelingTime = "";
        
        if (game.hasEnded()) {
            if (game.getEndTime() != null && game.getStartTime() != null) {
                duelingTime = " (" + DateFormat.getDuration((game.getEndTime().getTime() - game.getStartTime().getTime())/1000) + ')';
            }
            this.state = "Finished" + duelingTime;
            this.result = game.getWinner();
        } 
        else {
            if (game.getStartTime() != null) {
                duelingTime = " (" + DateFormat.getDuration((new Date().getTime() - game.getStartTime().getTime())/1000) + ')';
            }
            this.state = "Dueling" + duelingTime;            
            this.result = "";
        }
        this.tableId = pair.getTableId();
        
    }

    public int getRoundNum() {
        return roundNum;
    }

    public UUID getMatchId() {
        return this.matchId;
    }

    public UUID getGameId() {
        return this.gameId;
    }

    public String getState() {
        return this.state;
    }

    public String getResult() {
        return this.result;
    }

    public String getPlayers() {
        return this.players;
    }

    public UUID getTableId() {
        return tableId;
    }

    public ViewProto.TournamentGameView toProto() {
        return ViewProto.TournamentGameView.newBuilder()
                .setRoundNum(roundNum)
                .setMatchId(matchId != null ? matchId.toString() : "")
                .setGameId(gameId != null ? gameId.toString() : "")
                .setState(state != null ? state : "")
                .setResult(result != null ? result : "")
                .setPlayers(players != null ? players : "")
                .setTableId(tableId != null ? tableId.toString() : "")
                .build();
    }

    public static TournamentGameView fromProto(ViewProto.TournamentGameView proto) {
        return new TournamentGameView(proto);
    }

    // Private constructor for fromProto
    private TournamentGameView(ViewProto.TournamentGameView proto) {
        this.roundNum = proto.getRoundNum();
        this.matchId = proto.getMatchId().isEmpty() ? null : UUID.fromString(proto.getMatchId());
        this.gameId = proto.getGameId().isEmpty() ? null : UUID.fromString(proto.getGameId());
        this.state = proto.getState();
        this.result = proto.getResult();
        this.players = proto.getPlayers();
        this.tableId = proto.getTableId().isEmpty() ? null : UUID.fromString(proto.getTableId());
    }

}
