
package mage.cards.a;

import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 *
 * @author Plopman
 */
public final class AncientZiggurat extends CardImpl {

    public AncientZiggurat(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.LAND},"");

        // {tap}: Add one mana of any color. Spend this mana only to cast a creature spell.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyColor(1)
                .condition(new FilteredSpellManaCondition(StaticTypedFilters.A_CREATURE_SPELL))
                .ruleText("Add one mana of any color. Spend this mana only to cast a creature spell")
                .build()
        );
    }

    private AncientZiggurat(final AncientZiggurat card) {
        super(card);
    }

    @Override
    public AncientZiggurat copy() {
        return new AncientZiggurat(this);
    }
}
