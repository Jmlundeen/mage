
package mage.cards.p;


import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.KickedCondition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.FirstStrikeAbility;
import mage.abilities.keyword.HasteAbility;
import mage.abilities.keyword.KickerAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;

import java.util.UUID;

/**
 *
 * @author LoneFox

 */
public final class PouncingKavu extends CardImpl {

    public PouncingKavu(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{1}{R}");
        this.subtype.add(SubType.KAVU);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // Kicker {2}{R}
        this.addAbility(new KickerAbility("{2}{R}"));

        // First strike
        this.addAbility(FirstStrikeAbility.getInstance());

        // If Pouncing Kavu was kicked, it enters with two +1/+1 counters on it and with haste.
        Ability ability = new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance(2)),
                KickedCondition.ONCE)
                .setText("if {this} was kicked, it enters with two +1/+1 counters on it")
        );
        ability.addEffect(new ConditionalContinuousEffect(
                new ContinuousEffectBuilder(Outcome.AddAbility, ContinuousAffected.SOURCE)
                .withGainedAbilities(HasteAbility.getInstance()),
                KickedCondition.ONCE,
                "and with haste"
        ));
        this.addAbility(ability);
    }

    private PouncingKavu(final PouncingKavu card) {
        super(card);
    }

    @Override
    public PouncingKavu copy() {
        return new PouncingKavu(this);
    }
}
