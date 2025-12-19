package mage.abilities.effects.common.replacement;

import mage.abilities.Ability;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.TargetController;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Replacement effect for modifying counter placement on permanents or players.
 * <p>
 * This effect can handle various modifications such as:
 * <br> - Multiplying counter amounts (e.g., Doubling Season, Vorinclex)
 * <br> - Dividing counter amounts (e.g., halving for opponents)
 * <br> - Adding extra counters (e.g., Winding Constrictor)
 * <br> - Subtracting counters (e.g., Vizier of Remedies)
 * <br> - Setting counter amounts to a specific value (e.g., Melira)
 * <br> - Replacing counter types (e.g., replacing -1/-1 counters with +1/+1 counters)
 * <p>
 * @author Jmlundeen
 */
public class ReplaceCounterEffect extends ReplacementEffectImpl {

    /**
     * Enum representing the types of modifications that can be applied to counter placement.
     */
    public enum ModificationType {
        /**
         * Multiplies the counter amount (e.g., Doubling Season doubles counters)
         */
        MULTIPLY,

        /**
         * Divides the counter amount, rounded down (e.g., Vorinclex halves opponent counters)
         */
        DIVIDE,

        /**
         * Adds additional counters to the amount (e.g., Winding Constrictor adds 1)
         */
        ADD,

        /**
         * Subtracts from the counter amount, minimum 0 (e.g., Vizier of Remedies subtracts 1)
         */
        SUBTRACT,

        /**
         * Sets the counter amount to a specific value (e.g., Melira sets poison counters to 1)
         */
        SET,

        /**
         * Replaces one counter type with another (e.g., replacing -1/-1 with +1/+1)
         */
        REPLACE,

        /**
         * Prevents specific counters from being placed entirely (e.g., Solemnity)
         */
        PREVENT
    }

    /**
     * Functional interface for defining conditions on counter placement.
     */
    public interface CounterCondition {
        boolean apply(GameEvent event, Game game, Ability source);
    }

    // Core configuration
    protected final ModificationType modificationType;
    protected final int factor;

    // Counter type filters
    protected Set<CounterType> validCounterTypes = new HashSet<>();
    protected CounterType replacementCounterType;

    // Target filters
    protected TargetController eventController = TargetController.ANY;
    protected FilterPermanent permanentFilter;
    protected TargetController validPlayerTarget = TargetController.ANY;
    protected boolean targetPlayers = false;
    protected boolean targetPermanents = false;

    // Optional features
    protected boolean optional = false;
    protected CounterCondition condition;
    protected boolean onlyFromCosts = false;
    protected boolean onlyFromEffects = false;

    /**
     * Creates a counter replacement effect with the specified modification type and factor.
     *
     * @param modificationType the type of modification (MULTIPLY, ADD, etc.)
     * @param factor          the amount to multiply, divide, add, or subtract
     */
    public ReplaceCounterEffect(ModificationType modificationType, int factor) {
        this(Duration.WhileOnBattlefield, Outcome.Benefit, modificationType, factor, false);
    }

    /**
     * Creates a counter replacement effect with the specified modification type, factor, and self-scope.
     *
     * @param modificationType the type of modification (MULTIPLY, ADD, etc.)
     * @param factor          the amount to multiply, divide, add, or subtract
     * @param selfScope       true if the effect should apply to itself as it's entering
     */
    public ReplaceCounterEffect(ModificationType modificationType, int factor, boolean selfScope) {
        this(Duration.WhileOnBattlefield, Outcome.Benefit, modificationType, factor, selfScope);
    }

    /**
     * Creates a counter replacement effect for REPLACE types (no factor needed).
     *
     * @param modificationType the type of modification (should be REPLACE)
     */
    public ReplaceCounterEffect(ModificationType modificationType) {
        this(Duration.WhileOnBattlefield, Outcome.Benefit, modificationType, 0, false);
    }

    /**
     * constructor with duration and outcome specification. Non-self scope.
     *
     * @param duration         how long the effect lasts
     * @param outcome          the outcome type
     * @param modificationType the type of modification
     * @param factor          the amount for the
     */
    public ReplaceCounterEffect(Duration duration, Outcome outcome, ModificationType modificationType, int factor) {
        this(duration, outcome, modificationType, factor, false);
    }

