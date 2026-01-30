package mage.view;

import mage.cards.Card;
import mage.cards.Cards;
import mage.game.Game;
import mage.game.permanent.PermanentCard;
import mage.ws.v1.view.ViewProto;

import java.io.Serializable;
import java.util.UUID;

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

    private RevealedView(ViewProto.RevealedView proto) {
        this.name = proto.getName();
        proto.getCardsMap().forEach((uuid, cardProto) -> this.cards.put(UUID.fromString(uuid), CardView.fromProto(cardProto)));
    }

    public String getName() {
        return name;
    }

    public CardsView getCards() {
        return cards;
    }

    public ViewProto.RevealedView toProto() {
        ViewProto.RevealedView.Builder builder = ViewProto.RevealedView.newBuilder()
                .setName(name != null ? name : "");
        cards.forEach((uuid, cardView) -> builder.putCards(uuid.toString(), cardView.toCardViewProto()));
        return builder.build();
    }

    public static RevealedView fromProto(ViewProto.RevealedView proto) {
        return new RevealedView(proto);
    }
}
