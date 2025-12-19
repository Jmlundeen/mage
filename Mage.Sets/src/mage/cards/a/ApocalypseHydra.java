package mage.cards.a;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.DynamicValueCompareCondition;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.MultipliedValue;
import mage.abilities.dynamicvalue.common.GetXValue;
import mage.abilities.effects.common.DamageTargetEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ComparisonType;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.target.common.TargetAnyTarget;

import java.util.UUID;

/**
 * @author Loki
 */
public final class ApocalypseHydra extends CardImpl {

    private static final DynamicValue xValue = GetXValue.instance;
    private static final DynamicValue doubledXValue = new MultipliedValue(GetXValue.instance, 2);
    private static final Condition xIs5OrMore = new DynamicValueCompareCondition(xValue, ComparisonType.OR_GREATER, 5);

    public ApocalypseHydra(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{X}{R}{G}");
        this.subtype.add(SubType.HYDRA);

        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Apocalypse Hydra enters the battlefield with X +1/+1 counters on it. If X is 5 or more, it enters with an additional X +1/+1 counters on it.
        this.addAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1, doubledXValue),
                xIs5OrMore,
                new EntersWithCountersEffect(CounterType.P1P1, xValue))
                .setText("{this} enters with X +1/+1 counters on it. If X is 5 or more, it enters with an additional X +1/+1 counters on it")
        ));

        // {1}{R}, Remove a +1/+1 counter from Apocalypse Hydra: Apocalypse Hydra deals 1 damage to any target.
        Ability ability = new SimpleActivatedAbility(
                new DamageTargetEffect(1, "it"), new ManaCostsImpl<>("{1}{R}")
        );
        ability.addCost(new RemoveCountersSourceCost(CounterType.P1P1.createInstance()));
        ability.addTarget(new TargetAnyTarget());
        this.addAbility(ability);
    }

    private ApocalypseHydra(final ApocalypseHydra card) {
        super(card);
    }

    @Override
    public ApocalypseHydra copy() {
        return new ApocalypseHydra(this);
    }
}
