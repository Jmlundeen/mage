package mage.cards.i;

import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.continuous.GainAbilityControlledEffect;
import mage.abilities.effects.common.continuous.GainClassAbilitySourceEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.abilities.effects.common.replacement.ReplaceCounterEffect;
import mage.abilities.keyword.ClassLevelAbility;
import mage.abilities.keyword.ClassReminderAbility;
import mage.abilities.keyword.WardAbility;
import mage.abilities.triggers.BeginningOfCombatTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.constants.TargetController;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.predicate.permanent.CounterAnyPredicate;
import mage.target.common.TargetControlledCreaturePermanent;

import java.util.UUID;

/**
 * @author PurpleCrowbar
 */
public final class InnkeepersTalent extends CardImpl {

    private static final FilterControlledPermanent filter = new FilterControlledPermanent("Permanents you control with counters on them");

    static {
        filter.add(CounterAnyPredicate.instance);
    }

    public InnkeepersTalent(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{1}{G}");

        this.subtype.add(SubType.CLASS);

        // (Gain the next level as a sorcery to add its ability.)
        this.addAbility(new ClassReminderAbility());

        // At the beginning of combat on your turn, put a +1/+1 counter on target creature you control.
        Ability ability = new BeginningOfCombatTriggeredAbility(new AddCountersTargetEffect(CounterType.P1P1.createInstance()));
        ability.addTarget(new TargetControlledCreaturePermanent());
        this.addAbility(ability);

        // {G}: Level 2
        this.addAbility(new ClassLevelAbility(2, "{G}"));

        // Permanents you control with counters on them have ward {1}.
        this.addAbility(new SimpleStaticAbility(new GainClassAbilitySourceEffect(
                new GainAbilityControlledEffect(new WardAbility(new GenericManaCost(1), false), Duration.WhileOnBattlefield, filter), 2
        )));

        // {3}{G}: Level 3
        this.addAbility(new ClassLevelAbility(3, "{3}{G}"));

        // If you would put one or more counters on a permanent or player, put twice that many of each of those kinds of counters on that permanent or player instead.
        ReplaceCounterEffect replaceCounterEffect = new ReplaceCounterEffect(ReplaceCounterEffect.ModificationType.MULTIPLY, 2)
                .setPermanentFilter(StaticFilters.FILTER_PERMANENT)
                .setTargetPlayers(true)
                .setEventController(TargetController.YOU);
        this.addAbility(new SimpleStaticAbility(new GainClassAbilitySourceEffect(replaceCounterEffect, 3)));
    }

    private InnkeepersTalent(final InnkeepersTalent card) {
        super(card);
    }

    @Override
    public InnkeepersTalent copy() {
        return new InnkeepersTalent(this);
    }
}
