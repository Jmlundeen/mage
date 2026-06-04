package mage.filter.predicate.typed.card;

import mage.cards.Card;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.TypedPredicate;

public interface ICardPredicate extends TypedPredicate<ObjectSourcePlayer<Card>> {

    @Override
    default Class<?> getObjectClass() {
        return Card.class;
    }
}
