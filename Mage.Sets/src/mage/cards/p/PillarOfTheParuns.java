
package mage.cards.p;

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
 * @author noxx
 */
public final class PillarOfTheParuns extends CardImpl {

    public PillarOfTheParuns(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // {T}: Add one mana of any color. Spend this mana only to cast a multicolored spell.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyColor(1)
                .condition(new FilteredSpellManaCondition(StaticTypedFilters.A_MULTICOLORED_SPELL))
                .ruleText("Add one mana of any color. Spend this mana only to cast a multicolored spell.")
                .build()
        );
    }

    private PillarOfTheParuns(final PillarOfTheParuns card) {
        super(card);
    }

    @Override
    public PillarOfTheParuns copy() {
        return new PillarOfTheParuns(this);
    }
}
