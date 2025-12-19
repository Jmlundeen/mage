
package mage.cards.k;

import mage.MageInt;
import mage.abilities.common.CantBlockAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.KickedCondition;
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
public final class KavuAggressor extends CardImpl {

    public KavuAggressor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{2}{R}");
        this.subtype.add(SubType.KAVU);
        this.power = new MageInt(3);
        this.toughness = new MageInt(2);

        // Kicker {4}
        this.addAbility(new KickerAbility("{4}"));

        // Kavu Aggressor can't block.
        this.addAbility(new CantBlockAbility());

        // If Kavu Aggressor was kicked, it enters with a +1/+1 counter on it.
        this.addAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance()),
                KickedCondition.ONCE)
                .setText("if {this} was kicked, it enters with a +1/+1 counter on it")
        ));
    }

    private KavuAggressor(final KavuAggressor card) {
        super(card);
    }

    @Override
    public KavuAggressor copy() {
        return new KavuAggressor(this);
    }
}
