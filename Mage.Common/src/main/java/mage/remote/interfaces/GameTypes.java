
package mage.remote.interfaces;

import mage.players.PlayerType;
import mage.view.GameTypeView;
import mage.view.TournamentTypeView;

import java.util.List;

/**
 * @author noxx
 */
public interface GameTypes {

    PlayerType[] getPlayerTypes();

    List<GameTypeView> getGameTypes();
    List<GameTypeView> getTournamentGameTypes();

    String[] getDeckTypes();

    String[] getDraftCubes();

    List<TournamentTypeView> getTournamentTypes();
}
