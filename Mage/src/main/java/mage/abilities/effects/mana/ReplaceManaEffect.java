package mage.abilities.effects.mana;

import mage.Mana;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.effects.ReplacementEffect;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.ChooseColorEffect;
import mage.choices.ChoiceColor;
import mage.constants.Duration;
import mage.constants.ManaType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.LoseUnspentManaEvent;
import mage.game.events.ManaEvent;
import mage.game.events.TappedForManaEvent;
import mage.game.permanent.Permanent;
import mage.players.ManaPoolItem;
import mage.players.Player;
import mage.util.CardUtil;

import java.io.Serializable;
import java.util.*;

/**
 * Effect to replace mana as it is produced or lost as unspent mana, with various options for matching and transforming the mana.
 *
 * @author Jmlundeen
 */
public class ReplaceManaEffect extends ReplacementEffectImpl {

    public enum Mode {
        PRODUCED,
        UNSPENT
    }

    /**
     * Function to match mana being produced. Use when needing to match against specific mana, producer, or other context information.
     */
    @FunctionalInterface
    public interface ProducedManaMatcher extends Serializable {

        boolean matches(ProducedManaContext context);
    }

    /**
     * Function to transform mana being produced.
     * Returns a Mana object to replace the produced mana.
     */
    @FunctionalInterface
    public interface ProducedManaTransform extends Serializable {

        Mana apply(ProducedManaContext context);
    }

    /**
     * Function to match unspent mana being lost. Use when needing to match against specific mana
     */
    @FunctionalInterface
    public interface UnspentManaMatcher extends Serializable {

        boolean matches(UnspentManaContext context);
    }

    /**
     * Function to transform unspent mana being lost. The function should modify the mana items in the context to change the mana being lost.
     */
    @FunctionalInterface
    public interface UnspentManaTransform extends Serializable {

        List<ManaPoolItem> apply(UnspentManaContext context);
    }

    /**
     * Context object passed to matchers and transformers for mana being produced.
     */
    public record ProducedManaContext(
            Game game,
            Ability source,
            ReplaceManaEffect effect,
            ManaEvent event,
            Mana originalMana,
            Mana mana,
            UUID recipientId,
            UUID eventSourceId,
            Permanent producerPermanent,
            boolean checkPlayableState
    ) {
    }

    /**
     * Context object passed to matchers and transformers for unspent mana being lost.
     */
    public record UnspentManaContext(
            Game game,
            Ability source,
            ReplaceManaEffect effect,
            LoseUnspentManaEvent event,
            UUID playerId,
            List<ManaPoolItem> manaItems,
            boolean checkPlayableState
    ) {
    }

    private Mode mode = Mode.PRODUCED;
    private final Set<GameEvent.EventType> eventTypes = EnumSet.of(GameEvent.EventType.TAPPED_FOR_MANA);
    private ProducedManaMatcher producedMatcher;
    private ProducedManaTransform producedTransform;
    private UnspentManaMatcher unspentMatcher;
    private UnspentManaTransform unspentTransform;
    private boolean producerMustMatchTargetPointer;

    public ReplaceManaEffect(Duration duration, Outcome outcome) {
        super(duration, outcome);
    }

    protected ReplaceManaEffect(final ReplaceManaEffect effect) {
        super(effect);
        this.mode = effect.mode;
        this.eventTypes.clear();
        this.eventTypes.addAll(effect.eventTypes);
        this.producedMatcher = effect.producedMatcher;
        this.producedTransform = effect.producedTransform;
        this.unspentMatcher = effect.unspentMatcher;
        this.unspentTransform = effect.unspentTransform;
        this.producerMustMatchTargetPointer = effect.producerMustMatchTargetPointer;
    }

    private ReplaceManaEffect(Duration duration, Outcome outcome, Mode mode) {
        this(duration, outcome);
        this.mode = mode;
        this.eventTypes.clear();
        this.eventTypes.add(mode == Mode.UNSPENT ? GameEvent.EventType.LOSE_UNSPENT_MANA : GameEvent.EventType.TAPPED_FOR_MANA);
    }

    /**
     * Helper constructor for creating a produced mana replacement effect
     * @param duration duration of the effect
     * @param outcome outcome for ai
     * @param transform function to state what happens to the produced mana
     */
    public static ReplaceManaEffect produced(Duration duration, Outcome outcome, ProducedManaTransform transform) {
        return new ReplaceManaEffect(duration, outcome, Mode.PRODUCED)
                .setProducedTransform(transform);
    }

