package mage.filter.predicate.typed.card;

import mage.cards.Card;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.Predicate;
import mage.filter.predicate.TypedPredicate;
import mage.game.Game;

public interface ICardPredicate extends TypedPredicate<Card> {

    @Override
    default Class<Card> getObjectClass() {
        return Card.class;
    }

    static TypedPredicate<Card> getOSPPredicate(Predicate<Card> predicate) {
        return new TypedPredicate<Card>() {
            @Override
            public Class<Card> getObjectClass() {
                return Card.class;
            }

            @Override
            public boolean apply(ObjectSourcePlayer<Card> input, Game game) {
                return predicate.apply(input.getObject(), game);
            }
        };
    }
}
