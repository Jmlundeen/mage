package mage.abilities.effects.common.counter;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.constants.AbilityType;
import mage.constants.Outcome;
import mage.constants.TargetController;
import mage.counters.Counter;
import mage.counters.CounterType;
import mage.filter.FilterTyped;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.util.CardUtil;
import mage.util.ObjectQuery;

import java.util.*;

/**
 * Generic counter-adding effect that can target source, attached permanent,
 * current targets, all permanents matching a typed filter, or players chosen by
 * a target-controller mode.
 * @author jmlundeen
 */
public class AddCountersEffect extends OneShotEffect {

    private static final DynamicValue DEFAULT_AMOUNT = StaticValue.get(1);

    private enum AddCountersMode {
        SOURCE,
        ATTACHED,
        TARGET,
        ALL,
        PLAYERS
    }

    private final CounterType counterType;
    private final DynamicValue amount;
    private final AddCountersMode mode;
    private final boolean putOnCard;
    private final FilterTyped filter;
    private final TargetController targetController;

    /**
     * Constructor for placing counters on the source permanent.
     * @param counterType type of counter to place
     */
    public AddCountersEffect(CounterType counterType) {
        this(counterType, DEFAULT_AMOUNT, Outcome.Benefit, AddCountersMode.SOURCE, false, null, null);
    }

    /**
     * Constructor for placing counters on the source permanent.
     * @param counterType type of counter to place
     * @param amount amount of counters to place
     */
    public AddCountersEffect(CounterType counterType, DynamicValue amount) {
        this(counterType, amount, Outcome.Benefit, AddCountersMode.SOURCE, false, null, null);
    }

    /**
     * Constructor for placing counters on the source permanent or card
     * @param counterType type of counter to place
     * @param amount amount of counters to place
     * @param putOnCard place the counters on the source card instead of permanent
     */
    public AddCountersEffect(CounterType counterType, DynamicValue amount, boolean putOnCard) {
        this(counterType, amount, Outcome.Benefit, AddCountersMode.SOURCE, putOnCard, null, null);
    }

    /**
     * Constructor for placing counters on all permanents matching a typed filter.
     * @param counterType type of counter to place
     * @param filter filter for permanents to place counters on
     */
    public AddCountersEffect(CounterType counterType, FilterTyped filter) {
        this(counterType, DEFAULT_AMOUNT, Outcome.Benefit, AddCountersMode.ALL, false, filter, null);
    }

    /**
     * Constructor for placing counters on all permanents matching a typed filter.
     * @param counterType type of counter to place
     * @param amount amount of counters to place
     * @param filter filter for permanents to place counters on
     */
    public AddCountersEffect(CounterType counterType, DynamicValue amount, FilterTyped filter) {
        this(counterType, amount, Outcome.Benefit, AddCountersMode.ALL, false, filter, null);
    }

    /**
     * Constructor for placing counters on players chosen by a target-controller mode.
     * @param counterType type of counter to place
     * @param targetController target-controller mode for choosing players
     */
    public AddCountersEffect(CounterType counterType, TargetController targetController) {
        this(counterType, DEFAULT_AMOUNT, Outcome.Benefit, AddCountersMode.PLAYERS, false, null, targetController);
    }

    /**
     * Constructor for placing counters on players chosen by a target-controller mode.
     * @param counterType type of counter to place
     * @param amount amount of counters to place
     * @param targetController target-controller mode for choosing players
     */
    public AddCountersEffect(CounterType counterType, DynamicValue amount, TargetController targetController) {
        this(counterType, amount, Outcome.Benefit, AddCountersMode.PLAYERS, false, null, targetController);
    }

    /**
     * Constructor for placing counters on the current targets of the ability.
     * @param counterType type of counter to place
     * @param outcome outcome of the effect
     */
    public AddCountersEffect(CounterType counterType, Outcome outcome) {
        this(counterType, DEFAULT_AMOUNT, outcome, AddCountersMode.TARGET, false, null, null);
    }

    /**
     * Constructor for placing counters on the current targets of the ability.
     * @param counterType type of counter to place
     * @param amount amount of counters to place
     * @param outcome outcome of the effect
     */
    public AddCountersEffect(CounterType counterType, DynamicValue amount, Outcome outcome) {
        this(counterType, amount, outcome, AddCountersMode.TARGET, false, null, null);
    }

    /**
     * Helper method for placing counters on the permanent attached to the source.
     * @param counterType type of counter to place
     */
    public static AddCountersEffect attached(CounterType counterType) {
        return new AddCountersEffect(counterType, DEFAULT_AMOUNT, Outcome.Benefit, AddCountersMode.ATTACHED, false, null, null);
    }

