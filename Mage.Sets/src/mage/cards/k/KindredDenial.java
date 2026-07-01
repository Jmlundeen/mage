package mage.cards.k;

import mage.abilities.dynamicvalue.common.manavalue.CounteredManaValue;
import mage.abilities.effects.common.SeekCardEffect;
import mage.abilities.effects.common.countered.CounterEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ComparisonType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.card.CardPredicate;
import mage.filter.predicate.typed.mageObject.value.ManaValuePredicate;
import mage.target.TargetSpell;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class KindredDenial extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("a card with the same mana value as that spell")
            .addAll(
                    CardPredicate.instance,
                    new ManaValuePredicate(ComparisonType.EQUAL_TO, CounteredManaValue.instance)
            );

    public KindredDenial(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{2}{U}{U}");

        // Counter target spell. Seek a card with the same mana value as that spell.
        this.getSpellAbility().addEffect(new CounterEffect()
                .setText("counter target spell")
                .setRememberManaValue(true)
        );
        this.getSpellAbility().addEffect(new SeekCardEffect(filter));
        this.getSpellAbility().addTarget(new TargetSpell());
    }

    private KindredDenial(final KindredDenial card) {
        super(card);
    }

    @Override
    public KindredDenial copy() {
        return new KindredDenial(this);
    }
}
