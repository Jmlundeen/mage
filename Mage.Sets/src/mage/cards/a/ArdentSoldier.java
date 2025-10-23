
package mage.cards.a;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.KickedCondition;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.KickerAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 *
 * @author LoneFox

 */
public final class ArdentSoldier extends CardImpl {

    public ArdentSoldier(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{1}{W}");
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.SOLDIER);
        this.power = new MageInt(1);
        this.toughness = new MageInt(2);

        // Kicker {2}
        this.addAbility(new KickerAbility("{2}"));
        // Vigilance
        this.addAbility(VigilanceAbility.getInstance());
        // If Ardent Soldier was kicked, it enters with a +1/+1 counter on it.
        this.addAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance()),
                KickedCondition.ONCE)
                .setText("If {this} was kicked, it enters with a +1/+1 counter on it.")
        ));
    }

    private ArdentSoldier(final ArdentSoldier card) {
        super(card);
    }

    @Override
    public ArdentSoldier copy() {
        return new ArdentSoldier(this);
    }
}
