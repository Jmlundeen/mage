package mage.cards.p;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.KickedCondition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.HasteAbility;
import mage.abilities.keyword.KickerAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ContinuousAffected;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 * @author Backfir3
 */
public final class PouncingWurm extends CardImpl {

    public PouncingWurm(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}");
        this.subtype.add(SubType.WURM);

        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Kicker {2}{G}
        this.addAbility(new KickerAbility("{2}{G}"));

        // If Pouncing Wurm was kicked, it enters with three +1/+1 counters on it and with haste.
        Ability ability = new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance(3)),
                KickedCondition.ONCE)
                .setText("if {this} was kicked, it enters with three +1/+1 counters on it")
        );
        ability.addEffect(new ConditionalContinuousEffect(
                new ContinuousEffectBuilder(Outcome.AddAbility, ContinuousAffected.SOURCE)
                        .withGainedAbilities(HasteAbility.getInstance()),
                KickedCondition.ONCE,
                "and with haste"
        ));
        this.addAbility(ability);
    }

    private PouncingWurm(final PouncingWurm card) {
        super(card);
    }

    @Override
    public PouncingWurm copy() {
        return new PouncingWurm(this);
    }
}
