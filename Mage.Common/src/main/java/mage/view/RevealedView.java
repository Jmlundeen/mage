

package mage.view;

import mage.cards.Card;
import mage.cards.Cards;
import mage.game.Game;
import mage.game.permanent.PermanentCard;

import java.io.Serializable;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class RevealedView implements Serializable {

    private final String name;
    private final CardsView cards = new CardsView();

    public RevealedView(String name, Cards cards, Game game) {
        this.name = name;
        for (Card card : cards.getCards(game)) {
            if (card instanceof PermanentCard && card.isFaceDown()) {
                this.cards.put(card.getId(), new CardView(card.getMainCard())); // do not use game param, so it will take default card
            } else {
                this.cards.put(card.getId(), new CardView(card, game));
            }
        }
    }

    public String getName() {
        return name;
    }

    public CardsView getCards() {
        return cards;
    }
}
