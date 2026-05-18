package mage.abilities.effects.common.reveal;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.Outcome;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.FilterCard;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetCardInHand;

public class RevealEffect extends OneShotEffect {

    private boolean rememberRevealed = false;
    private TargetController targetController = TargetController.YOU;
    private Zone revealFromZone = Zone.HAND;
    private FilterCard filter = new FilterCard();
    private int maxCardsToReveal = Integer.MAX_VALUE;
    private int minCardsToReveal = Integer.MAX_VALUE;

    public RevealEffect(Outcome outcome) {
        super(outcome);
    }

    public RevealEffect(final RevealEffect effect) {
        super(effect);
        this.rememberRevealed = effect.rememberRevealed;
        this.targetController = effect.targetController;
        this.revealFromZone = effect.revealFromZone;
        this.filter = effect.filter.copy();
        this.maxCardsToReveal = effect.maxCardsToReveal;
        this.minCardsToReveal = effect.minCardsToReveal;
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player revealer;
        if (targetController == TargetController.YOU) {
            revealer = game.getPlayer(source.getControllerId());
        } else {
            throw new UnsupportedOperationException("TargetController " + targetController + " not supported in reveal effect");
        }
        if (revealer == null) {
            return false;
        }
        Cards cardsToReveal;
        if (minCardsToReveal == Integer.MAX_VALUE) {
            if (revealFromZone == Zone.HAND) {
                cardsToReveal = new CardsImpl(revealer.getHand().getCards(filter, revealer.getId(), source, game));
            } else {
                throw new UnsupportedOperationException("Zone " + revealFromZone + " not supported in reveal effect");
            }
            if (cardsToReveal.isEmpty()) {
                return false;
            }
        } else {
            cardsToReveal = chooseCardsToReveal(revealer, game, source);
            if (cardsToReveal.isEmpty()) {
                return minCardsToReveal == 0;
            }
        }

        revealer.revealCards(source, cardsToReveal, game);
        if (rememberRevealed) {
            source.getEffects().setValue("revealedCards", cardsToReveal);
        }
        return true;
    }

    @Override
    public OneShotEffect copy() {
        return new RevealEffect(this);
    }

    public RevealEffect rememberRevealed() {
        this.rememberRevealed = true;
        return this;
    }

    public RevealEffect setTargetController(TargetController targetController) {
        this.targetController = targetController;
        return this;
    }

    public RevealEffect setRevealFromZone(Zone revealFromZone) {
        this.revealFromZone = revealFromZone;
        return this;
    }

    public RevealEffect setFilter(FilterCard filter) {
        this.filter = filter.copy();
        return this;
    }

    public RevealEffect setMinCardsToReveal(int minCardsToReveal) {
        this.minCardsToReveal = minCardsToReveal;
        return this;
    }

    public RevealEffect setMaxCardsToReveal(int maxCardsToReveal) {
        this.minCardsToReveal = maxCardsToReveal;
        this.maxCardsToReveal = maxCardsToReveal;
        return this;
    }

    public RevealEffect setMinMaxCardsToReveal(int minCardsToReveal, int maxCardsToReveal) {
        this.minCardsToReveal = minCardsToReveal;
        this.maxCardsToReveal = maxCardsToReveal;
        return this;
    }

    private Cards chooseCardsToReveal(Player revealer, Game game, Ability source) {
        if (revealFromZone == Zone.HAND) {
            return chooseCardsFromHand(revealer, game, source);
        }
        throw new UnsupportedOperationException("Zone " + revealFromZone + " not supported in reveal effect");
    }

    private Cards chooseCardsFromHand(Player revealer, Game game, Ability source) {
        TargetCardInHand target = new TargetCardInHand(minCardsToReveal, maxCardsToReveal, filter);
        if (!target.choose(outcome, revealer.getId(), source, game)) {
            return new CardsImpl();
        }
        return new CardsImpl(target.getTargets());
    }
}
