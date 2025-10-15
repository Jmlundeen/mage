package mage.abilities.effects.common.continuous.rulemodifying;

import mage.abilities.Ability;
import mage.abilities.effects.ContinuousRuleModifyingEffectImpl;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.TargetController;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PreventCountersEffect extends ContinuousRuleModifyingEffectImpl {

    protected int maxCounters = 0;

    // Counter type filters
    protected Set<CounterType> validCounterTypes = new HashSet<>();

    // Target filters
    protected TargetController eventController = TargetController.ANY;
    protected FilterPermanent permanentFilter;
    protected TargetController validPlayerTarget = TargetController.ANY;
    protected boolean targetPlayers = false;
    protected boolean targetPermanents = false;

    /**
     * Default constructor: prevents all counters from being placed.
     */
    public PreventCountersEffect() {
        this(Duration.WhileOnBattlefield, Outcome.Detriment, 0);
    }

    /**
     * Constructor to prevent specific counter types from being placed.
     * @param validCounterTypes the counter types to prevent
     */
    public PreventCountersEffect(CounterType... validCounterTypes) {
        this(Duration.WhileOnBattlefield, Outcome.Detriment, 0, validCounterTypes);
    }

    /**
     * Constructor to prevent counters from being placed if the target already has
     * a certain number of those counters.
     *
     * @param maxCounters the maximum number of counters allowed
     * @param validCounterTypes the counter types to check and prevent
     */
    public PreventCountersEffect(int maxCounters, CounterType... validCounterTypes) {
        this(Duration.WhileOnBattlefield, Outcome.Detriment, maxCounters, validCounterTypes);
    }

    /**
     * Main constructor.
     *
     * @param duration the duration of the effect
     * @param outcome the outcome of the effect
     * @param maxCounters the maximum number of counters allowed (0 to ignore)
     * @param validCounterTypes the counter types to check and prevent
     */
    public PreventCountersEffect(Duration duration, Outcome outcome, int maxCounters, CounterType... validCounterTypes) {
        super(duration, outcome);
        this.validCounterTypes.addAll(Arrays.asList(validCounterTypes));
        this.maxCounters = maxCounters;
    }

    private PreventCountersEffect(final PreventCountersEffect effect) {
        super(effect);
        this.maxCounters = effect.maxCounters;
        this.validCounterTypes = new HashSet<>(validCounterTypes);
        this.eventController = effect.eventController;
        this.permanentFilter = effect.permanentFilter;
        this.validPlayerTarget = effect.validPlayerTarget;
        this.targetPlayers = effect.targetPlayers;
        this.targetPermanents = effect.targetPermanents;
    }

    @Override
    public PreventCountersEffect copy() {
        return new PreventCountersEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        if (maxCounters > 0) {
            return event.getType() == GameEvent.EventType.ADD_COUNTER;
        }
        return event.getType() == GameEvent.EventType.ADD_COUNTERS;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        // Check if the counter type matches our filter
        if (!validCounterTypes.isEmpty()) {
            if (!isValidCounterType(event.getData())) {
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
            if (hasValidTarget && maxCounters > 0) {
                hasValidTarget = targetPlayer.getCountersCount(event.getData()) >= maxCounters;
            }
        } else if (targetPermanent != null && targetPermanents) {
            if (permanentFilter != null && !permanentFilter.match(targetPermanent, source.getControllerId(), source, game)) {
                return false;
            }
            if (maxCounters > 0) {
                hasValidTarget = targetPermanent.getCounters(game).getCount(event.getData()) >= maxCounters;
            } else {
                hasValidTarget = true;
            }
        }

        return hasValidTarget;
    }

    /**
     * Sets which player's counter placement is affected.
     *
     * @param eventController YOU, OPPONENT, or ANY
     */
    public PreventCountersEffect setEventController(TargetController eventController) {
        this.eventController = eventController;
        return this;
    }

    /**
     * Sets a filter for which permanents are affected. Also enables targeting permanents.
     *
     * @param filter the permanent filter
     */
    public PreventCountersEffect setPermanentFilter(FilterPermanent filter) {
        this.permanentFilter = filter;
        this.targetPermanents = true;
        return this;
    }

    /**
     * Sets which players can be affected by this effect. Also enables targeting players.
     *
     * @param validPlayerTarget YOU, OPPONENT, or ANY
     */
    public PreventCountersEffect setValidPlayerTarget(TargetController validPlayerTarget) {
        this.validPlayerTarget = validPlayerTarget;
        this.targetPlayers = true;
        return this;
    }

    /**
     * Sets whether this effect applies to players receiving counters.
     *
     * @param targetPlayers true if players can be affected
     */
    public PreventCountersEffect setTargetPlayers(boolean targetPlayers) {
        this.targetPlayers = targetPlayers;
        return this;
    }

    /**
     * Sets whether this effect applies to permanents receiving counters.
     *
     * @param targetPermanents true if permanents can be affected
     */
    public PreventCountersEffect setTargetPermanents(boolean targetPermanents) {
        this.targetPermanents = targetPermanents;
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

    public int getMaxCounters() {
        return maxCounters;
    }

    public Set<CounterType> getValidCounterTypes() {
        return validCounterTypes;
    }

    @Override
    public PreventCountersEffect setText(String staticText) {
        return (PreventCountersEffect) super.setText(staticText);
    }
}
