package mage.cards.l;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.replacement.ReplaceCounterEffect;
import mage.abilities.keyword.WarpAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.predicate.Predicates;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class LoadingZone extends CardImpl {

    private static final FilterPermanent filter = new FilterControlledPermanent("creature, Spacecraft, or Planet you control");

    static {
        filter.add(Predicates.or(
                CardType.CREATURE.getPredicate(),
                SubType.SPACECRAFT.getPredicate(),
                SubType.PLANET.getPredicate()
        ));
    }

    public LoadingZone(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{G}");

        // If one or more counters would be put on a creature, Spacecraft, or Planet you control, twice that many of each of those kinds of counters are put on it instead.
        this.addAbility(new SimpleStaticAbility(new ReplaceCounterEffect(ReplaceCounterEffect.ModificationType.MULTIPLY, 2)
                .setPermanentFilter(filter)
                .setText("if one or more counters would be put on a creature, Spacecraft, or Planet you control, " +
                        "twice that many of each of those kinds of counters are put on it instead")
        ));

        // Warp {G}
        this.addAbility(new WarpAbility(this, "{G}"));
    }

    private LoadingZone(final LoadingZone card) {
        super(card);
    }

    @Override
    public LoadingZone copy() {
        return new LoadingZone(this);
    }
}
