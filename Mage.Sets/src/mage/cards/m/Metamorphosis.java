
package mage.cards.m;

import mage.abilities.costs.common.SacrificeTargetCost;
import mage.abilities.dynamicvalue.IntPlusDynamicValue;
import mage.abilities.dynamicvalue.common.manavalue.SacrificedManaValue;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.StaticFilters;
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 *
 * @author MarcoMarin
 */
public final class Metamorphosis extends CardImpl {

    public Metamorphosis(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{G}");

        // As an additional cost to cast Metamorphosis, sacrifice a creature.
        this.getSpellAbility().addCost(new SacrificeTargetCost(StaticFilters.FILTER_PERMANENT_CREATURE));

        // Add X mana of any one color, where X is one plus the sacrificed creature's converted mana cost. Spend this mana only to cast creature spells.
        this.getSpellAbility().addEffect(ComposedManaAbilityBuilder.builder()
                .addChoiceAnyOneColor(new IntPlusDynamicValue(1, SacrificedManaValue.instance))
                .condition(new FilteredSpellManaCondition(StaticTypedFilters.A_CREATURE_SPELL))
                .ruleText("Add X mana of any one color, where X is 1 plus the sacrificed creature's mana value. Spend this mana only to cast creature spells.")
                .buildEffect()
        );
    }

    private Metamorphosis(final Metamorphosis card) {
        super(card);
    }

    @Override
    public Metamorphosis copy() {
        return new Metamorphosis(this);
    }
}
