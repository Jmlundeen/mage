
package mage.remote.interfaces;

import mage.players.PlayerType;
import mage.ws.v1.view.ViewProto;

import java.util.List;

/**
 * @author noxx
 */
public interface GameTypes {

    PlayerType[] getPlayerTypes();

    List<ViewProto.GameTypeView> getGameTypes();
    List<ViewProto.GameTypeView> getTournamentGameTypes();

    String[] getDeckTypes();

    String[] getDraftCubes();

    List<ViewProto.TournamentTypeView> getTournamentTypes();
}
