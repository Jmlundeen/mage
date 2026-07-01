package mage.cards.h;

import mage.abilities.dynamicvalue.common.manavalue.CounteredManaValue;
import mage.abilities.effects.common.countered.CounterEffect;
import mage.abilities.effects.keyword.DiscoverEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.FilterTyped;
import mage.filter.StaticTypedFilters;
import mage.filter.predicate.typed.LogicalPredicate;
import mage.filter.predicate.typed.Spell.SpellPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.target.TargetGeneric;

import java.util.UUID;

/**
 * @author Susucr
 */
public final class HurlIntoHistory extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("artifact or creature spell")
            .addAll(SpellPredicate.instance,
                    LogicalPredicate.or(
                    IMageObjectPredicate.getOSPPredicate(CardType.CREATURE.getPredicate()),
                    IMageObjectPredicate.getOSPPredicate(CardType.ARTIFACT.getPredicate())
            ));

    public HurlIntoHistory(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{3}{U}{U}");

        // Counter target artifact or creature spell. Discover X, where X is that spell's mana value.
        this.getSpellAbility().addEffect(new CounterEffect(filter)
                .setText("Counter target artifact or creature spell")
                .setRememberManaValue(true)
        );
        this.getSpellAbility().addEffect(new DiscoverEffect(CounteredManaValue.instance, false)
                .setText("Discover X, where X is that spell's mana value. <i>" +
                        "(Exile cards from the top of your library until you exile a nonland card with that mana value or less. " +
                        "Cast it without paying its mana cost or put it into your hand. Put the rest on the bottom in a random order.)</i>")
        );
        this.getSpellAbility().addTarget(new TargetGeneric(StaticTypedFilters.SPELL));
    }

    private HurlIntoHistory(final HurlIntoHistory card) {
        super(card);
    }

    @Override
    public HurlIntoHistory copy() {
        return new HurlIntoHistory(this);
    }
}
