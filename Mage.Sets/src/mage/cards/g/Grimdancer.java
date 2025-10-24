package mage.cards.g;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class Grimdancer extends CardImpl {

    public Grimdancer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{B}{B}");

        this.subtype.add(SubType.NIGHTMARE);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Grimdancer enters the battlefield with your choice of two different counters on it from among menace, deathtouch, and lifelink.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.MENACE, StaticValue.get(1))
                .withAdditionalCounters(CounterType.DEATHTOUCH)
                .withAdditionalCounters(CounterType.LIFELINK)
                .withChooseCounter(2)));
    }

    private Grimdancer(final Grimdancer card) {
        super(card);
    }

    @Override
    public Grimdancer copy() {
        return new Grimdancer(this);
    }
}
