package mage.cards.p;

import mage.MageInt;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.condition.common.HaventCastSpellFromHandThisTurnCondition;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ReplacementEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.effects.common.replacement.ReplaceCounterEffect;
import mage.abilities.keyword.LifelinkAbility;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 * @author Susucr
 */
public final class PrairieDog extends CardImpl {

    public PrairieDog(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}");

        this.subtype.add(SubType.SQUIRREL);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Lifelink
        this.addAbility(LifelinkAbility.getInstance());

        // At the beginning of your end step, if you haven't cast a spell from your hand this turn, put a +1/+1 counter on Prairie Dog.
        this.addAbility(new BeginningOfEndStepTriggeredAbility(
                TargetController.YOU, new AddCountersSourceEffect(CounterType.P1P1.createInstance()),
                false, HaventCastSpellFromHandThisTurnCondition.instance
        ).addHint(HaventCastSpellFromHandThisTurnCondition.hint));

        // {4}{W}: Until end of turn, if you would put one or more +1/+1 counters on a creature you control, put that many plus one +1/+1 counters on it instead.
        ReplacementEffect effect = new ReplaceCounterEffect(Duration.EndOfTurn, Outcome.Benefit, ReplaceCounterEffect.ModificationType.ADD, 1)
                .setPermanentFilter(StaticFilters.FILTER_CONTROLLED_CREATURE)
                .setEventController(TargetController.YOU)
                .setText("Until end of turn, if you would put one or more +1/+1 counters on a creature you control, " +
                        "put that many plus one +1/+1 counters on it instead");

        this.addAbility(new SimpleActivatedAbility(effect, new ManaCostsImpl<>("{4}{W}")));
    }

    private PrairieDog(final PrairieDog card) {
        super(card);
    }

    @Override
    public PrairieDog copy() {
        return new PrairieDog(this);
    }
}
