package mage.view;

import mage.MageObject;
import mage.cards.Card;
import mage.cards.Cards;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.PermanentCard;
import mage.ws.v1.view.ViewProto;

import java.io.Serializable;

/**
 *
 * @author BetaSteward_at_googlemail.com, nantuko
 */
public class LookedAtView implements Serializable {

    private final String name;
    private final SimpleCardsView cards = new SimpleCardsView();

    public LookedAtView(String name, Cards cards, Game game) {
        this.name = name;
        for (Card card: cards.getCards(game)) {
            if (card instanceof PermanentCard && card.isFaceDown()) {
                MageObject trueCard = ((Permanent) card).getBasicMageObject();
                this.cards.put(card.getId(), new SimpleCardView(trueCard.getId(), trueCard.getExpansionSetCode(), trueCard.getCardNumber(), trueCard.getUsesVariousArt()));
            } else {
                this.cards.put(card.getId(), new SimpleCardView(card.getId(), card.getExpansionSetCode(), card.getCardNumber(), card.getUsesVariousArt()));
            }
        }
    }

    protected LookedAtView(ViewProto.LookedAtView proto) {
        this.name = proto.getName();
        this.cards.putAll(SimpleCardsView.fromProto(proto.getCards()));
    }
    public String getName() {
        return name;
    }

    public SimpleCardsView getCards() {
        return cards;
    }

    public ViewProto.LookedAtView toProto() {
        return ViewProto.LookedAtView.newBuilder()
                .setName(name != null ? name : "")
                .setCards(cards.toProto())
                .build();
    }

    public static LookedAtView fromProto(ViewProto.LookedAtView proto) {
        return new LookedAtView(proto);
    }
}
