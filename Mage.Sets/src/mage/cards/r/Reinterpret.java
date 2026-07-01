package mage.cards.r;

import mage.abilities.dynamicvalue.common.manavalue.CounteredManaValue;
import mage.abilities.effects.common.cast.PlayEffect;
import mage.abilities.effects.common.countered.CounterEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ComparisonType;
import mage.constants.Zone;
import mage.filter.FilterTyped;
import mage.filter.StaticTypedFilters;
import mage.filter.predicate.typed.card.CardPredicate;
import mage.filter.predicate.typed.mageObject.value.ManaValuePredicate;
import mage.target.TargetGeneric;

import java.util.Set;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class Reinterpret extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("card with equal or lesser value than countered spell")
            .addAll(
                    CardPredicate.instance,
                    new ManaValuePredicate(ComparisonType.OR_LESS, CounteredManaValue.instance)
            );

    public Reinterpret(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{2}{U}{R}");

        // Counter target spell. You may cast a spell with an equal or lesser mana value from your hand without paying its mana cost.
        this.getSpellAbility().addEffect(new CounterEffect()
                .setText("counter target spell")
                .setRememberManaValue(true)
        );
        this.getSpellAbility().addEffect(new PlayEffect(filter, Set.of(Zone.HAND)));
        this.getSpellAbility().addTarget(new TargetGeneric(StaticTypedFilters.SPELL));
    }

    private Reinterpret(final Reinterpret card) {
        super(card);
    }

    @Override
    public Reinterpret copy() {
        return new Reinterpret(this);
    }
}
