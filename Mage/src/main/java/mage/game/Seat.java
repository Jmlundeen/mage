
package mage.game;

import mage.players.Player;
import mage.players.PlayerType;
import mage.players.net.UserData;
import mage.ws.view.ViewProto;

import java.io.Serializable;

/**
 *
 * @author BetaSteward_at_googlemail.com
 */
public class Seat implements Serializable {

//    private static final Logger logger = Logger.getLogger(Seat.class);
    private PlayerType playerType;
    private Player player;

    public Seat(PlayerType playerType) {
        this.playerType = playerType;
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    public void setPlayerType(PlayerType playerType) {
        this.playerType = playerType;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public ViewProto.SeatView toProto() {
        ViewProto.SeatView.Builder seatView = ViewProto.SeatView.newBuilder();
        if (this.getPlayer() != null) {
            seatView.setPlayerId(this.getPlayer().getId().toString());
            seatView.setPlayerName(this.getPlayer().getName());
            if (this.getPlayer().getUserData() == null) {
                seatView.setFlagName(UserData.getDefaultFlagName());
                seatView.setHistory("");
                seatView.setGeneralRating(0);
                seatView.setConstructedRating(0);
                seatView.setLimitedRating(0);
            } else {
                seatView.setFlagName(this.getPlayer().getUserData().getFlagName());
                seatView.setHistory(this.getPlayer().getUserData().getHistory());
                seatView.setGeneralRating(this.getPlayer().getUserData().getGeneralRating());
                seatView.setConstructedRating(this.getPlayer().getUserData().getConstructedRating());
                seatView.setLimitedRating(this.getPlayer().getUserData().getLimitedRating());
            }
        } else {
            // Empty seat
            seatView.setPlayerName("");
            seatView.setFlagName("");
            seatView.setHistory("");
            seatView.setGeneralRating(0);
            seatView.setConstructedRating(0);
            seatView.setLimitedRating(0);
        }
        seatView.setPlayerType(this.getPlayerType().toString());
        return seatView.build();
    }
}
