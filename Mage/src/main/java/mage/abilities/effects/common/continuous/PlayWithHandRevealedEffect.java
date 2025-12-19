
package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.*;
import mage.game.Game;
import mage.players.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author LoneFox
 */
public class PlayWithHandRevealedEffect extends ContinuousEffectImpl {

    private final TargetController who;

    public PlayWithHandRevealedEffect(TargetController who) {
        super(Duration.WhileOnBattlefield, Layer.PlayerEffects, SubLayer.NA, Outcome.Detriment);
        this.who = who;
    }

    protected PlayWithHandRevealedEffect(final PlayWithHandRevealedEffect effect) {
        super(effect);
        who = effect.who;
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Player player = (Player) object;
            player.revealCards("Cards in " + player.getName() + "'s hand", player.getHand(), game, false);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Iterable<UUID> affectedPlayers;
        switch (who) {
            case ANY:
                affectedPlayers = game.getState().getPlayersInRange(controller.getId(), game);
                break;
            case OPPONENT:
                affectedPlayers = game.getOpponents(source.getControllerId());
                break;
            case YOU:
                List<UUID> tmp = new ArrayList<>();
                tmp.add(source.getControllerId());
                affectedPlayers = tmp;
                break;
            default:
                return false;
        }
        for (UUID playerID : affectedPlayers) {
            Player player = game.getPlayer(playerID);
            if (player != null) {
                affectedObjects.add(player);
            }
        }
        return !affectedObjects.isEmpty();
    }

    @Override
    public PlayWithHandRevealedEffect copy() {
        return new PlayWithHandRevealedEffect(this);
    }

    @Override
    public String getText(Mode mode) {
        if (staticText != null && !staticText.isEmpty()) {
            return staticText;
        }
        switch (who) {
            case ANY:
                return "Players play with their hands revealed";
            case OPPONENT:
                return "Your opponents play with their hands revealed";
            case YOU:
                return "Play with your hand revealed";
            default:
                return "Unknown TargetController for PlayWithHandRevealedEffect";
        }
    }
}
