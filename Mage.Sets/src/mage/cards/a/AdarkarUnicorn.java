package mage.cards.a;

import mage.MageInt;
import mage.Mana;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredAbilityManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.ability.type.CumulativeUpkeepAbilityPredicate;

import java.util.UUID;

/**
 * @author Cguy7777
 */
public final class AdarkarUnicorn extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("cumulative upkeep ability")
            .add(CumulativeUpkeepAbilityPredicate.instance);

    public AdarkarUnicorn(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}{W}");

        this.subtype.add(SubType.UNICORN);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // {T}: Add {U} or {1}{U}. Spend this mana only to pay cumulative upkeep costs.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.BlueMana(1))
                .addStatic(0, 1, 0, 0, 0, 1, 0, 0)
                .chooseManaValue()
                .condition(new FilteredAbilityManaCondition(filter))
                .ruleText("Add {U} or {1}{U}. Spend this mana only to pay cumulative upkeep costs")
                .build()
        );
    }

    private AdarkarUnicorn(final AdarkarUnicorn card) {
        super(card);
    }

    @Override
    public AdarkarUnicorn copy() {
        return new AdarkarUnicorn(this);
    }
}
