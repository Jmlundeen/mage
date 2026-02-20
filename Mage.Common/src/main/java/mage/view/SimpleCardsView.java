

package mage.view;

import mage.cards.Card;
import mage.ws.view.ViewProto;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 *
 * @author BetaSteward_at_googlemail.com
 */
public class SimpleCardsView extends LinkedHashMap<UUID, SimpleCardView> {

    public SimpleCardsView() {}

    public SimpleCardsView(Collection<Card> cards, boolean isGameObject) {
        if (cards == null) {
            return;
        }
        for (Card card: cards) {
            this.put(card.getId(), new SimpleCardView(card.getId(), card.getExpansionSetCode(), card.getCardNumber(), card.getUsesVariousArt(), isGameObject));
        }
    }

    public ViewProto.SimpleCardsView toProto() {
        ViewProto.SimpleCardsView.Builder builder = ViewProto.SimpleCardsView.newBuilder();
        for (SimpleCardView card : this.values()) {
            builder.addCards(card.toProto());
        }
        return builder.build();
    }

    public static SimpleCardsView fromProto(ViewProto.SimpleCardsView proto) {
        SimpleCardsView view = new SimpleCardsView();
        for (ViewProto.SimpleCardView cardProto : proto.getCardsList()) {
            SimpleCardView card = SimpleCardView.fromProto(cardProto);
            view.put(card.getId(), card);
        }
        return view;
    }
}
