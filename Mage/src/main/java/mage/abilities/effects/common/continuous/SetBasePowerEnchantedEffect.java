package mage.abilities.effects.common.continuous;

import mage.MageItem;
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
 * @author jeffwadsworth
 */
public class SetBasePowerEnchantedEffect extends ContinuousEffectImpl {

    private final int power;

    public SetBasePowerEnchantedEffect(int power) {
        super(Duration.WhileOnBattlefield, Layer.PTChangingEffects_7, SubLayer.SetPT_7b, (power > 0 ? Outcome.BoostCreature : Outcome.Neutral));
        staticText = "Enchanted creature has base power " + power;
        this.power = power;
    }

    protected SetBasePowerEnchantedEffect(final SetBasePowerEnchantedEffect effect) {
        super(effect);
        this.power = effect.power;
    }

    @Override
    public SetBasePowerEnchantedEffect copy() {
        return new SetBasePowerEnchantedEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).getPower().setModifiedBaseValue(power);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent enchantment = game.getPermanent(source.getSourceId());
        if (enchantment == null || enchantment.getAttachedTo() == null) {
            return false;
        }

        Permanent enchanted = game.getPermanent(enchantment.getAttachedTo());
        if (enchanted == null) {
            return false;
        }
        affectedObjects.add(enchanted);
        return true;
    }
}