    /**
     * Helper method for placing counters on the permanent attached to the source.
     * @param counterType type of counter to place
     * @param amount amount of counters to place
     */
    public static AddCountersEffect attached(CounterType counterType, DynamicValue amount) {
        return new AddCountersEffect(counterType, amount, Outcome.Benefit, AddCountersMode.ATTACHED, false, null, null);
    }

    /**
     * Helper method for placing counters on the current targets of the ability.
     * @param counterType type of counter to place
     */
    public static AddCountersEffect target(CounterType counterType) {
        return new AddCountersEffect(
                counterType,
                DEFAULT_AMOUNT,
                counterType == CounterType.M1M1 ? Outcome.UnboostCreature : Outcome.Benefit
        );
    }

    /**
     * Helper method for placing counters on the current targets of the ability.
     * @param counterType type of counter to place
     * @param amount amount of counters to place
     */
    public static AddCountersEffect target(CounterType counterType, DynamicValue amount) {
        return new AddCountersEffect(
                counterType,
                amount,
                counterType == CounterType.M1M1 ? Outcome.UnboostCreature : Outcome.Benefit
        );
    }

    /**
     * Helper method for placing counters on the source permanent or card.
     * @param counterType type of counter to place
     * @param amount amount of counters to place
     */
    public static AddCountersEffect sourceCard(CounterType counterType, DynamicValue amount) {
        return new AddCountersEffect(counterType, amount, true);
    }

    private AddCountersEffect(
            CounterType counterType,
            DynamicValue amount,
            Outcome outcome,
            AddCountersMode mode,
            boolean putOnCard,
            FilterTyped filter,
            TargetController targetController
    ) {
        super(outcome);
        this.counterType = counterType;
        this.amount = amount;
        this.mode = mode;
        this.putOnCard = putOnCard;
        this.filter = filter == null ? null : filter.copy();
        this.targetController = targetController;
    }

    private AddCountersEffect(final AddCountersEffect effect) {
        super(effect);
        this.counterType = effect.counterType;
        this.amount = effect.amount;
        this.mode = effect.mode;
        this.putOnCard = effect.putOnCard;
        this.filter = effect.filter == null ? null : effect.filter.copy();
        this.targetController = effect.targetController;
    }

    @Override
    public AddCountersEffect copy() {
        return new AddCountersEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        if (counterType == null) {
            return false;
        }

        return switch (mode) {
            case SOURCE -> applyToSource(game, source);
            case ATTACHED -> applyToAttached(game, source);
            case TARGET -> applyToTargets(game, source);
            case ALL -> applyToAll(game, source);
            case PLAYERS -> applyToPlayers(game, source);
        };
    }

    private boolean applyToSource(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }

        Counter newCounter = createCounter(game, source);
        if (newCounter == null) {
            return false;
        }

        if (putOnCard) {
            Card card = game.getCard(source.getSourceId());
            if (card == null) {
                return false;
            }

            boolean added = card.addCounters(newCounter, source.getControllerId(), source, game, getAppliedEffects());
            if (added && !game.isSimulation()) {
                announceCounterPlacement(game, controller, newCounter.getCount(), newCounter.getName(), card.getLogName());
            }
            return added;
        }

        Permanent permanent = game.getPermanent(source.getSourceId());
        if (permanent == null && source.getAbilityType() == AbilityType.STATIC) {
            permanent = game.getPermanentEntering(source.getSourceId());
        }
        if (permanent == null) {
            return false;
        }

        if (source.getStackMomentSourceZCC() != 0
                && source.getStackMomentSourceZCC() != permanent.getZoneChangeCounter(game)) {
            return false;
        }

