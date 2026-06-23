package mage.cards.o;

import mage.MageInt;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredAbilityManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class OmenHawker extends CardImpl {

    public OmenHawker(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{U}");

        this.subtype.add(SubType.OCTOPUS);
        this.subtype.add(SubType.ADVISOR);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // {T}: Add {C}{U}. Spend this many only to activate abilities.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(0, 1, 0, 0, 0, 1, 0)
                .condition(new FilteredAbilityManaCondition(StaticTypedFilters.ACTIVATED_ABILITY))
                .ruleText("Add {C}{U}. Spend this mana only to activate abilities")
                .build()
        );
    }

    private OmenHawker(final OmenHawker card) {
        super(card);
    }

    @Override
    public OmenHawker copy() {
        return new OmenHawker(this);
    }
}
