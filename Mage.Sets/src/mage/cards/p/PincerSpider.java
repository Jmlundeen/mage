
package mage.cards.p;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.KickedCondition;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.KickerAbility;
import mage.abilities.keyword.ReachAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 *
 * @author michael.napoleon@gmail.com
 */
public final class PincerSpider extends CardImpl {

    public PincerSpider(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{2}{G}");
        this.subtype.add(SubType.SPIDER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // Kicker {3}
        this.addAbility(new KickerAbility("{3}"));
        
        // Reach
        this.addAbility(ReachAbility.getInstance());
        
        // If Pincer Spider was kicked, it enters with a +1/+1 counter on it.
        this.addAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance()),
                KickedCondition.ONCE)
                .setText("if {this} was kicked, it enters with a +1/+1 counter on it")
        ));
        
    }

    private PincerSpider(final PincerSpider card) {
        super(card);
    }

    @Override
    public PincerSpider copy() {
        return new PincerSpider(this);
    }
}
