package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;

/**
 * @author xenohedron
 */

public class SetChosenColorEffect extends ContinuousEffectImpl {

    public SetChosenColorEffect() {
        super(Duration.WhileOnBattlefield, Layer.ColorChangingEffects_5, SubLayer.NA, Outcome.Neutral);
        staticText = "{this} is the chosen color.";
    }

    protected SetChosenColorEffect(final SetChosenColorEffect effect) {
        super(effect);
    }

    @Override
    public SetChosenColorEffect copy() {
        return new SetChosenColorEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            ObjectColor color = (ObjectColor) game.getState().getValue(source.getSourceId() + "_color");
            permanent.getColor(game).setColor(color);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = game.getPermanent(source.getSourceId());
        ObjectColor color = (ObjectColor) game.getState().getValue(source.getSourceId() + "_color");
        if (permanent != null && color != null) {
            affectedObjects.add(permanent);
            return true;
        }
        return false;
    }
}
