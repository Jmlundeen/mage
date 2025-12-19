
package mage.cards.p;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.KickedCondition;
import mage.abilities.costs.common.PayLifeCost;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.KickerAbility;
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
public final class PhyrexianScuta extends CardImpl {

    public PhyrexianScuta(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{3}{B}");
        this.subtype.add(SubType.PHYREXIAN);
        this.subtype.add(SubType.ZOMBIE);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Kicker-Pay 3 life.
        this.addAbility(new KickerAbility(new PayLifeCost(3)));

        // If Phyrexian Scuta was kicked, it enters with two +1/+1 counters on it.
        this.addAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance(2)),
                KickedCondition.ONCE)
                .setText("if {this} was kicked, it enters with two +1/+1 counters on it")
        ));
    }

    private PhyrexianScuta(final PhyrexianScuta card) {
        super(card);
    }

    @Override
    public PhyrexianScuta copy() {
        return new PhyrexianScuta(this);
    }
}
