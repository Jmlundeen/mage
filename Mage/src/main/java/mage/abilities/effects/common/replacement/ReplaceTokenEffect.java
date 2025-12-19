package mage.abilities.effects.common.replacement;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.CopyEffect;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.TargetController;
import mage.game.Game;
import mage.game.events.CreateTokenEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.Token;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.util.CardUtil;
import mage.util.functions.CopyApplier;
import mage.util.functions.CopyTokenFunction;
import mage.util.functions.EmptyCopyApplier;

import java.util.*;

/**
 * Replacement effect for modifying token creation amounts or replacing tokens with other types.
 * This effect can handle various modifications such as multiplying, dividing, adding, subtracting,
 * or replacing tokens based on specific conditions.
 *
 * @author jmlundeen
 */
public class ReplaceTokenEffect extends ReplacementEffectImpl {


    /**
     * Enum representing the types of modifications that can be applied to token creation.
     */
    public enum ModificationType {
        // Amount-based modifications
        MULTIPLY,      // Doubles or multiplies token amounts
        DIVIDE,        // Divides token amounts
        ADD,           // Adds additional tokens of specified type
        SUBTRACT,      // Subtracts from token amounts

        // Token replacement
        REPLACE,           // Replace with a different token type
        REPLACE_ATTACHED,  // Replace with copy of attached permanent
        REPLACE_CHOOSE,    // Replace with copy of chosen permanent

        // Special types
        THAT_MANY,         // Create that many of another token type
        CONTROLLER         // Change token controller
    }

    /**
     * Functional interface for defining conditions on tokens.
     */
    public interface TokenCondition {
        boolean apply(Token token, Game game);
    }

    protected final ModificationType modificationType; // Type of modification
    protected final int factor; // Factor for multiplication or addition
    protected Token token; // Token template for replacement or addition
    protected boolean optional = false; // Whether the effect is optional
    protected boolean gainControl = false; // Whether to gain control of tokens
    protected TokenCondition tokenCondition; // Condition for applying the effect
    protected TargetPermanent target; // Target permanent for replacement
    protected TargetController controllingPlayer = TargetController.YOU; // Player whose tokens are affected
    protected Set<Token> additionalTokens = new LinkedHashSet<>(); // Additional tokens to create

    public ReplaceTokenEffect(ModificationType modificationType, int factor, Token token) {
        this(Duration.WhileOnBattlefield, Outcome.Benefit, modificationType, factor, token);
    }

    public ReplaceTokenEffect(ModificationType modificationType, int factor) {
        this(Duration.WhileOnBattlefield, Outcome.Benefit, modificationType, factor, null);
    }

    public ReplaceTokenEffect(Duration duration, Outcome outcome, ModificationType modificationType) {
        this(duration, outcome, null, 0, null);
    }

    public ReplaceTokenEffect(Duration duration, Outcome outcome, ModificationType modificationType, int factor,
                              Token token) {
        super(duration, outcome, false);
        this.modificationType = modificationType;
        this.factor = factor;
        this.token = token;
    }

    protected ReplaceTokenEffect(final ReplaceTokenEffect effect) {
        super(effect);
        this.modificationType = effect.modificationType;
        this.factor = effect.factor;
        this.gainControl = effect.gainControl;
        this.token = this.token == null ? null : effect.token.copy();
        this.tokenCondition = effect.tokenCondition;
        this.target = effect.target;
        this.optional = effect.optional;
        this.controllingPlayer = effect.controllingPlayer;
        this.additionalTokens = CardUtil.deepCopyObject(effect.additionalTokens);
    }

    @Override
    public ReplaceTokenEffect copy() {
        return new ReplaceTokenEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.CREATE_TOKEN;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        if (optional) {
            Permanent permanent = source.getSourcePermanentIfItStillExists(game);
            if (permanent == null) {
                return false;
            }
            if (!controller.chooseUse(outcome, "Use " + permanent.getLogName() + " token replacement?", source, game)) {
                return false;
            }
        }
        boolean result;
        if (gainControl) {
            result = controller.hasOpponent(event.getPlayerId(), game);
        } else {
            switch (controllingPlayer) {
                case YOU:
                    result = event.getPlayerId().equals(source.getControllerId());
                    break;
                case ANY:
                    result = true;
                    break;
                case OPPONENT:
                    result = controller.hasOpponent(event.getPlayerId(), game);
                    break;
                default:
                    throw new IllegalStateException("Unknown controlling player: " + controllingPlayer);
            }
        }
        if (tokenCondition == null) {
            return result;
        }
        CreateTokenEvent tokenEvent = (CreateTokenEvent) event;
        for (Token t : tokenEvent.getTokens().keySet()) {
            if (tokenCondition.apply(t, game)) {
                return result;
            }
        }
        return false;
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }

        CreateTokenEvent createEvent = (CreateTokenEvent) event;

        // Handle controller change type
        if (modificationType == ModificationType.CONTROLLER) {
            if (gainControl) {
                createEvent.setPlayerId(controller.getId());
            }
            return false;
        }

