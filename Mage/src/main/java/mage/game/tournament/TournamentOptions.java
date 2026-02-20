
package mage.game.tournament;

import mage.game.match.MatchOptions;
import mage.players.PlayerType;
import mage.ws.model.ModelProto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author BetaSteward_at_googlemail.com
 */
public class TournamentOptions implements Serializable {

    protected String name;
    protected String tournamentType;
    protected List<PlayerType> playerTypes = new ArrayList<>();
    protected MatchOptions matchOptions;
    protected LimitedOptions limitedOptions;
    protected boolean watchingAllowed = true;
    protected boolean planeChase = false;
    protected int numberRounds;
    protected String password;
    protected int quitRatio;
    protected int minimumRating;

    public TournamentOptions(String name, String matchType, boolean isSingleMultiplayerGame) {
        this.name = name;
        this.matchOptions = new MatchOptions("", matchType, isSingleMultiplayerGame);
    }

    public String getName() {
        return name;
    }

    public String getTournamentType() {
        return tournamentType;
    }

    public void setTournamentType(String tournamentType) {
        this.tournamentType = tournamentType;
    }

    public List<PlayerType> getPlayerTypes() {
        return playerTypes;
    }

    public MatchOptions getMatchOptions() {
        return matchOptions;
    }

    public void setLimitedOptions(LimitedOptions limitedOptions) {
        this.limitedOptions = limitedOptions;
    }

    public LimitedOptions getLimitedOptions() {
        return limitedOptions;
    }

    public boolean isWatchingAllowed() {
        return watchingAllowed;
    }

    public void setWatchingAllowed(boolean watchingAllowed) {
        this.watchingAllowed = watchingAllowed;
    }

    public boolean isPlaneChase() {
        return planeChase;
    }

    public void setPlaneChase(boolean planeChase) {
        this.planeChase = planeChase;
        this.matchOptions.setPlaneChase(planeChase);
    }    

    public int getNumberRounds() {
        return numberRounds;
    }

    public void setNumberRounds(int numberRounds) {
        this.numberRounds = numberRounds;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getQuitRatio() {
        return quitRatio;
    }

    public void setQuitRatio(int quitRatio) {
        this.quitRatio = quitRatio;
    }

    public int getMinimumRating() { return minimumRating; }

    public void setMinimumRating(int minimumRating) { this.minimumRating = minimumRating; }

    public ModelProto.TournamentOptions toProto() {
        ModelProto.TournamentOptions.Builder builder = ModelProto.TournamentOptions.newBuilder()
                .setName(this.name)
                .setTournamentType(this.tournamentType != null ? this.tournamentType : "")
                .setMatchOptionsName(this.matchOptions.getName())
                .setMatchOptionsGameType(this.matchOptions.getGameType())
                .setMatchOptionsMultiPlayer(this.matchOptions.isSingleGameTourney())
                .setWatchingAllowed(this.watchingAllowed)
                .setPlaneChase(this.planeChase)
                .setNumberRounds(this.numberRounds)
                .setPassword(this.password != null ? this.password : "")
                .setQuitRatio(this.quitRatio)
                .setMinimumRating(this.minimumRating);

        // Add player types as strings
        for (PlayerType playerType : this.playerTypes) {
            builder.addPlayerTypes(playerType.toString());
        }

        // Add limited options if available
        if (this.limitedOptions != null) {
            builder.setLimitedOptions(this.limitedOptions.toProto());
        }

        return builder.build();
    }

    public static TournamentOptions fromProto(ModelProto.TournamentOptions proto) {
        TournamentOptions options = new TournamentOptions(
                proto.getName(),
                proto.getMatchOptionsGameType(),
                proto.getMatchOptionsMultiPlayer()
        );
        options.tournamentType = proto.getTournamentType().isEmpty() ? null : proto.getTournamentType();
        options.watchingAllowed = proto.getWatchingAllowed();
        options.planeChase = proto.getPlaneChase();
        options.numberRounds = proto.getNumberRounds();
        options.password = proto.getPassword().isEmpty() ? null : proto.getPassword();
        options.quitRatio = proto.getQuitRatio();
        options.minimumRating = proto.getMinimumRating();

        // Convert player type strings back to PlayerType enum
        for (String playerTypeStr : proto.getPlayerTypesList()) {
            options.playerTypes.add(PlayerType.getByDescription(playerTypeStr));
        }

        // Convert limited options if present
        if (proto.hasLimitedOptions()) {
            options.limitedOptions = LimitedOptions.fromProto(proto.getLimitedOptions());
        }

        return options;
    }
}