        int before = permanent.getCounters(game).getCount(newCounter.getName());
        boolean added = permanent.addCounters(newCounter, source.getControllerId(), source, game, getAppliedEffects());
        if (added && !game.isSimulation()) {
            int amountAdded = permanent.getCounters(game).getCount(newCounter.getName()) - before;
            if (amountAdded > 0) {
                announceCounterPlacement(game, controller, amountAdded, newCounter.getName(), permanent.getLogName());
            }
        }
        return added;
    }

    private boolean applyToAttached(Game game, Ability source) {
        Permanent permanent = game.getPermanent(source.getSourceId());
        if (permanent == null || permanent.getAttachedTo() == null) {
            return false;
        }

        Permanent attachedTo = game.getPermanent(permanent.getAttachedTo());
        if (attachedTo == null) {
            return false;
        }

        Counter newCounter = createCounter(game, source);
        return newCounter != null && attachedTo.addCounters(newCounter, source.getControllerId(), source, game);
    }

    private boolean applyToTargets(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        MageObject sourceObject = game.getObject(source);
        if (controller == null || sourceObject == null) {
            return false;
        }

        Counter newCounter = createCounter(game, source);
        if (newCounter == null) {
            return false;
        }

        int affectedTargets = 0;
        for (UUID targetId : getTargetPointer().getTargets(game, source)) {
            Counter newCounterForTarget = newCounter.copy();

            Permanent permanent = game.getPermanent(targetId);
            if (permanent != null) {
                if (permanent.addCounters(newCounterForTarget, source.getControllerId(), source, game)) {
                    affectedTargets++;
                    announceTargetCounterPlacement(game, sourceObject, controller, newCounterForTarget, permanent.getLogName());
                }
                continue;
            }

            Player player = game.getPlayer(targetId);
            if (player != null) {
                if (player.addCounters(newCounterForTarget, source.getControllerId(), source, game)) {
                    affectedTargets++;
                    announceTargetCounterPlacement(game, sourceObject, controller, newCounterForTarget, player.getLogName());
                }
                continue;
            }

            Card card = game.getCard(targetId);
            if (card != null && card.addCounters(newCounterForTarget, source.getControllerId(), source, game)) {
                affectedTargets++;
                announceTargetCounterPlacement(game, sourceObject, controller, newCounterForTarget, card.getLogName());
            }
        }
        return affectedTargets > 0;
    }

    private boolean applyToAll(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        MageObject sourceObject = game.getObject(source);
        if (controller == null || sourceObject == null || filter == null) {
            return false;
        }

        Counter newCounter = createCounter(game, source);
        if (newCounter == null) {
            return false;
        }

        boolean result = false;
        for (Permanent permanent : ObjectQuery.queryPermanents(game, controller, source, filter)) {
            Counter newCounterForPermanent = newCounter.copy();
            if (permanent.addCounters(newCounterForPermanent, source.getControllerId(), source, game)) {
                game.informPlayers(sourceObject.getLogName() + ": " + controller.getLogName() + " puts "
                        + newCounterForPermanent.getCount() + ' ' + newCounterForPermanent.getName()
                        + (newCounterForPermanent.getCount() == 1 ? " counter" : " counters") + " on " + permanent.getLogName());
                result = true;
            }
        }
        return result;
    }

    private boolean applyToPlayers(Game game, Ability source) {
        Counter newCounter = createCounter(game, source);
        if (newCounter == null) {
            return false;
        }

        boolean result = false;
        for (UUID playerId : getPlayers(game, source)) {
            Counter newCounterForPlayer = newCounter.copy();
            Player player = game.getPlayer(playerId);
            if (player != null && player.addCounters(newCounterForPlayer, source.getControllerId(), source, game)) {
                game.informPlayers(player.getLogName() + " gets "
                        + newCounterForPlayer.getCount() + ' ' + newCounterForPlayer.getName()
                        + (newCounterForPlayer.getCount() == 1 ? " counter" : " counters")
                        + CardUtil.getSourceLogName(game, source));
                result = true;
            }
        }
        return result;
    }

    private Counter createCounter(Game game, Ability source) {
        int calculated = amount.calculate(game, source, this);
        if (calculated <= 0) {
            return null;
        }
        return counterType.createInstance(calculated);
    }

    private Collection<UUID> getPlayers(Game game, Ability source) {
        return switch (targetController) {
            case OPPONENT -> game.getOpponents(source.getControllerId());
            case EACH_PLAYER, ANY -> game.getState().getPlayersInRange(source.getControllerId(), game);
            case YOU -> Collections.singletonList(source.getControllerId());
            case CONTROLLER_ATTACHED_TO -> {
                List<UUID> list = new ArrayList<>();
                Optional.ofNullable(source.getSourcePermanentOrLKI(game))
                        .map(Permanent::getAttachedTo)
                        .map(game::getControllerId)
                        .ifPresent(list::add);
                yield list;
            }
            default -> throw new UnsupportedOperationException(targetController + " not supported");
        };
    }

    private void announceTargetCounterPlacement(
            Game game,
            MageObject sourceObject,
            Player controller,
            Counter counter,
            String targetName
    ) {
        game.informPlayers(sourceObject.getLogName() + ": " + controller.getLogName() + " puts "
                + counter.getCount() + ' ' + counter.getName()
                + (counter.getCount() == 1 ? " counter" : " counters") + " on " + targetName);
    }

    private void announceCounterPlacement(
            Game game,
            Player controller,
            int amount,
            String counterName,
            String targetName
    ) {
        game.informPlayers(controller.getLogName() + " puts " + amount + ' ' + counterName
                + (amount == 1 ? " counter" : " counters") + " on " + targetName);
    }

    @SuppressWarnings("unchecked")
    private List<UUID> getAppliedEffects() {
        return (List<UUID>) this.getValue("appliedEffects");
    }
}

