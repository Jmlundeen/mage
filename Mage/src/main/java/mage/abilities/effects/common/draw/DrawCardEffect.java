package mage.abilities.effects.common.draw;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.Outcome;
import mage.filter.FilterTyped;
import mage.filter.StaticTypedFilters;
import mage.game.Game;
import mage.players.Player;
import mage.target.targetpointer.RememberedTargets;
import mage.util.ObjectQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * One-shot effect that makes one or more players draw cards. The players
 * to draw are resolved through the effect's target pointer (if set) or
 * by querying players matching a {@link FilterTyped} filter (defaults to
 * the source controller).
 *
 * @author jmlundeen
 */
public class DrawCardEffect extends OneShotEffect {

    /** Number of cards each player draws. */
    private final DynamicValue amount;
    /** Filter that players must match to be eligible to draw. */
    private final FilterTyped filter;
    /** If true, the player may decline to draw. */
    private final boolean optional;
    /** If true, the player chooses how many to draw up to the calculated amount. */
    private final boolean upTo;
    /** If true, drawn cards are stored in a {@link RememberedTargets}. */
    private boolean rememberDrawnCards;

    /**
     * Creates an effect that draws {@code amount} cards for the source
     * controller (optional and upTo are false).
     */
    public DrawCardEffect(int amount) {
        this(StaticValue.get(amount));
    }

    /**
     * Creates an effect that draws a dynamic amount of cards for the
     * source controller.
     */
    public DrawCardEffect(DynamicValue amount) {
        this(amount, StaticTypedFilters.YOU, false, false);
    }

    /**
     * Creates an effect that draws {@code amount} cards for players
     * matching the given filter.
     */
    public DrawCardEffect(int amount, FilterTyped filter) {
        this(StaticValue.get(amount), filter, false, false);
    }

    /**
     * Creates an effect that draws a dynamic amount of cards for players
     * matching the given filter.
     */
    public DrawCardEffect(DynamicValue amount, FilterTyped filter) {
        this(amount, filter, false, false);
    }

    /**
     * Full constructor
     *
     * @param amount   dynamic number of cards to draw
     * @param filter   player filter
     * @param optional if true, player may decline
     * @param upTo     if true, player chooses 0..amount
     */
    public DrawCardEffect(DynamicValue amount, FilterTyped filter, boolean optional, boolean upTo) {
        super(Outcome.DrawCard);
        this.amount = amount.copy();
        this.filter = filter;
        this.optional = optional;
        this.upTo = upTo;
    }

    private DrawCardEffect(final DrawCardEffect effect) {
        super(effect);
        this.amount = effect.amount.copy();
        this.filter = effect.filter.copy();
        this.optional = effect.optional;
        this.upTo = effect.upTo;
        this.rememberDrawnCards = effect.rememberDrawnCards;
    }

    @Override
    public DrawCardEffect copy() {
        return new DrawCardEffect(this);
    }

    @Override
    public DrawCardEffect setText(String staticText) {
        super.setText(staticText);
        return this;
    }

    @Override
    public boolean apply(Game game, Ability source) {
        List<MageItem> affectedObjects = new ArrayList<>(source.getAffectedObjects());
        if (queryAffectedObjects(source, game, affectedObjects)) {
            applyToObjects(source, game, affectedObjects);
            return true;
        }
        return false;
    }

    @Override
    public boolean queryAffectedObjects(Ability source, Game game, List<MageItem> affectedObjects) {
        for (UUID targetId : getTargetPointer().getTargets(game, source)) {
            Player player = game.getPlayer(targetId);
            if (player != null) {
                affectedObjects.add(player);
            }
        }
        if (affectedObjects.isEmpty()) {
            Player controller = game.getPlayer(source.getControllerId());
            if (controller == null) {
                return false;
            }
            affectedObjects.addAll(ObjectQuery.queryPlayers(game, controller, source, filter));
        }
        return !affectedObjects.isEmpty();
    }

    @Override
    public void applyToObjects(Ability source, Game game, List<MageItem> affectedObjects) {
        int cardsToDraw = amount.calculate(game, source, this);
        Cards drawnCards;
        if (rememberDrawnCards) {
            drawnCards = new CardsImpl();
        } else {
            drawnCards = null;
        }
        for (MageItem mageItem : affectedObjects) {
            Player player = (Player) mageItem;

            if (upTo) {
                cardsToDraw = player.getAmount(0, cardsToDraw, "Draw how many cards?", source, game);
            }
            if (!optional || player.chooseUse(outcome, String.format("Draw %d cards?", cardsToDraw), source, game)) {
                player.drawCards(cardsToDraw, source, game, drawnCards);
            }
            if (drawnCards != null) {
                RememberedTargets rememberedTargets = new RememberedTargets(drawnCards, game);
                source.getEffects().setTargetPointer(rememberedTargets);
            }
        }
    }

    /**
     * If true, all drawn cards are stored in a {@link RememberedTargets}
     * on the source effects for use by subsequent linked effects.
     */
    public DrawCardEffect setRememberDrawnCards(boolean rememberDrawnCards) {
        this.rememberDrawnCards = rememberDrawnCards;
        return this;
    }
}