    /**
     * Helper constructor for creating an unspent mana replacement effect
     * @param duration duration of the effect
     * @param outcome outcome for ai
     * @param transform function to modify the mana items representing the unspent mana being lost. The function should modify the mana items in-place to change the mana being lost.
     */
    public static ReplaceManaEffect unspent(Duration duration, Outcome outcome, UnspentManaTransform transform) {
        return new ReplaceManaEffect(duration, outcome, Mode.UNSPENT)
                .setUnspentTransform(transform);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        if (mode == Mode.UNSPENT) {
            LoseUnspentManaEvent loseUnspentManaEvent = (LoseUnspentManaEvent) event;
            UnspentManaContext context = createUnspentManaContext(loseUnspentManaEvent, source, game);
            getResolvedUnspentTransform().apply(context);
            return true;
        }
        ProducedManaContext context = createProducedManaContext((ManaEvent) event, source, game);
        Mana transformedMana = producedTransform.apply(context);
        if (transformedMana != null) {
            context.mana().setToMana(transformedMana);
        }
        return false;
    }

    private UnspentManaTransform getResolvedUnspentTransform() {
        return unspentTransform != null ? unspentTransform : UnspentManaContext::manaItems;
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return eventTypes.contains(event.getType());
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        if (mode == Mode.UNSPENT) {
            if (!(event instanceof LoseUnspentManaEvent loseUnspentManaEvent)) {
                return false;
            }
            UnspentManaContext context = createUnspentManaContext(loseUnspentManaEvent, source, game);
            return unspentMatcher == null || unspentMatcher.matches(context);
        }
        if (!(event instanceof ManaEvent manaEvent)) {
            return false;
        }
        ProducedManaContext context = createProducedManaContext(manaEvent, source, game);
        if (producerMustMatchTargetPointer) {
            UUID targetId = getTargetPointer().getFirst(game, source);
            if (targetId == null || context.producerPermanent() == null || !targetId.equals(context.producerPermanent().getId())) {
                return false;
            }
        }
        return producedMatcher == null || producedMatcher.matches(context);
    }

    @Override
    public ReplacementEffect copy() {
        return new ReplaceManaEffect(this);
    }

    public ReplaceManaEffect setProducedMatcher(ProducedManaMatcher producedMatcher) {
        this.producedMatcher = producedMatcher;
        return this;
    }

    public ReplaceManaEffect setProducedMatcher(FilterTyped filter) {
        this.producedMatcher = context -> context.producerPermanent() != null
                && filter.match(context.producerPermanent(), context.source().getControllerId(), context.source(), context.game());
        return this;
    }

    public ReplaceManaEffect setProducedTransform(ProducedManaTransform producedTransform) {
        this.mode = Mode.PRODUCED;
        if (this.eventTypes.isEmpty() || this.eventTypes.contains(GameEvent.EventType.LOSE_UNSPENT_MANA)) {
            this.eventTypes.clear();
            this.eventTypes.add(GameEvent.EventType.TAPPED_FOR_MANA);
        }
        this.producedTransform = Objects.requireNonNull(producedTransform);
        return this;
    }

    public ReplaceManaEffect setUnspentMatcher(UnspentManaMatcher unspentMatcher) {
        this.unspentMatcher = unspentMatcher;
        return this;
    }

    public ReplaceManaEffect setUnspentTransform(UnspentManaTransform unspentTransform) {
        this.mode = Mode.UNSPENT;
        this.eventTypes.clear();
        this.eventTypes.add(GameEvent.EventType.LOSE_UNSPENT_MANA);
        this.unspentTransform = Objects.requireNonNull(unspentTransform);
        return this;
    }

    public ReplaceManaEffect setMode(Mode mode) {
        this.mode = mode;
        this.eventTypes.clear();
        this.eventTypes.add(mode == Mode.UNSPENT ? GameEvent.EventType.LOSE_UNSPENT_MANA : GameEvent.EventType.TAPPED_FOR_MANA);
        return this;
    }