    /**
     * Full constructor with duration and outcome specification.
     *
     * @param duration         how long the effect lasts
     * @param outcome          the outcome type
     * @param modificationType the type of modification
     * @param factor          the amount for the
     * @param selfScope       true if the effect should apply to itself as it's entering
     */
    public ReplaceCounterEffect(Duration duration, Outcome outcome, ModificationType modificationType, int factor, boolean selfScope) {
        super(duration, outcome, selfScope);
        this.modificationType = modificationType;
        this.factor = factor;
    }

    protected ReplaceCounterEffect(final ReplaceCounterEffect effect) {
        super(effect);
        this.modificationType = effect.modificationType;
        this.factor = effect.factor;
        this.validCounterTypes = new HashSet<>(effect.validCounterTypes);
        this.replacementCounterType = effect.replacementCounterType;
        this.eventController = effect.eventController;
        this.permanentFilter = effect.permanentFilter != null ? effect.permanentFilter.copy() : null;
        this.validPlayerTarget = effect.validPlayerTarget;
        this.targetPlayers = effect.targetPlayers;
        this.targetPermanents = effect.targetPermanents;
        this.optional = effect.optional;
        this.condition = effect.condition;
        this.onlyFromCosts = effect.onlyFromCosts;
        this.onlyFromEffects = effect.onlyFromEffects;
    }

    @Override
    public ReplaceCounterEffect copy() {
        return new ReplaceCounterEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ADD_COUNTERS;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        if (event.getAmount() <= 0) {
            return false; // No counters being added
        }
        // Check if event matches cost/effect filter
        if (onlyFromCosts && event.getFlag()) {
            return false; // Flag true means it's from an effect
        }
        if (onlyFromEffects && !event.getFlag()) {
            return false; // Flag false means it's from a cost
        }

        // Check if the counter type matches our filter
        if (!validCounterTypes.isEmpty()) {
            String counterName = event.getData();
            if (counterName == null || !isValidCounterType(counterName)) {
                return false;
            }
        }

        // Check target (permanent or player)
        Player targetPlayer = game.getPlayer(event.getTargetId());
        Permanent targetPermanent = game.getPermanentEntering(event.getTargetId());
        if (targetPermanent == null) {
            targetPermanent = game.getPermanent(event.getTargetId());
        }

        boolean hasValidTarget = false;
        if (targetPlayer != null && targetPlayers) {
            switch (validPlayerTarget) {
                case YOU:
                    if (targetPlayer.getId().equals(source.getControllerId())) {
                        hasValidTarget = true;
                    }
                    break;
                case OPPONENT:
                    Player controller = game.getPlayer(source.getControllerId());
                    if (controller != null && controller.hasOpponent(targetPlayer.getId(), game)) {
                        hasValidTarget = true;
                    }
                    break;
                case ANY:
                    hasValidTarget = true;
                    break;
                default:
                    break;
            }
        } else if (targetPermanent != null && targetPermanents) {
            if (permanentFilter != null && !permanentFilter.match(targetPermanent, source.getControllerId(), source, game)) {
                return false;
            }
            hasValidTarget = true;
        }

        if (!hasValidTarget) {
            return false;
        }

        // Check target controller
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }

        boolean controllerMatches = false;
        switch (eventController) {
            case YOU:
                controllerMatches = event.getPlayerId().equals(source.getControllerId());
                break;
            case OPPONENT:
                controllerMatches = controller.hasOpponent(event.getPlayerId(), game);
                break;
            case ANY:
                controllerMatches = true;
                break;
            default:
                break;
        }

        if (!controllerMatches) {
            return false;
        }

        // Check custom condition
        if (condition != null && !condition.apply(event, game, source)) {
            return false;
        }

        // Handle optional choice
        if (optional) {
            Permanent permanent = source.getSourcePermanentIfItStillExists(game);
            if (permanent == null) {
                return false;
            }
            return controller.chooseUse(outcome, "Use " + permanent.getLogName() + " counter replacement?", source, game);
        }

        return true;
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        int currentAmount = event.getAmount();

