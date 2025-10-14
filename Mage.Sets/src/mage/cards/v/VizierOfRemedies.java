package mage.cards.v;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.replacement.ReplaceCounterEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 * @author Stravant
 */
public final class VizierOfRemedies extends CardImpl {

    public VizierOfRemedies(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.CLERIC);
        this.power = new MageInt(2);
        this.toughness = new MageInt(1);

        // If one or more -1/-1 counters would be put on a creature you control, that many -1/-1 counters minus one are put on it instead.
        addAbility(new SimpleStaticAbility(new ReplaceCounterEffect(ReplaceCounterEffect.ModificationType.SUBTRACT, 1)
                .addValidCounterTypes(CounterType.M1M1)
                .setPermanentFilter(StaticFilters.FILTER_CONTROLLED_CREATURE)
                .setText("If one or more -1/-1 counters would be put on a creature you control, " +
                        "that many -1/-1 counters minus one are put on it instead")
        ));

    }

    private VizierOfRemedies(final VizierOfRemedies card) {
        super(card);
    }

    @Override
    public VizierOfRemedies copy() {
        return new VizierOfRemedies(this);
    }
}
