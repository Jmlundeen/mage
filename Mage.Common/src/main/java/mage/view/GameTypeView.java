

package mage.view;

import mage.game.match.MatchType;
import mage.ws.v1.model.ModelProto;

import java.io.Serializable;

/**
 *
 * @author BetaSteward_at_googlemail.com
 */
public class GameTypeView implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final int minPlayers;
    private final int maxPlayers;
    private final int numTeams;
    private final int playersPerTeam;
    private final boolean useRange;
    private final boolean useAttackOption;

    public GameTypeView(MatchType gameType) {
        this.name = gameType.getName();
        this.minPlayers = gameType.getMinPlayers();
        this.maxPlayers = gameType.getMaxPlayers();
        this.numTeams = gameType.getNumTeams();
        this.playersPerTeam = gameType.getPlayersPerTeam();
        this.useAttackOption = gameType.isUseAttackOption();
        this.useRange = gameType.isUseRange();
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getNumTeams() {
        return numTeams;
    }

    public int getPlayersPerTeam() {
        return playersPerTeam;
    }

    public boolean isUseRange() {
        return useRange;
    }

    public boolean isUseAttackOption() {
        return useAttackOption;
    }

    public ModelProto.GameTypeView toProto() {
        return ModelProto.GameTypeView.newBuilder()
                .setName(name != null ? name : "")
                .setMinPlayers(minPlayers)
                .setMaxPlayers(maxPlayers)
                .setNumTeams(numTeams)
                .setPlayersPerTeam(playersPerTeam)
                .setUseRange(useRange)
                .setUseAttackOption(useAttackOption)
                .build();
    }

    public static GameTypeView fromProto(ModelProto.GameTypeView proto) {
        return new GameTypeView(proto);
    }

    // Private constructor for fromProto
    private GameTypeView(ModelProto.GameTypeView proto) {
        this.name = proto.getName();
        this.minPlayers = proto.getMinPlayers();
        this.maxPlayers = proto.getMaxPlayers();
        this.numTeams = proto.getNumTeams();
        this.playersPerTeam = proto.getPlayersPerTeam();
        this.useRange = proto.getUseRange();
        this.useAttackOption = proto.getUseAttackOption();
    }

}
