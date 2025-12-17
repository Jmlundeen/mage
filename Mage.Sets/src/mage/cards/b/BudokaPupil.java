package mage.cards.b;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.SourceHasCounterCondition;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.effects.common.FlipSourceEffect;
import mage.abilities.effects.common.continuous.BoostTargetEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.keyword.TrampleAbility;
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
public final class BudokaPupil extends FlipCard {

    private static final Condition condition = new SourceHasCounterCondition(CounterType.KI, 2);

    public BudokaPupil(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new CardType[]{CardType.CREATURE}, new SubType[]{SubType.HUMAN, SubType.MONK}, "{1}{G}{G}",
                "Ichiga, Who Topples Oaks",
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.SPIRIT});

        // Budoka Pupil
        this.getLeftHalfCard().setPT(2, 2);

        // Whenever you cast a Spirit or Arcane spell, you may put a ki counter on Budoka Pupil.
        this.getLeftHalfCard().addAbility(new SpellCastControllerTriggeredAbility(new AddCountersSourceEffect(CounterType.KI.createInstance()), StaticFilters.FILTER_SPELL_SPIRIT_OR_ARCANE, true));

        // At the beginning of the end step, if there are two or more ki counters on Budoka Pupil, you may flip it.
        this.getLeftHalfCard().addAbility(new BeginningOfEndStepTriggeredAbility(
                TargetController.NEXT, new FlipSourceEffect().setText("flip it"), true, condition
        ));

        // Ichiga, Who Topples Oaks
        this.getRightHalfCard().setPT(4, 3);

        // Trample.
        this.getRightHalfCard().addAbility(TrampleAbility.getInstance());

        // Remove a ki counter from Ichiga, Who Topples Oaks: Target creature gets +2/+2 until end of turn.
        Ability ability = new SimpleActivatedAbility(
                new BoostTargetEffect(2, 2, Duration.EndOfTurn),
                new RemoveCountersSourceCost(CounterType.KI.createInstance()));
        ability.addTarget(new TargetCreaturePermanent());
        this.getRightHalfCard().addAbility(ability);
    }

    private BudokaPupil(final BudokaPupil card) {
        super(card);
    }

    @Override
    public BudokaPupil copy() {
        return new BudokaPupil(this);
    }
}
