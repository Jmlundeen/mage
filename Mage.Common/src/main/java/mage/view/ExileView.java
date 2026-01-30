package mage.view;

import mage.cards.Card;
import mage.game.ExileZone;
import mage.game.Game;
import mage.ws.v1.view.ViewProto;

import java.util.UUID;

/**
 *
 * @author BetaSteward_at_googlemail.com
 */
public class ExileView extends CardsView {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final UUID id;

    public ExileView(ExileZone exileZone, Game game, UUID createdForPlayerId) {
        this.name = exileZone.getName();
        this.id = exileZone.getId();
        for (Card card: exileZone.getCards(game)) {
            this.put(card.getId(), new CardView(card, game, exileZone.isPlayerAllowedToSeeCard(createdForPlayerId, card)));
        }
    }

    private ExileView(ViewProto.ExileView proto) {
        this.id = UUID.fromString(proto.getId());
        this.name = proto.getName();
        proto.getCardsMap().forEach((uuid, cardProto) -> this.put(UUID.fromString(uuid), CardView.fromProto(cardProto)));
    }

    public String getName() {
        return name;
    }

    public UUID getId() {
        return id;
    }

    public ViewProto.ExileView toProto() {
        ViewProto.ExileView.Builder builder = ViewProto.ExileView.newBuilder()
                .setId(id.toString())
                .setName(name);
        this.forEach((uuid, cardView) -> builder.putCards(uuid.toString(), cardView.toCardViewProto()));
        return builder.build();
    }

    public static ExileView fromProto(ViewProto.ExileView proto) {
        return new ExileView(proto);
    }

}
