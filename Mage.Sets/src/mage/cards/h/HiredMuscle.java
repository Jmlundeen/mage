package mage.cards.h;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.SourceHasCounterCondition;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.effects.common.FlipSourceEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.keyword.FearAbility;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.cards.CardSetInfo;
import mage.cards.FlipCard;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class HiredMuscle extends FlipCard {

    private static final Condition condition = new SourceHasCounterCondition(CounterType.KI, 2);

    public HiredMuscle(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new CardType[]{CardType.CREATURE}, new SubType[]{SubType.HUMAN, SubType.WARRIOR}, "{1}{B}{B}",
                "Scarmaker",
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.SPIRIT});

        // Hired Muscle
        this.getLeftHalfCard().setPT(2, 2);

        // Whenever you cast a Spirit or Arcane spell, you may put a ki counter on Hired Muscle.
        this.getLeftHalfCard().addAbility(new SpellCastControllerTriggeredAbility(
                new AddCountersSourceEffect(CounterType.KI.createInstance()),
                StaticFilters.FILTER_SPELL_SPIRIT_OR_ARCANE, true
        ));

        // At the beginning of the end step, if there are two or more ki counters on Hired Muscle, you may flip it.
        this.getLeftHalfCard().addAbility(new BeginningOfEndStepTriggeredAbility(
                TargetController.NEXT, new FlipSourceEffect().setText("flip it"), true, condition
        ));

        // Scarmaker
        this.getRightHalfCard().setPT(4, 4);

        // Remove a ki counter from Scarmaker: Target creature gains fear until end of turn.
        Ability ability = new SimpleActivatedAbility(
                new GainAbilityTargetEffect(FearAbility.getInstance(), Duration.EndOfTurn),
                new RemoveCountersSourceCost(CounterType.KI.createInstance()));
        ability.addTarget(new TargetCreaturePermanent());
        this.getRightHalfCard().addAbility(ability);
    }

    private HiredMuscle(final HiredMuscle card) {
        super(card);
    }

    @Override
    public HiredMuscle copy() {
        return new HiredMuscle(this);
    }
}
