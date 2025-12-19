package mage.cards.m;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.mana.AnyColorManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 *
 * @author jeffwadsworth
 */
public final class Morselhoarder extends CardImpl {

    public Morselhoarder(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{R/G}{R/G}");
        this.subtype.add(SubType.ELEMENTAL);
        this.power = new MageInt(6);
        this.toughness = new MageInt(4);

        // Morselhoarder enters the battlefield with two -1/-1 counters on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.M1M1.createInstance(2))));

        // Remove a -1/-1 counter from Morselhoarder: Add one mana of any color.
        this.addAbility(new AnyColorManaAbility(new RemoveCountersSourceCost(CounterType.M1M1.createInstance()), new CountersSourceCount(CounterType.M1M1), false));

    }

    private Morselhoarder(final Morselhoarder card) {
        super(card);
    }

    @Override
    public Morselhoarder copy() {
        return new Morselhoarder(this);
    }
}
