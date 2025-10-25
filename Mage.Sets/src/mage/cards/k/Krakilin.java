
package mage.cards.k;

import mage.MageInt;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.dynamicvalue.common.SourceXCostValue;
import mage.abilities.effects.common.RegenerateSourceEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 *
 * @author fireshoes
 */
public final class Krakilin extends CardImpl {

    public Krakilin(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{X}{G}{G}");
        this.subtype.add(SubType.BEAST);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Krakilin enters the battlefield with X +1/+1 counters on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.P1P1, SourceXCostValue.instance)));

        // {1}{G}: Regenerate Krakilin.
        this.addAbility(new SimpleActivatedAbility(new RegenerateSourceEffect(), new ManaCostsImpl<>("{1}{G}")));
    }

    private Krakilin(final Krakilin card) {
        super(card);
    }

    @Override
    public Krakilin copy() {
        return new Krakilin(this);
    }
}
