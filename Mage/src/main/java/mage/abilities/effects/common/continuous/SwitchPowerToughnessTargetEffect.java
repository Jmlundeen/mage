package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;
import java.util.UUID;

/**
 * @author ayratn
 */
public class SwitchPowerToughnessTargetEffect extends ContinuousEffectImpl {

    public SwitchPowerToughnessTargetEffect(Duration duration) {
        super(duration, Layer.PTChangingEffects_7, SubLayer.SwitchPT_e, Outcome.BoostCreature);
    }

    protected SwitchPowerToughnessTargetEffect(final SwitchPowerToughnessTargetEffect effect) {
        super(effect);
    }

    @Override
    public SwitchPowerToughnessTargetEffect copy() {
        return new SwitchPowerToughnessTargetEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).switchPowerToughness();
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (UUID uuid : getTargetPointer().getTargets(game, source)) {
            Permanent permanent = game.getPermanent(uuid);
            if (permanent != null) {
                affectedObjects.add(permanent);
            }
        }
        return !affectedObjects.isEmpty();
    }

    @Override
    public String getText(Mode mode) {
        if (staticText != null && !staticText.isEmpty()) {
            return staticText;
        }
        return "switch " + getTargetPointer().describeTargets(mode.getTargets(), "that creature") + "'s power and toughness" +
                ' ' + duration.toString();
    }
}
