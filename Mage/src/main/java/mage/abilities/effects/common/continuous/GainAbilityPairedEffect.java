
package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.MageObjectReference;
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
 * @author noxx
 */
public class GainAbilityPairedEffect extends ContinuousEffectImpl {

    protected Ability ability;

    public GainAbilityPairedEffect(Ability ability, String rule) {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.ability = ability;
        staticText = rule;
    }

    protected GainAbilityPairedEffect(final GainAbilityPairedEffect effect) {
        super(effect);
        this.ability = effect.ability.copy();
        ability.newId(); // This is needed if the effect is copied e.g. by a clone so the ability can be added multiple times to permanents
    }

    @Override
    public GainAbilityPairedEffect copy() {
        return new GainAbilityPairedEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).addAbility(ability, source.getSourceId(), game);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = game.getPermanent(source.getSourceId());
        if (permanent == null || permanent.getPairedCard() == null) {
            return false;
        }
        Permanent paired = permanent.getPairedCard().getPermanent(game);
        if (paired != null && paired.getPairedCard() != null && paired.getPairedCard().equals(new MageObjectReference(permanent, game))) {
            affectedObjects.add(permanent);
            affectedObjects.add(paired);
            return true;
        } else {
            // No longer the same cards as originally paired.
            permanent.setPairedCard(null);
            return false;
        }
    }
}
