
package mage.abilities.effects.keyword;

import mage.ObjectColor;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.keyword.ProtectionAbility;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.predicate.mageobject.ColorPredicate;

/**
 * @author LevelX2
 */
public class ProtectionChosenColorSourceEffect extends ContinuousEffectBuilder {

    public ProtectionChosenColorSourceEffect() {
        super(Duration.WhileOnBattlefield, Outcome.AddAbility, ContinuousAffected.SOURCE);
        withGainedAbility((card, source, game) -> {
            ObjectColor color = (ObjectColor) game.getState().getValue(card.getId() + "_color");
            if (color != null) {
                FilterCard protectionFilter = new FilterCard(color.getDescription());
                protectionFilter.add(new ColorPredicate(color));
                return new ProtectionAbility(protectionFilter);
            }
            return null;
        });
        staticText = "{this} has protection from the chosen color";
    }

    protected ProtectionChosenColorSourceEffect(final ProtectionChosenColorSourceEffect effect) {
        super(effect);
    }

    @Override
    public ProtectionChosenColorSourceEffect copy() {
        return new ProtectionChosenColorSourceEffect(this);
    }
}
