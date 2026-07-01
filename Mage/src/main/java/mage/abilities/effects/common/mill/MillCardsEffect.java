package mage.abilities.effects.common.mill;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Cards;
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
 * One-shot effect that mills cards from one or more players' libraries.
 * The players to mill are resolved through the effect's target pointer
 * (if set) or by querying players matching a {@link FilterTyped} filter
 * (defaults to the source controller).
 * <p>
 * Supports optional milling (player may decline) and remembering the
 * milled cards for subsequent linked effects.
 * <p>
 * Configuration is fluent via {@link #setRememberMilledCards}. Constructors
 * accept either an int or {@link DynamicValue} for the mill amount.
 * <p>
 * Typical usage:
 * <pre>{@code
 * new MillCardsEffect(5)                       // mill yourself
 * new MillCardsEffect(3, StaticFilters.OPPONENT)  // mill each opponent
 * }</pre>
 *
 * @author jmlundeen
 */
public class MillCardsEffect extends OneShotEffect {

    /** Number of cards to mill (dynamic). */
    private final DynamicValue numberCards;
    /** Filter that players must match to be eligible to mill. */
    private final FilterTyped filter;
    /** If true, the player may decline to mill. */
    private final boolean optional;
    /** If true, milled cards are stored in a {@link RememberedTargets}. */
    private boolean rememberMilledCards;

    /**
     * Creates an effect that mills {@code numberCards} from the source
     * controller's library (optional is false).
     */
    public MillCardsEffect(int numberCards) {
        this(StaticValue.get(numberCards));
    }

    /**
     * Creates an effect that mills a dynamic number of cards from the
     * source controller's library (optional is false).
     */
    public MillCardsEffect(DynamicValue numberCards) {
        this(numberCards, StaticTypedFilters.YOU, false);
    }

    /**
     * Creates an effect that mills {@code numberCards} from players
     * matching the given filter.
     */
    public MillCardsEffect(int numberCards, FilterTyped filter) {
        this(StaticValue.get(numberCards), filter, false);
    }

    /**
     * Creates an effect that mills a dynamic number of cards from players
     * matching the given filter.
     */
    public MillCardsEffect(DynamicValue numberCards, FilterTyped filter) {
        this(numberCards, filter, false);
    }

    /**
     * Full constructor.
     *
     * @param numberCards dynamic number of cards to mill
     * @param filter      player filter
     * @param optional    if true, player may decline
     */
    public MillCardsEffect(DynamicValue numberCards, FilterTyped filter, boolean optional) {
        super(Outcome.Discard);
        this.numberCards = numberCards;
        this.filter = filter.copy();
        this.optional = optional;
    }

    private MillCardsEffect(final MillCardsEffect effect) {
        super(effect);
        this.numberCards = effect.numberCards;
        this.filter = effect.filter.copy();
        this.optional = effect.optional;
        this.rememberMilledCards = effect.rememberMilledCards;
    }

    @Override
    public MillCardsEffect copy() {
        return new MillCardsEffect(this);
    }

    @Override
    public void applyToObjects(Ability source, Game game, List<MageItem> affectedObjects) {
        int amount = numberCards.calculate(game, source, this);
        for (MageItem mageItem : affectedObjects) {
            Player player = (Player) mageItem;
            if (!optional || player.chooseUse(outcome, String.format("Mill %d cards?", amount), source, game)) {
                Cards milledCards = player.millCards(amount, source, game);
                if (rememberMilledCards) {
                    RememberedTargets rememberedTargets = new RememberedTargets(milledCards, game);
                    source.getEffects().setTargetPointer(rememberedTargets);
                }
            }
        }
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
    public boolean apply(Game game, Ability source) {
        List<MageItem> affectedObjects = new ArrayList<>(source.getAffectedObjects());
        if (queryAffectedObjects(source, game, affectedObjects)) {
            applyToObjects(source, game, affectedObjects);
            return true;
        }
        return false;
    }

    /**
     * If true, all milled cards are stored in a {@link RememberedTargets}
     * on the source effects for use by subsequent linked effects.
     */
    public MillCardsEffect setRememberMilledCards(boolean rememberMilledCards) {
        this.rememberMilledCards = rememberMilledCards;
        return this;
    }
}

