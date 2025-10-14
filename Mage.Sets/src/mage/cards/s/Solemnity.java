package mage.cards.s;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.replacement.ReplaceCounterEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.FilterPermanent;
import mage.filter.predicate.Predicates;

import java.util.UUID;

/**
 * @author spjspj
 */
public final class Solemnity extends CardImpl {

    private static final FilterPermanent filter = new FilterPermanent();

    static {
        filter.add(Predicates.or(
                CardType.ARTIFACT.getPredicate(),
                CardType.CREATURE.getPredicate(),
                CardType.ENCHANTMENT.getPredicate(),
                CardType.LAND.getPredicate()));
    }

    public Solemnity(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{W}");

        // Players can't get counters.
        this.addAbility(new SimpleStaticAbility(new ReplaceCounterEffect(ReplaceCounterEffect.ModificationType.PREVENT)
                .setTargetPlayers(true)
                .setText("Players can't get counters")
        ));

        // Counters can't be put on artifacts, creatures, enchantments, or lands.
        this.addAbility(new SimpleStaticAbility(new ReplaceCounterEffect(ReplaceCounterEffect.ModificationType.PREVENT)
                .setPermanentFilter(filter)
                .setText("Counters can't be put on artifacts, creatures, enchantments, or lands")
        ));
    }

    private Solemnity(final Solemnity card) {
        super(card);
    }

    @Override
    public Solemnity copy() {
        return new Solemnity(this);
    }
}
