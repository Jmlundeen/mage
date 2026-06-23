package mage.cards.k;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.RemoveVariableCountersSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.dynamicvalue.IntPlusDynamicValue;
import mage.abilities.dynamicvalue.common.GetXValue;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ManaType;
import mage.counters.CounterType;

import java.util.Set;
import java.util.UUID;

/**
 * @author LevelX2
 */
public final class KyrenToy extends CardImpl {

    public KyrenToy(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{3}");

        // {1}, {T}: Put a charge counter on Kyren Toy.
        Ability ability = new SimpleActivatedAbility(new AddCountersSourceEffect(CounterType.CHARGE.createInstance(1)), new GenericManaCost(1));
        ability.addCost(new TapSourceCost());
        this.addAbility(ability);

        // {T}, Remove X charge counters from Kyren Toy: Add X mana of {C}, and then add {C}.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .cost(new RemoveVariableCountersSourceCost(CounterType.CHARGE))
                .addDynamicChoice(new IntPlusDynamicValue(1, GetXValue.instance), Set.of(ManaType.COLORLESS))
                .ruleText("Add an amount of {C} equal to X plus one")
                .build()
        );
    }

    private KyrenToy(final KyrenToy card) {
        super(card);
    }

    @Override
    public KyrenToy copy() {
        return new KyrenToy(this);
    }
}
