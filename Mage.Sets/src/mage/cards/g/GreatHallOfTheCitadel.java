package mage.cards.g;

import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SuperType;
import mage.filter.FilterTyped;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class GreatHallOfTheCitadel extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("a legendary spell")
            .add(SuperType.LEGENDARY.getPredicate());

    public GreatHallOfTheCitadel(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {1}, {T}: Add two mana in any combination of colors. Spend this mana only to cast legendary spells.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new GenericManaCost(1))
                .cost(new TapSourceCost())
                .addAnyCombination(2)
                .condition(new FilteredSpellManaCondition(filter))
                .ruleText("Add two mana in any combination of colors. Spend this mana only to cast legendary spells")
                .build()
        );
    }

    private GreatHallOfTheCitadel(final GreatHallOfTheCitadel card) {
        super(card);
    }

    @Override
    public GreatHallOfTheCitadel copy() {
        return new GreatHallOfTheCitadel(this);
    }
}