    /**
     * Premade transform for multiplying produced mana
     * @param multiplier factor to multiply the produced mana by
     */
    public static ProducedManaTransform multiplyProducedMana(int multiplier) {
        return context -> {
            Mana mana = context.originalMana().copy();
            mana.setWhite(CardUtil.overflowMultiply(context.originalMana().getWhite(), multiplier));
            mana.setBlue(CardUtil.overflowMultiply(context.originalMana().getBlue(), multiplier));
            mana.setBlack(CardUtil.overflowMultiply(context.originalMana().getBlack(), multiplier));
            mana.setRed(CardUtil.overflowMultiply(context.originalMana().getRed(), multiplier));
            mana.setGreen(CardUtil.overflowMultiply(context.originalMana().getGreen(), multiplier));
            mana.setColorless(CardUtil.overflowMultiply(context.originalMana().getColorless(), multiplier));
            mana.setAny(CardUtil.overflowMultiply(context.originalMana().getAny(), multiplier));
            mana.setGeneric(CardUtil.overflowMultiply(context.originalMana().getGeneric(), multiplier));
            return mana;
        };
    }

    /**
     * Premade transform for replacing all produced mana with a specific mana.
     * @param mana the mana to replace with
     */
    public static ProducedManaTransform replaceAllProducedMana(Mana mana) {
        return context -> {
            if (mana == null) {
                throw new IllegalArgumentException("Mana to replace with must not be null");
            }
            if (!context.checkPlayableState() && mana.getAny() > 0) {
                ChoiceColor choiceColor = new ChoiceColor();
                Player controller = context.game().getPlayer(context.source().getControllerId());
                if (controller == null) {
                    return context.originalMana();
                }
                if (controller.choose(Outcome.PutManaInPool, choiceColor, context.game())) {
                    return choiceColor.getMana(mana.getAny());
                } else {
                    return context.originalMana();
                }
            }
            return mana.copy();
        };
    }

    /**
     * Premade transform for replacing all produced mana with a specific color, keeping the same amount and conditions.
     * @param color the color to replace with
     */
    public static ProducedManaTransform replaceAllWithColor(ManaType color) {
        return context -> {
            Mana mana = new Mana();
            mana.set(color, context.originalMana().count());
            return mana;
        };
    }

    /**
     * Premade transform for replacing produced mana based on the subtype of the producing permanent.
     * @param subtypeMap map of subtypes to mana types for replacement
     */
    public static ProducedManaTransform replaceBySubtypeMap(Map<SubType, ManaType> subtypeMap) {
        return context -> {
            Permanent permanent = context.producerPermanent();
            if (permanent == null || subtypeMap == null || subtypeMap.isEmpty()) {
                return context.mana();
            }

            Set<ManaType> choices = new LinkedHashSet<>();
            for (Map.Entry<SubType, ManaType> entry : subtypeMap.entrySet()) {
                if (entry.getKey() != null && permanent.hasSubtype(entry.getKey(), context.game()) && entry.getValue() != null) {
                    choices.add(entry.getValue());
                }
            }

            if (choices.isEmpty()) {
                return context.mana();
            }

            if (choices.size() == 1) {
                return manaOfType(choices.iterator().next(), context.originalMana().count());
            }

            if (context.game().inCheckPlayableState()) {
                Mana mana = new Mana(
                        choices.contains(ManaType.WHITE) ? 1 : 0,
                        choices.contains(ManaType.BLUE) ? 1 : 0,
                        choices.contains(ManaType.BLACK) ? 1 : 0,
                        choices.contains(ManaType.RED) ? 1 : 0,
                        choices.contains(ManaType.GREEN) ? 1 : 0,
                        0, 0, 0
                );
                mana.setAnyCombination(true);
                return mana;
            }

            Player controller = context.game().getPlayer(context.recipientId());
            if (controller == null) {
                return context.mana();
            }

            ChoiceColor choice = new ChoiceColor();
            choice.getChoices().clear();
            choice.setMessage("Pick a color to produce");
            if (choices.contains(ManaType.WHITE)) {
                choice.getChoices().add("White");
            }
            if (choices.contains(ManaType.BLUE)) {
                choice.getChoices().add("Blue");
            }
            if (choices.contains(ManaType.BLACK)) {
                choice.getChoices().add("Black");
            }
            if (choices.contains(ManaType.RED)) {
                choice.getChoices().add("Red");
            }
            if (choices.contains(ManaType.GREEN)) {
                choice.getChoices().add("Green");
            }

            if (!controller.choose(Outcome.PutManaInPool, choice, context.game())) {
                return context.mana();
            }

            return choice.getChoice() == null
                    ? context.mana()
                    : manaOfType(getManaTypeFromChoice(choice.getChoice()), context.originalMana().count());
        };
    }

