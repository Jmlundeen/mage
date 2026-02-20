
package mage.view;

import mage.game.tournament.TournamentType;
import mage.ws.model.ModelProto;

import java.io.Serializable;

/**
 *
 * @author BetaSteward_at_googlemail.com
 */
public class TournamentTypeView implements Serializable {

    private static final long serialVersionUID = 2L;

    private final String name;
    private final int minPlayers;
    private final int maxPlayers;
    private final int numBoosters;
    private final boolean draft;
    private final boolean limited;
    private final boolean cubeBooster;
    private final boolean elimination;
    private final boolean random;
    private final boolean reshuffled;
    private final boolean richMan;
    private final boolean jumpstart;

    public TournamentTypeView(TournamentType tournamentType) {
        this.name = tournamentType.getName();
        this.minPlayers = tournamentType.getMinPlayers();
        this.maxPlayers = tournamentType.getMaxPlayers();
        this.numBoosters = tournamentType.getNumBoosters();
        this.draft = tournamentType.isDraft();
        this.limited = tournamentType.isLimited();
        this.cubeBooster = tournamentType.isCubeBooster();
        this.elimination = tournamentType.isElimination();
        this.random = tournamentType.isRandom();
        this.reshuffled = tournamentType.isReshuffled();
        this.richMan = tournamentType.isRichMan();
        this.jumpstart = tournamentType.isJumpstart();
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

    public int getNumBoosters() {
        return numBoosters;
    }

    public boolean isDraft() {
        return draft;
    }

    public boolean isLimited() {
        return limited;
    }

    public boolean isCubeBooster() {
        return cubeBooster;
    }

    public boolean isElimination() {
        return elimination;
    }

    public boolean isRandom() {
        return random;
    }

    public boolean isReshuffled() {
        return reshuffled;
    }

    public boolean isRichMan() {
        return richMan;
    }

    public boolean isJumpstart() {
        return jumpstart;
    }

    public ModelProto.TournamentTypeView toProto() {
        return ModelProto.TournamentTypeView.newBuilder()
                .setName(name != null ? name : "")
                .setMinPlayers(minPlayers)
                .setMaxPlayers(maxPlayers)
                .setNumBoosters(numBoosters)
                .setDraft(draft)
                .setLimited(limited)
                .setCubeBooster(cubeBooster)
                .setElimination(elimination)
                .setRandom(random)
                .setReshuffled(reshuffled)
                .setRichMan(richMan)
                .setJumpstart(jumpstart)
                .build();
    }

    public static TournamentTypeView fromProto(ModelProto.TournamentTypeView proto) {
        return new TournamentTypeView(proto);
    }

    // Private constructor for fromProto
    private TournamentTypeView(ModelProto.TournamentTypeView proto) {
        this.name = proto.getName();
        this.minPlayers = proto.getMinPlayers();
        this.maxPlayers = proto.getMaxPlayers();
        this.numBoosters = proto.getNumBoosters();
        this.draft = proto.getDraft();
        this.limited = proto.getLimited();
        this.cubeBooster = proto.getCubeBooster();
        this.elimination = proto.getElimination();
        this.random = proto.getRandom();
        this.reshuffled = proto.getReshuffled();
        this.richMan = proto.getRichMan();
        this.jumpstart = proto.getJumpstart();
    }

}
