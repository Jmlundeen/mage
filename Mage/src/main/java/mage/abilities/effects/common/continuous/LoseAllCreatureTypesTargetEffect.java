package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;

/**
 * @author emerald000
 */
public class LoseAllCreatureTypesTargetEffect extends ContinuousEffectImpl {

    public LoseAllCreatureTypesTargetEffect(Duration duration) {
        super(duration, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Neutral);
    }

    protected LoseAllCreatureTypesTargetEffect(final LoseAllCreatureTypesTargetEffect effect) {
        super(effect);
    }

    @Override
    public LoseAllCreatureTypesTargetEffect copy() {
        return new LoseAllCreatureTypesTargetEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).removeAllCreatureTypes(game);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent != null) {
            affectedObjects.add(permanent);
            return true;
        }
        return false;
    }

    @Override
    public String getText(Mode mode) {
        if (staticText != null && !staticText.isEmpty()) {
            return staticText;
        }
        return getTargetPointer().describeTargets(mode.getTargets(), "it") + " loses all creature types"
                + (duration.toString().isEmpty() ? "" : ' ' + duration.toString());
    }
}