        // Handle different modification types
        switch (modificationType) {
            case MULTIPLY:
                int multiplied = CardUtil.overflowMultiply(currentAmount, factor);
                event.setAmountForCounters(multiplied, true);
                break;

            case DIVIDE:
                int divided = Math.floorDiv(currentAmount, Math.max(1, factor));
                event.setAmountForCounters(divided, true);
                break;

            case ADD:
                int added = CardUtil.overflowInc(currentAmount, factor);
                event.setAmountForCounters(added, true);
                break;

            case SUBTRACT:
                int subtracted = Math.max(0, currentAmount - factor);
                event.setAmountForCounters(subtracted, true);
                break;

            case SET:
                event.setAmountForCounters(factor, true);
                break;

            case REPLACE:
                // Replace the counter type
                if (replacementCounterType != null) {
                    event.setData(replacementCounterType.getName());
                }
                break;

            default:
                break;
        }

        return false;
    }

    /**
     * Specifies which counter type(s) this effect applies to.
     * If not set, applies to all counter types.
     *
     * @param counterTypes the counter type(s) to filter
     */
    public ReplaceCounterEffect addValidCounterTypes(CounterType... counterTypes) {
        this.validCounterTypes.addAll(Arrays.asList(counterTypes));
        return this;
    }

    /**
     * Specifies the replacement counter type (for REPLACE modification).
     *
     * @param counterType the counter type to replace with
     */
    public ReplaceCounterEffect withReplacementCounter(CounterType counterType) {
        this.replacementCounterType = counterType;
        return this;
    }

    /**
     * Sets which player's counter placement is affected.
     *
     * @param eventController YOU, OPPONENT, or ANY
     */
    public ReplaceCounterEffect setEventController(TargetController eventController) {
        this.eventController = eventController;
        return this;
    }

    /**
     * Sets a filter for which permanents are affected. Also enables targeting permanents.
     *
     * @param filter the permanent filter
     */
    public ReplaceCounterEffect setPermanentFilter(FilterPermanent filter) {
        this.permanentFilter = filter;
        this.targetPermanents = true;
        return this;
    }

    /**
     * Sets which players can be affected by this effect. Also enables targeting players.
     *
     * @param validPlayerTarget YOU, OPPONENT, or ANY
     */
    public ReplaceCounterEffect setValidPlayerTarget(TargetController validPlayerTarget) {
        this.validPlayerTarget = validPlayerTarget;
        this.targetPlayers = true;
        return this;
    }

    /**
     * Sets whether this effect applies to players receiving counters.
     *
     * @param targetPlayers true if players can be affected
     */
    public ReplaceCounterEffect setTargetPlayers(boolean targetPlayers) {
        this.targetPlayers = targetPlayers;
        return this;
    }

    /**
     * Sets whether this effect applies to permanents receiving counters.
     *
     * @param targetPermanents true if permanents can be affected
     */
    public ReplaceCounterEffect setTargetPermanents(boolean targetPermanents) {
        this.targetPermanents = targetPermanents;
        return this;
    }

    /**
     * Makes this effect optional, requiring a choice from the controller.
     *
     * @param optional true if the effect should be optional
     */
    public ReplaceCounterEffect setOptional(boolean optional) {
        this.optional = optional;
        return this;
    }

    /**
     * Sets a custom condition for when this effect applies.
     *
     * @param condition the condition to check
     */
    public ReplaceCounterEffect withCondition(CounterCondition condition) {
        this.condition = condition;
        return this;
    }

    /**
     * Makes this effect only apply to counters placed as costs (e.g., planeswalker loyalty).
     *
     */
    public ReplaceCounterEffect onlyFromCosts() {
        this.onlyFromCosts = true;
        this.onlyFromEffects = false;
        return this;
    }

    /**
     * Makes this effect only apply to counters placed as effects (not costs).
     *
     */
    public ReplaceCounterEffect onlyFromEffects() {
        this.onlyFromEffects = true;
        this.onlyFromCosts = false;
        return this;
    }

    /**
     * Checks if a counter name matches our valid counter types.
     */
    private boolean isValidCounterType(String counterName) {
        for (CounterType type : validCounterTypes) {
            if (type.getName().equals(counterName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ReplaceCounterEffect setText(String staticText) {
        return (ReplaceCounterEffect) super.setText(staticText);
    }
}

