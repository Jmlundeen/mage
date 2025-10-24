
package mage.cards.f;


import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.KickedCondition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.FlyingAbility;
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
public final class FaerieSquadron extends CardImpl {

    public FaerieSquadron(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{U}");
        this.subtype.add(SubType.FAERIE);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // Kicker {3}{U}
        this.addAbility(new KickerAbility("{3}{U}"));

        // If Faerie Squadron was kicked, it enters with two +1/+1 counters on it and with flying.
        Ability ability = new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance(2)),
                KickedCondition.ONCE)
                .setText("If {this} was kicked, it enters with two +1/+1 counters on it and with flying."));
        ability.addEffect(new ConditionalContinuousEffect(
                new ContinuousEffectBuilder(Outcome.AddAbility, ContinuousAffected.SOURCE)
                        .withGainedAbilities(FlyingAbility.getInstance()),
                KickedCondition.ONCE,"")
        );
        this.addAbility(ability);
    }

    private FaerieSquadron(final FaerieSquadron card) {
        super(card);
    }

    @Override
    public FaerieSquadron copy() {
        return new FaerieSquadron(this);
    }
}