        // Handle token replacement types
        if (modificationType == ModificationType.REPLACE ||
            modificationType == ModificationType.REPLACE_CHOOSE ||
            modificationType == ModificationType.REPLACE_ATTACHED) {
            handleTokenReplacement(createEvent, source, game, controller);
            return false;
        }

        // Handle amount modifications
        if (modificationType == ModificationType.MULTIPLY ||
            modificationType == ModificationType.DIVIDE) {
            handleAmountModification(createEvent, source, game);
            return false;
        }

        // Handle add token type
        if (modificationType == ModificationType.ADD ||
            modificationType == ModificationType.SUBTRACT ||
            modificationType == ModificationType.THAT_MANY) {
            handleAddToken(createEvent, source, game, controller);
            return false;
        }

        return false;
    }

    /**
     * Handles replacing tokens with different token types (REPLACE, REPLACE_CHOOSE, REPLACE_ATTACHED).
     */
    private void handleTokenReplacement(CreateTokenEvent createEvent, Ability source, Game game, Player controller) {
        if (gainControl) {
            createEvent.setPlayerId(controller.getId());
        }

        // Determine the token template to create
        Token tokenToCreate = determineTokenToCreate(source, game, controller);
        if (tokenToCreate == null) {
            return;
        }

        List<Map.Entry<Token, Integer>> toReplace = new ArrayList<>();

        // Collect tokens to replace
        for (Map.Entry<Token, Integer> entry : createEvent.getTokens().entrySet()) {
            if (tokenCondition == null || tokenCondition.apply(entry.getKey(), game)) {
                toReplace.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
            }
        }

        // Remove replaced tokens
        for (Map.Entry<Token, Integer> entry : toReplace) {
            createEvent.getTokens().remove(entry.getKey());
        }

        // Add replacement tokens
        for (Map.Entry<Token, Integer> entry : toReplace) {
            int amount = entry.getValue() * (factor > 0 ? factor : 1);
            if (amount <= 0) {
                continue;
            }

            // Add main replacement token
            Token newToken = tokenToCreate.copy();
            addOrMergeToken(createEvent, newToken, amount);

            // Add any additional tokens
            for (Token addTemplate : additionalTokens) {
                Token additionalToken = addTemplate.copy();
                addOrMergeToken(createEvent, additionalToken, amount);
            }
        }
    }

    /**
     * Handles multiplying or dividing token amounts (MULTIPLY, DIVIDE).
     */
    private void handleAmountModification(CreateTokenEvent createEvent, Ability source, Game game) {
        for (Map.Entry<Token, Integer> entry : createEvent.getTokens().entrySet()) {
            if (tokenCondition != null && !tokenCondition.apply(entry.getKey(), game)) {
                continue;
            }

            int currentAmount = entry.getValue();
            int newAmount;

            if (modificationType == ModificationType.MULTIPLY) {
                newAmount = CardUtil.overflowMultiply(currentAmount, factor);
            } else {
                newAmount = Math.max(1, currentAmount / factor);
            }

            entry.setValue(newAmount);
        }
    }

    /**
     * Handles adding or subtracting tokens (ADD, SUBTRACT, THAT_MANY).
     */
    private void handleAddToken(CreateTokenEvent createEvent, Ability source, Game game, Player controller) {
        if (gainControl) {
            createEvent.setPlayerId(controller.getId());
        }

        Token tokenToCreate = determineTokenToCreate(source, game, controller);

        if (modificationType == ModificationType.THAT_MANY) {
            // Calculate total amount of matching tokens
            int totalMatchingAmount = 0;
            for (Map.Entry<Token, Integer> entry : createEvent.getTokens().entrySet()) {
                if (tokenCondition == null || tokenCondition.apply(entry.getKey(), game)) {
                    totalMatchingAmount = CardUtil.overflowInc(totalMatchingAmount, entry.getValue());
                }
            }

            if (totalMatchingAmount > 0 && tokenToCreate != null) {
                // Add the template token with the total matching amount
                Token newToken = tokenToCreate.copy();
                addOrMergeToken(createEvent, newToken, totalMatchingAmount);

                // Add any additional tokens
                for (Token addTemplate : additionalTokens) {
                    Token additionalToken = addTemplate.copy();
                    addOrMergeToken(createEvent, additionalToken, totalMatchingAmount);
                }
            }
            return;
        }

        // Handle ADD and SUBTRACT
        boolean anyModified = false;
        for (Map.Entry<Token, Integer> entry : createEvent.getTokens().entrySet()) {
            if (tokenCondition != null && !tokenCondition.apply(entry.getKey(), game)) {
                continue;
            }

            // If we have a specific token template, only modify matching tokens
            boolean matchingToken = tokenToCreate == null ||
                                   entry.getKey().getClass().equals(tokenToCreate.getClass());

            if (!matchingToken) {
                continue;
            }

            int currentAmount = entry.getValue();
            int newAmount;

            if (modificationType == ModificationType.ADD) {
                newAmount = CardUtil.overflowInc(currentAmount, factor);
            } else { // SUBTRACT
                newAmount = Math.max(0, currentAmount - factor);
            }

            entry.setValue(newAmount);
            anyModified = true;

            // Add any additional tokens with the new amount
            for (Token addTemplate : additionalTokens) {
                Token additionalToken = addTemplate.copy();
                addOrMergeToken(createEvent, additionalToken, newAmount);
            }
        }

        // If no existing tokens were modified, and we have a template, create new tokens
        if (!anyModified && tokenToCreate != null && modificationType == ModificationType.ADD) {
            Token newToken = tokenToCreate.copy();
            addOrMergeToken(createEvent, newToken, factor);

            // Add any additional tokens
            for (Token addTemplate : additionalTokens) {
                Token additionalToken = addTemplate.copy();
                addOrMergeToken(createEvent, additionalToken, factor);
            }
        }
    }

    /**
     * Determines which token to create based on the modification type and settings.
     */
    private Token determineTokenToCreate(Ability source, Game game, Player controller) {
        if (target != null) {
            controller.choose(outcome, target, source, game);
            Permanent permanent = game.getPermanent(target.getFirstTarget());
            if (permanent != null) {
                return copyPermanentToToken(permanent, game, source);
            }
        } else if (modificationType == ModificationType.REPLACE_ATTACHED) {
            Permanent permanent = Optional
                    .ofNullable(source.getSourcePermanentIfItStillExists(game))
                    .map(Permanent::getAttachedTo)
                    .map(game::getPermanent)
                    .orElse(null);
            if (permanent != null) {
                return copyPermanentToToken(permanent, game, source);
            }
        } else if (token != null) {
            return token.copy();
        }
        return null;
    }

    /**
     * Adds a token to the event or merges with existing token of same type.
     */
    private void addOrMergeToken(CreateTokenEvent createEvent, Token tokenToAdd, int amount) {
        if (amount <= 0) {
            return;
        }

        // Try to merge with existing token of same class
        for (Map.Entry<Token, Integer> existing : createEvent.getTokens().entrySet()) {
            if (existing.getKey().getClass().equals(tokenToAdd.getClass())) {
                existing.setValue(CardUtil.overflowInc(existing.getValue(), amount));
                return;
            }
        }

        // No matching token found, add as new entry
        createEvent.getTokens().put(tokenToAdd, amount);
    }
    /**
     * Enables the effect to gain control of tokens when applied.
     */
    public ReplaceTokenEffect withGainControl() {
        this.gainControl = true;
        return this;
    }

    /**
     * Sets a condition that tokens must meet for the effect to apply.
     * @param tokenCondition the condition to be applied to tokens
     */
    public ReplaceTokenEffect withTokenCondition(TokenCondition tokenCondition) {
        this.tokenCondition = tokenCondition;
        return this;
    }

    /**
     * Sets a target permanent to be used for token replacement.
     * @param target the target permanent to be used for replacement
     */
    public ReplaceTokenEffect withChosenPermanent(TargetPermanent target) {
        this.target = target;
        return this;
    }

    /**
     * Marks the effect as optional, allowing the controller to choose whether to apply it.
     * @param optional true if the effect should be optional, false otherwise
     */
    public ReplaceTokenEffect setOptional(boolean optional) {
        this.optional = optional;
        return this;
    }

    /**
     * Sets which player's tokens are affected by this effect. Default is YOU.
     * @param controllingPlayer supports YOU, OPPONENT, ANY
     */
    public ReplaceTokenEffect setControllingPlayer(TargetController controllingPlayer) {
        this.controllingPlayer = controllingPlayer;
        return this;
    }

    /**
     * Add a token template to be created when this replacement applies.
     * The added token will be created in the same amount as the main token (not an absolute amount).
     */
    public ReplaceTokenEffect withAdditionalTokens(Token token) {
        if (token == null) {
            return this;
        }
        this.additionalTokens.add(token);
        return this;
    }

    /**
     * Creates a token copy of a permanent, handling copy effects properly.
     */
    private static Token copyPermanentToToken(Permanent permanent, Game game, Ability source) {
        CopyApplier applier = new EmptyCopyApplier();
        // handle copies of copies
        Permanent copyFromPermanent = permanent;
        for (ContinuousEffect effect : game.getState().getContinuousEffects().getLayeredEffects(game)) {
            if (!(effect instanceof CopyEffect)) {
                continue;
            }
            CopyEffect copyEffect = (CopyEffect) effect;
            // there is another copy effect that our targetPermanent copies stats from
            if (!copyEffect.getSourceId().equals(permanent.getId())) {
                continue;
            }
            MageObject object = ((CopyEffect) effect).getTarget();
            if (!(object instanceof Permanent)) {
                continue;
            }
            copyFromPermanent = (Permanent) object;
            if (copyEffect.getApplier() != null) {
                applier = copyEffect.getApplier();
            }
        }

        // create token and modify all attributes permanently (without game usage)
        Token token = CopyTokenFunction.createTokenCopy(copyFromPermanent, game); // needed so that entersBattlefield triggered abilities see the attributes (e.g. Master Biomancer)
        applier.apply(game, token, source, permanent.getId());
        return token;
    }
}
