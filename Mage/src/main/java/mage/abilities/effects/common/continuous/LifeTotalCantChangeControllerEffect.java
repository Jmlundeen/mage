
package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.game.Game;
import mage.players.Player;

import java.util.List;

/**
 * @author nantuko
 */
public class LifeTotalCantChangeControllerEffect extends ContinuousEffectImpl {

    public LifeTotalCantChangeControllerEffect(Duration duration) {
        super(duration, Layer.PlayerEffects, SubLayer.NA, Outcome.Benefit);
        staticText = "Your life total can't change. <i>(You can't gain or lose life. You can't pay any amount of life except 0.)</i>";
    }

    protected LifeTotalCantChangeControllerEffect(final LifeTotalCantChangeControllerEffect effect) {
        super(effect);
    }

    @Override
    public LifeTotalCantChangeControllerEffect copy() {
        return new LifeTotalCantChangeControllerEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Player) object).setLifeTotalCanChange(false);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player player = game.getPlayer(source.getControllerId());
        if (player != null) {
            affectedObjects.add(player);
            return true;
        } else {
            return false;
        }
    }

}
