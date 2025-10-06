package mage.abilities.dynamicvalue.common;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.counters.Counter;
import mage.counters.CounterType;
import mage.counters.Counters;
import mage.game.Game;
import mage.game.permanent.Permanent;

/**
 * Counts the number of counters on a given object
 *
 * @author Jmlundeen
 */
public class ObjectCountersCount implements DynamicValue {

    private final CounterType counterType;

    /**
     * Number of counters of any type on the source permanent
     */
    public static final ObjectCountersCount ANY = new ObjectCountersCount((CounterType) null);

    /**
     * Number of counters of the specified type on the source permanent
     */
    public ObjectCountersCount(CounterType counterType) {
        this.counterType = counterType;
    }

    protected ObjectCountersCount(final ObjectCountersCount countersCount) {
        this.counterType = countersCount.counterType;
    }

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect, MageObject mageObject) {
        if (mageObject == null) {
            return 0;
        }
        int count = 0;
        if (mageObject instanceof Permanent) {
        count = counterType == null ?
                ((Permanent) mageObject).getCounters(game).values().stream().mapToInt(Counter::getCount).sum()
                : ((Permanent) mageObject).getCounters(game).getCount(counterType);
        } else {
            Counters counters = game.getState().getCardState(mageObject.getId()).getCounters();
            count = counterType == null ?
                    counters.values().stream().mapToInt(Counter::getCount).sum()
                    : counters.getCount(counterType);
        }
        return count;
    }

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        return 0;
    }

    @Override
    public DynamicValue copy() {
        return new ObjectCountersCount(this);
    }

    @Override
    public String toString() {
        return "1";
    }

    @Override
    public String getMessage() {
        return (counterType != null ? counterType.toString() + ' ' : "") + "counter on it";
    }
}
