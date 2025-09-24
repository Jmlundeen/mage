package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;

/**
 * @author LevelX2
 */
public class SetBasePowerToughnessAttachedEffect extends ContinuousEffectImpl {

    private final int power;
    private final int toughness;

    public SetBasePowerToughnessAttachedEffect(int power, int toughness, AttachmentType attachmentType) {
        super(Duration.WhileOnBattlefield, Layer.PTChangingEffects_7, SubLayer.SetPT_7b, Outcome.BoostCreature);
        staticText = attachmentType.verb() + " creature has base power and toughness " + power + "/" + toughness;
        this.power = power;
        this.toughness = toughness;
    }

    protected SetBasePowerToughnessAttachedEffect(final SetBasePowerToughnessAttachedEffect effect) {
        super(effect);
        this.power = effect.power;
        this.toughness = effect.toughness;
    }

    @Override
    public SetBasePowerToughnessAttachedEffect copy() {
        return new SetBasePowerToughnessAttachedEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            permanent.getPower().setModifiedBaseValue(power);
            permanent.getToughness().setModifiedBaseValue(toughness);
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
