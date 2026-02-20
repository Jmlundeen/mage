

package mage.interfaces;

import mage.players.PlayerType;
import mage.utils.MageVersion;
import mage.view.GameTypeView;
import mage.view.TournamentTypeView;
import mage.ws.model.ModelProto;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author BetaSteward_at_googlemail.com
 */
public record ServerState(List<GameTypeView> gameTypes, List<TournamentTypeView> tournamentTypes, PlayerType[] playerTypes,
                          String[] deckTypes, String[] draftCubes, boolean testMode, MageVersion version, long cardsContentVersion,
                          long expansionsContentVersion) implements Serializable {

    public List<GameTypeView> getTournamentGameTypes() {
        return gameTypes.stream()
                .filter(gameTypeView -> gameTypeView.getMinPlayers() == 2 && gameTypeView.getMaxPlayers() == 2)
                .collect(Collectors.toList());
    }

    public ModelProto.ServerState toProto() {
        return ModelProto.ServerState.newBuilder()
                .addAllGameTypes(gameTypes.stream()
                        .map(GameTypeView::toProto)
                        .collect(Collectors.toList()))
                .addAllTournamentTypes(tournamentTypes.stream()
                        .map(TournamentTypeView::toProto)
                        .collect(Collectors.toList()))
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
                proto.getGameTypesList().stream()
                        .map(GameTypeView::fromProto)
                        .collect(Collectors.toList()),
                proto.getTournamentTypesList().stream()
                        .map(TournamentTypeView::fromProto)
                        .collect(Collectors.toList()),
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
