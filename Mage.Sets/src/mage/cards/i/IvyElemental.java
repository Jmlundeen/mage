
package mage.cards.i;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.SourceXCostValue;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 *
 * @author cbt33
 */
public final class IvyElemental extends CardImpl {

    public IvyElemental(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{X}{G}");
        this.subtype.add(SubType.ELEMENTAL);

        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Ivy Elemental enters the battlefield with X +1/+1 counters on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.P1P1, SourceXCostValue.instance)));
    }

    private IvyElemental(final IvyElemental card) {
        super(card);
    }

    @Override
    public IvyElemental copy() {
        return new IvyElemental(this);
    }
}
