

package mage.interfaces;

import mage.players.PlayerType;
import mage.utils.MageVersion;
import mage.ws.v1.model.ModelProto;
import mage.ws.v1.view.ViewProto;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class ServerState implements Serializable {

    private final List<ViewProto.GameTypeView> gameTypes;
    private final List<ViewProto.TournamentTypeView> tournamentTypes;
    private final PlayerType[] playerTypes;
    private final String[] deckTypes;
    private final String[] draftCubes;
    private final boolean testMode;
    private final MageVersion version;
    private final long cardsContentVersion;
    private final long expansionsContentVersion;

    public ServerState(List<ViewProto.GameTypeView> gameTypes, List<ViewProto.TournamentTypeView> tournamentTypes,
                       PlayerType[] playerTypes, String[] deckTypes, String[] draftCubes, boolean testMode,
                       MageVersion version, long cardsContentVersion, long expansionsContentVersion) {
        this.gameTypes = gameTypes;
        this.tournamentTypes = tournamentTypes;
        this.playerTypes = playerTypes;
        this.deckTypes = deckTypes;
        this.draftCubes = draftCubes;
        this.testMode = testMode;
        this.version = version;
        this.cardsContentVersion = cardsContentVersion;
        this.expansionsContentVersion = expansionsContentVersion;

    }

    public List<ViewProto.GameTypeView> getGameTypes() {
        return gameTypes;
    }

    public List<ViewProto.GameTypeView> getTournamentGameTypes() {
        return gameTypes.stream()
                .filter(gameTypeView -> gameTypeView.getMinPlayers() == 2 && gameTypeView.getMaxPlayers() == 2)
                .collect(Collectors.toList());
    }

    public List<ViewProto.TournamentTypeView> getTournamentTypes() {
        return tournamentTypes;
    }

    public PlayerType[] getPlayerTypes() {
        return playerTypes;
    }

    public String[] getDeckTypes() {
        return deckTypes;
    }

    public String[] getDraftCubes() {
        return draftCubes;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public MageVersion getVersion() {
        return version;
    }

    public long getCardsContentVersion() {
        return cardsContentVersion;
    }

    public long getExpansionsContentVersion() {
        return expansionsContentVersion;
    }

    public ModelProto.ServerState toProto() {
        return ModelProto.ServerState.newBuilder()
                .addAllGameTypes(gameTypes)
                .addAllTournamentTypes(tournamentTypes)
                .addAllPlayerTypes(
                        Arrays.stream(playerTypes)
                                .map(PlayerType::toString)
                                .collect(Collectors.toList())
                )
                .addAllDeckTypes(Arrays.asList(deckTypes))
                .addAllDraftCubes(Arrays.asList(draftCubes))
                .setTestMode(testMode)
                .setMageVersion(version.toProto())
                .setCardsContentVersion(cardsContentVersion)
                .setExpansionsContentVersion(expansionsContentVersion)
                .build();
    }

    public static ServerState fromProto(ModelProto.ServerState proto) {
        return new ServerState(
                proto.getGameTypesList(),
                proto.getTournamentTypesList(),
                proto.getPlayerTypesList().stream()
                        .map(PlayerType::getByDescription)
                        .toArray(PlayerType[]::new),
                proto.getDeckTypesList().toArray(new String[0]),
                proto.getDraftCubesList().toArray(new String[0]),
                proto.getTestMode(),
                MageVersion.fromProto(proto.getMageVersion(), ServerState.class),
                proto.getCardsContentVersion(),
                proto.getExpansionsContentVersion()
        );
    }
}
