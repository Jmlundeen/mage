package mage.cards.decks;

import mage.util.CardUtil;
import mage.util.Copyable;
import mage.ws.model.ModelProto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Client side deck with text only.
 * <p>
 * Can contain restricted, un-implemented or unknown cards
 *
 * @author BetaSteward_at_googlemail.com, JayDi85
 */
public class DeckCardLists implements Serializable, Copyable<DeckCardLists> {

    private String name = null;
    private String author = null;
    private List<DeckCardInfo> cards = new ArrayList<>();
    private List<DeckCardInfo> sideboard = new ArrayList<>();

    // Layout (if supported)
    private DeckCardLayout cardLayout = null;
    private DeckCardLayout sideboardLayout = null;

    public DeckCardLists() {
    }

    protected DeckCardLists(final DeckCardLists deck) {
        this.name = deck.name;
        this.author = deck.author;
        this.cards = CardUtil.deepCopyObject(deck.cards);
        this.sideboard = CardUtil.deepCopyObject(deck.sideboard);
        this.cardLayout = CardUtil.deepCopyObject(deck.cardLayout);
        this.sideboardLayout = CardUtil.deepCopyObject(deck.sideboardLayout);
    }

    @Override
    public DeckCardLists copy() {
        return new DeckCardLists(this);
    }

    /**
     * @return The layout of the cards
     */
    public DeckCardLayout getCardLayout() {
        return cardLayout;
    }

    public void setCardLayout(DeckCardLayout layout) {
        this.cardLayout = layout;
    }

    public DeckCardLayout getSideboardLayout() {
        return sideboardLayout;
    }

    public void setSideboardLayout(DeckCardLayout layout) {
        this.sideboardLayout = layout;
    }

    /**
     * @return the cards
     */
    public List<DeckCardInfo> getCards() {
        return cards;
    }

    /**
     * @param cards the cards to set
     */
    public void setCards(List<DeckCardInfo> cards) {
        this.cards = cards;
    }

    /**
     * @return the sideboard
     */
    public List<DeckCardInfo> getSideboard() {
        return sideboard;
    }

    /**
     * @param sideboard the sideboard to set
     */
    public void setSideboard(List<DeckCardInfo> sideboard) {
        this.sideboard = sideboard;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public ModelProto.DeckCardLists toProto() {
        ModelProto.DeckCardLists.Builder builder = ModelProto.DeckCardLists.newBuilder()
                .setName(this.name != null ? this.name : "")
                .setAuthor(this.author != null ? this.author : "");

        // Add main deck cards
        for (DeckCardInfo card : this.cards) {
            builder.addCards(card.toProto());
        }

        // Add sideboard cards
        for (DeckCardInfo card : this.sideboard) {
            builder.addSideboard(card.toProto());
        }

        // Add card layout if present
        if (this.cardLayout != null) {
            builder.setCardLayout(this.cardLayout.toProto());
        }

        // Add sideboard layout if present
        if (this.sideboardLayout != null) {
            builder.setSideboardLayout(this.sideboardLayout.toProto());
        }

        return builder.build();
    }

    public static DeckCardLists fromProto(ModelProto.DeckCardLists proto) {
        DeckCardLists deckCardLists = new DeckCardLists();
        deckCardLists.name = proto.getName().isEmpty() ? null : proto.getName();
        deckCardLists.author = proto.getAuthor().isEmpty() ? null : proto.getAuthor();

        // Convert main deck cards
        List<DeckCardInfo> cards = new ArrayList<>();
        for (ModelProto.DeckCardInfo cardProto : proto.getCardsList()) {
            cards.add(DeckCardInfo.fromProto(cardProto));
        }
        deckCardLists.cards = cards;

        // Convert sideboard cards
        List<DeckCardInfo> sideboard = new ArrayList<>();
        for (ModelProto.DeckCardInfo cardProto : proto.getSideboardList()) {
            sideboard.add(DeckCardInfo.fromProto(cardProto));
        }
        deckCardLists.sideboard = sideboard;

        // Convert card layout if present
        if (proto.hasCardLayout()) {
            deckCardLists.cardLayout = DeckCardLayout.fromProto(proto.getCardLayout());
        }

        // Convert sideboard layout if present
        if (proto.hasSideboardLayout()) {
            deckCardLists.sideboardLayout = DeckCardLayout.fromProto(proto.getSideboardLayout());
        }

        return deckCardLists;
    }
}
