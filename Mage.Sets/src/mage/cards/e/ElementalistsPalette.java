package mage.cards.e;

import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.mana.AnyColorManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.XCostManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ManaType;
import mage.counters.CounterType;
import mage.filter.FilterSpell;
import mage.filter.predicate.mageobject.VariableManaCostPredicate;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class ElementalistsPalette extends CardImpl {

    private static final FilterSpell filter = new FilterSpell("a spell with {X} in its mana cost");

    static {
        filter.add(VariableManaCostPredicate.instance);
    }

    public ElementalistsPalette(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{3}");

        // Whenever you cast a spell with {X} in its mana cost, put two charge counters on Elementalist's Palette.
        this.addAbility(new SpellCastControllerTriggeredAbility(
                new AddCountersSourceEffect(CounterType.CHARGE.createInstance(2)), filter, false
        ));

        // {T}: Add one mana of any color.
        this.addAbility(new AnyColorManaAbility());

        // {T}: Add {C} for each charge counter on Elementalist's Palette. Spend this mana only on costs that contain {X}.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addDynamic(new CountersSourceCount(CounterType.CHARGE), ManaType.COLORLESS)
                .condition(new XCostManaCondition())
                .ruleText("Add {C} for each charge counter on {this}. Spend this mana only on costs that contain {X}")
                .build()
        );
    }

    private ElementalistsPalette(final ElementalistsPalette card) {
        super(card);
    }

    @Override
    public ElementalistsPalette copy() {
        return new ElementalistsPalette(this);
    }
}