    /**
     * Premade transform for replacing produced mana with the color chosen by the player, keeping the same amount and conditions.
     * The chosen color should be stored in the game state with {@link ChooseColorEffect}
     */
    public static ProducedManaTransform replaceColorWithChosenColor() {
        return context -> {
            ObjectColor color = ChooseColorEffect.getChosenColor(context.source().getSourceId(), context.game());
            if (color == null) {
                return context.originalMana();
            }
            Mana mana = context.mana();
            int colorCount = context.originalMana().countColored();
            int genericCount = context.originalMana().getGeneric();
            int colorlessCount = context.originalMana().getColorless();
            switch (color.getOneColoredManaSymbol()) {
                case W -> mana.setToMana(Mana.WhiteMana(colorCount));
                case U -> mana.setToMana(Mana.BlueMana(colorCount));
                case B -> mana.setToMana(Mana.BlackMana(colorCount));
                case R -> mana.setToMana(Mana.RedMana(colorCount));
                case G -> mana.setToMana(Mana.GreenMana(colorCount));
                default -> {
                    // should not happen as the choice is limited to colored mana types, but just in case
                    return context.originalMana();
                }
            }
            mana.setGeneric(genericCount);
            mana.setColorless(colorlessCount);
            return mana;
        };
    }

    public static ProducedManaTransform replaceProducedMana(ProducedManaTransform transform) {
        return transform;
    }

    public static UnspentManaTransform replaceUnspentMana(UnspentManaTransform transform) {
        return transform;
    }

    /**
     * Changes unspent mana to the specified type, keeping the same amount and conditions
     * @param type
     * @return
     */
    public static UnspentManaTransform changeUnspentManaToType(ManaType type) {
        return context -> {
            for (ManaPoolItem item : context.manaItems()) {
                if (item == null || item.count() == 0) {
                    continue;
                }

                int amount = item.count();
                clearMana(item);
                item.add(type, amount);
            }
            return context.manaItems();
        };
    }

    private ProducedManaContext createProducedManaContext(ManaEvent event, Ability source, Game game) {
        Permanent producerPermanent = event instanceof TappedForManaEvent tappedForManaEvent
                ? tappedForManaEvent.getPermanent()
                : null;
        return new ProducedManaContext(
                game,
                source,
                this,
                event,
                event.getMana().copy(),
                event.getMana(),
                event.getPlayerId(),
                event.getSourceId(),
                producerPermanent,
                game.inCheckPlayableState()
        );
    }

    private UnspentManaContext createUnspentManaContext(LoseUnspentManaEvent event, Ability source, Game game) {
        return new UnspentManaContext(
                game,
                source,
                this,
                event,
                event.getPlayerId(),
                event.getManaItems(),
                game.inCheckPlayableState()
        );
    }

    private static Mana manaOfType(ManaType manaType, int amount) {
        if (manaType == null) {
            return new Mana();
        }
        return switch (manaType) {
            case WHITE -> Mana.WhiteMana(amount);
            case BLUE -> Mana.BlueMana(amount);
            case BLACK -> Mana.BlackMana(amount);
            case RED -> Mana.RedMana(amount);
            case GREEN -> Mana.GreenMana(amount);
            case COLORLESS -> Mana.ColorlessMana(amount);
            default -> Mana.AnyMana(amount);
        };
    }

    private static ManaType getManaTypeFromChoice(String choice) {
        return switch (choice) {
            case "White" -> ManaType.WHITE;
            case "Blue" -> ManaType.BLUE;
            case "Black" -> ManaType.BLACK;
            case "Red" -> ManaType.RED;
            case "Green" -> ManaType.GREEN;
            default -> null;
        };
    }

    private static void clearMana(ManaPoolItem item) {
        item.clear(ManaType.BLACK);
        item.clear(ManaType.BLUE);
        item.clear(ManaType.GREEN);
        item.clear(ManaType.RED);
        item.clear(ManaType.WHITE);
        item.clear(ManaType.COLORLESS);
    }
}
