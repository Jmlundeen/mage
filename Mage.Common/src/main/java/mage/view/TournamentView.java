package mage.view;

import mage.game.tournament.Round;
import mage.game.tournament.Tournament;
import mage.game.tournament.TournamentPlayer;
import mage.ws.v1.view.ViewProto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 * @author BetaSteward_at_googlemail.com
 */
public class TournamentView implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String tournamentName;
    private final String tournamentType;
    private final String tournamentState;
    
    private final Date startTime;
    private final Date endTime;
    private final Date stepStartTime;
    private final Date serverTime;
    private final int constructionTime;
    private final boolean watchingAllowed;

    private final List<RoundView> rounds = new ArrayList<>();
    private final List<TournamentPlayerView> players = new ArrayList<>();
    private final String runningInfo;
    
    public TournamentView(Tournament tournament) {

        tournamentName = tournament.getOptions().getName();
        StringBuilder typeText = new StringBuilder(tournament.getOptions().getTournamentType());
        if (!tournament.getTournamentType().isLimited()) {
            typeText.append(" / ").append(tournament.getOptions().getMatchOptions().getDeckType());
        }
        if (tournament.getNumberRounds() > 0) {
            typeText.append(' ').append(tournament.getNumberRounds()).append(" rounds");
        } 
        tournamentType = typeText.toString();
        startTime = tournament.getStartTime();
        endTime = tournament.getEndTime();
        stepStartTime = tournament.getStepStartTime();
        constructionTime = tournament.getOptions().getLimitedOptions().getConstructionTime();
        watchingAllowed = tournament.getOptions().isWatchingAllowed();
        serverTime = new Date();
        tournamentState = tournament.getTournamentState();

        if (tournament.getTournamentState().equals("Drafting") && tournament.getDraft() != null) {
            runningInfo = "booster/card: " + tournament.getDraft().getBoosterNum() + '/' + (tournament.getDraft().getCardNum());
        } else if (tournament.getOptions().getMatchOptions().isSingleGameTourney()) {
            runningInfo = "running single game match";
        } else {
            runningInfo = "";
        }
        for (TournamentPlayer player: tournament.getPlayers()) {
            players.add(new TournamentPlayerView(player));
        }
        Collections.sort(players);
        for (Round round: tournament.getRounds()) {
            rounds.add(new RoundView(round));
        }
    }

    public String getTournamentName() {
        return tournamentName;
    }

    public String getTournamentType() {
        return tournamentType;
    }

    public Date getStartTime() {
        return new Date(startTime.getTime());
    }

    public Date getEndTime() {
        if (endTime == null) {
            return null;
        }
        return new Date(endTime.getTime());
    }

    public boolean isWatchingAllowed() {
        return watchingAllowed;
    }

    public List<TournamentPlayerView> getPlayers() {
        return players;
    }

    public List<RoundView> getRounds() {
        return rounds;
    }

    public String getTournamentState() {
        return tournamentState;
    }

    public Date getStepStartTime() {
        return stepStartTime;
    }

    public int getConstructionTime() {
        return constructionTime;
    }

    public Date getServerTime() {
        return serverTime;
    }

    public String getRunningInfo() {
        return runningInfo;
    }

    public ViewProto.TournamentView toProto() {
        ViewProto.TournamentView.Builder builder = ViewProto.TournamentView.newBuilder()
                .setTournamentName(tournamentName)
                .setTournamentType(tournamentType)
                .setTournamentState(tournamentState)
                .setConstructionTime(constructionTime)
                .setWatchingAllowed(watchingAllowed)
                .setRunningInfo(runningInfo);

        if (startTime != null) {
            builder.setStartTimeMillis(startTime.getTime());
        }
        if (endTime != null) {
            builder.setEndTimeMillis(endTime.getTime());
        }
        if (stepStartTime != null) {
            builder.setStepStartTimeMillis(stepStartTime.getTime());
        }
        if (serverTime != null) {
            builder.setServerTimeMillis(serverTime.getTime());
        }

        for (RoundView round : rounds) {
            builder.addRounds(round.toProto());
        }

        for (TournamentPlayerView player : players) {
            builder.addPlayers(player.toProto());
        }

        return builder.build();
    }

    public static TournamentView fromProto(ViewProto.TournamentView proto) {
        // Create a TournamentView from protobuf data (for client-side use)
        return new TournamentView(proto);
    }

    // Private constructor for fromProto
    private TournamentView(ViewProto.TournamentView proto) {
        this.tournamentName = proto.getTournamentName();
        this.tournamentType = proto.getTournamentType();
        this.tournamentState = proto.getTournamentState();
        this.startTime = proto.getStartTimeMillis() > 0 ? new Date(proto.getStartTimeMillis()) : null;
        this.endTime = proto.getEndTimeMillis() > 0 ? new Date(proto.getEndTimeMillis()) : null;
        this.stepStartTime = proto.getStepStartTimeMillis() > 0 ? new Date(proto.getStepStartTimeMillis()) : null;
        this.serverTime = proto.getServerTimeMillis() > 0 ? new Date(proto.getServerTimeMillis()) : null;
        this.constructionTime = proto.getConstructionTime();
        this.watchingAllowed = proto.getWatchingAllowed();
        this.runningInfo = proto.getRunningInfo();

        // Reconstruct players
        for (ViewProto.TournamentPlayerView playerProto : proto.getPlayersList()) {
            this.players.add(TournamentPlayerView.fromProto(playerProto));
        }

        // Reconstruct rounds
        for (ViewProto.RoundView roundProto : proto.getRoundsList()) {
            this.rounds.add(RoundView.fromProto(roundProto));
        }
    }

}
