
package mage.cards.g;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.MultikickerCount;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.MultikickerAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 *
 * @author North
 */
public final class GnarlidPack extends CardImpl {

    public GnarlidPack(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{1}{G}");
        this.subtype.add(SubType.BEAST);

        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Multikicker (You may pay an additional any number of times as you cast this spell.)
        this.addAbility(new MultikickerAbility("{1}{G}"));

        // Gnarlid Pack enters the battlefield with a +1/+1 counter on it for each time it was kicked.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.P1P1, MultikickerCount.instance)));
    }

    private GnarlidPack(final GnarlidPack card) {
        super(card);
    }

    @Override
    public GnarlidPack copy() {
        return new GnarlidPack(this);
    }
}
