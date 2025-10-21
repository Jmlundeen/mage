
package mage.cards.d;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldAbility;
import mage.abilities.condition.common.KickedCondition;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.FearAbility;
import mage.abilities.keyword.KickerAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;

import java.util.UUID;

/**
 *
 * @author michael.napoleon@gmail.com
 */
public final class Duskwalker extends CardImpl {

    public Duskwalker(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{B}");
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.MINION);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // Kicker {3}{B}
        this.addAbility(new KickerAbility("{3}{B}"));
        
        // If Duskwalker was kicked, it enters with two +1/+1 counters on it and with fear.
        Ability ability = new EntersBattlefieldAbility(new EntersWithCountersEffect(CounterType.P1P1.createInstance(2)),
                KickedCondition.ONCE,
                "If {this} was kicked, it enters with two +1/+1 counters on it and with fear.", "");
        ability.addEffect(new ContinuousEffectBuilder(Duration.WhileOnBattlefield, Outcome.AddAbility, ContinuousAffected.SOURCE)
                .withGainedAbilities(FearAbility.getInstance()));
        this.addAbility(ability);
    }

    private Duskwalker(final Duskwalker card) {
        super(card);
    }

    @Override
    public Duskwalker copy() {
        return new Duskwalker(this);
    }
}
