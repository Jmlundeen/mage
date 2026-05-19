package mage.abilities.dynamicvalue.common;

import mage.abilities.Ability;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.cards.Cards;
import mage.constants.Zone;
import mage.filter.FilterCard;
import mage.game.Game;

public class RevealedCardsAmount implements DynamicValue {

    FilterCard filter = new FilterCard();
    Zone revealedFromZone = Zone.HAND;

    public RevealedCardsAmount() {
    }

    public RevealedCardsAmount(final RevealedCardsAmount other) {
        this.filter = other.filter.copy();
        this.revealedFromZone = other.revealedFromZone;
    }

    public RevealedCardsAmount(FilterCard filter) {
        this.filter = filter;
    }

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        Cards revealedCards = (Cards) effect.getValue("revealedCards");
        int amount = revealedCards != null ? revealedCards.size() : 0;
        if (amount == 0 && game.inCheckPlayableState()) {
            amount = switch (revealedFromZone) {
                case HAND -> game.getPlayer(sourceAbility.getControllerId()).getHand().count(filter, sourceAbility.getControllerId(), sourceAbility, game);
                case LIBRARY -> game.getPlayer(sourceAbility.getControllerId()).getLibrary().count(filter, game);
                default -> throw new IllegalArgumentException("Unsupported revealed from zone: " + revealedFromZone);
            };
        }
        return amount;
    }

    @Override
    public DynamicValue copy() {
        return new RevealedCardsAmount(this);
    }

    @Override
    public String getMessage() {
        return "cards revealed this way";
    }


    public RevealedCardsAmount setFilter(FilterCard filter) {
        this.filter = filter;
        return this;
    }

    public RevealedCardsAmount setRevealedFromZone(Zone revealedFromZone) {
        this.revealedFromZone = revealedFromZone;
        return this;
    }
}
