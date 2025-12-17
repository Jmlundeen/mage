package mage.cards.c;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.SourceHasCounterCondition;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.CounterUnlessPaysEffect;
import mage.abilities.effects.common.FlipSourceEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.cards.CardSetInfo;
import mage.cards.FlipCard;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.TargetController;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.target.TargetSpell;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class CallowJushi extends FlipCard {

    private static final Condition condition = new SourceHasCounterCondition(CounterType.KI, 2);

    public CallowJushi(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new CardType[]{CardType.CREATURE}, new SubType[]{SubType.HUMAN, SubType.WIZARD}, "{1}{U}{U}",
                "Jaraku the Interloper",
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.SPIRIT});

        // Callow Jushi
        this.getLeftHalfCard().setPT(2, 2);

        // Whenever you cast a Spirit or Arcane spell, you may put a ki counter on Callow Jushi.
        this.getLeftHalfCard().addAbility(new SpellCastControllerTriggeredAbility(new AddCountersSourceEffect(CounterType.KI.createInstance()), StaticFilters.FILTER_SPELL_SPIRIT_OR_ARCANE, true));

        // At the beginning of the end step, if there are two or more ki counters on Callow Jushi, you may flip it.
        this.getLeftHalfCard().addAbility(new BeginningOfEndStepTriggeredAbility(
                TargetController.NEXT, new FlipSourceEffect().setText("flip it"), true, condition
        ));

        // Jaraku the Interloper
        this.getRightHalfCard().setPT(3, 4);

        // Remove a ki counter from Jaraku the Interloper: Counter target spell unless its controller pays {2}.
        Ability ability = new SimpleActivatedAbility(
                new CounterUnlessPaysEffect(new GenericManaCost(2)),
                new RemoveCountersSourceCost(CounterType.KI.createInstance()));
        ability.addTarget(new TargetSpell());
        this.getRightHalfCard().addAbility(ability);
    }

    private CallowJushi(final CallowJushi card) {
        super(card);
    }

    @Override
    public CallowJushi copy() {
        return new CallowJushi(this);
    }
}
