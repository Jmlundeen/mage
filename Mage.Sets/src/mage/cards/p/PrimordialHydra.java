package mage.cards.p;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.SourceHasCounterCondition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.dynamicvalue.common.GetXValue;
import mage.abilities.effects.common.DoubleCountersSourceEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.TrampleAbility;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ContinuousAffected;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 * @author Loki
 */
public final class PrimordialHydra extends CardImpl {

    private static final Condition condition = new SourceHasCounterCondition(CounterType.P1P1, 10);

    public PrimordialHydra(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{X}{G}{G}");
        this.subtype.add(SubType.HYDRA);

        this.color.setGreen(true);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Primordial Hydra enters the battlefield with X +1/+1 counters on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.P1P1, GetXValue.instance)));

        // At the beginning of your upkeep, double the number of +1/+1 counters on Primordial Hydra.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(new DoubleCountersSourceEffect(CounterType.P1P1)));

        // Primordial Hydra has trample as long as it has ten or more +1/+1 counters on it.
        this.addAbility(new SimpleStaticAbility(new ConditionalContinuousEffect(
                new ContinuousEffectBuilder(Outcome.AddAbility, ContinuousAffected.SOURCE)
                        .withGainedAbilities(TrampleAbility.getInstance()),
                condition,
                "{this} has trample as long as it has ten or more +1/+1 counters on it"
        )));
    }

    private PrimordialHydra(final PrimordialHydra card) {
        super(card);
    }

    @Override
    public PrimordialHydra copy() {
        return new PrimordialHydra(this);
    }
}
