package mage.filter.predicate.typed.card;

import mage.cards.Card;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.game.Game;

public enum CardPredicate implements ICardPredicate{
    instance;

    @Override
    public boolean apply(ObjectSourcePlayer<Card> input, Game game) {
        return input.getObject() != null;
    }
}
