
package mage.cards.c;

import mage.abilities.Ability;
import mage.abilities.costs.common.PayLifeCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.keyword.DevoidAbility;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.Spell.ability.SpellHasAbilityPredicate;

import java.util.UUID;

/**
 *
 * @author fireshoes
 */
public final class CorruptedCrossroads extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("a spell with devoid")
            .add(new SpellHasAbilityPredicate(DevoidAbility.class));

    public CorruptedCrossroads(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.LAND},"");

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());
        
        // {T}, Pay 1 life: Add one mana of any color. Spend this mana only to cast a spell with devoid.
        Ability ability = ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .cost(new PayLifeCost(1))
                .addAnyColor(1)
                .condition(new FilteredSpellManaCondition(filter))
                .ruleText("Add one mana of any color. Spend this mana only to cast a spell with devoid")
                .build();
        this.addAbility(ability);
    }

    private CorruptedCrossroads(final CorruptedCrossroads card) {
        super(card);
    }

    @Override
    public CorruptedCrossroads copy() {
        return new CorruptedCrossroads(this);
    }
}
