
package mage.view;

import mage.game.tournament.TournamentPlayer;
import mage.ws.v1.view.ViewProto;

import java.io.Serializable;

/**
 *
 * @author BetaSteward_at_googlemail.com
 */
public class TournamentPlayerView implements Serializable, Comparable {

    private static final long serialVersionUID = 1L;

    private final String flagName;
    private final String name;
    private final String state;
    private final String results;
    private final String history;
    private final int points;
    private final boolean quit;

    TournamentPlayerView(TournamentPlayer tournamentPlayer) {
        this.name = tournamentPlayer.getPlayer().getName();
        StringBuilder sb = new StringBuilder(tournamentPlayer.getState().toString());
        String stateInfo = tournamentPlayer.getStateInfo();
        if (!stateInfo.isEmpty()) {
            sb.append(" (").append(stateInfo).append(')');
        }
        sb.append(tournamentPlayer.getDisconnectInfo());
        this.state = sb.toString();
        this.points = tournamentPlayer.getPoints();
        this.results = tournamentPlayer.getResults();
        this.quit = !tournamentPlayer.isInTournament();
        this.history = tournamentPlayer.getPlayer().getUserData().getHistory();
        this.flagName = tournamentPlayer.getPlayer().getUserData().getFlagName();
    }

    public String getName() {
        return this.name;
    }

    public String getState() {
        return state;
    }

    public int getPoints() {
        return this.points;
    }

    public String getResults() {
        return results;
    }

    public boolean hasQuit() {
        return quit;
    }

    @Override
    public int compareTo(Object t) {
        return ((TournamentPlayerView) t).getPoints() - this.getPoints();
    }

    public String getFlagName() {
        return flagName;
    }

    public String getHistory() {
        return history;
    }

    public ViewProto.TournamentPlayerView toProto() {
        return ViewProto.TournamentPlayerView.newBuilder()
                .setFlagName(flagName != null ? flagName : "")
                .setName(name != null ? name : "")
                .setState(state != null ? state : "")
                .setResults(results != null ? results : "")
                .setHistory(history != null ? history : "")
                .setPoints(points)
                .setQuit(quit)
                .build();
    }

    public static TournamentPlayerView fromProto(ViewProto.TournamentPlayerView proto) {
        return new TournamentPlayerView(proto);
    }

    // Private constructor for fromProto
    private TournamentPlayerView(ViewProto.TournamentPlayerView proto) {
        this.flagName = proto.getFlagName();
        this.name = proto.getName();
        this.state = proto.getState();
        this.results = proto.getResults();
        this.history = proto.getHistory();
        this.points = proto.getPoints();
        this.quit = proto.getQuit();
    }

}
