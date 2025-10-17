
package mage.abilities.dynamicvalue.common;

import mage.abilities.Ability;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.abilities.hint.Hint;
import mage.abilities.hint.ValueHint;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;

/**
 * @author Styxo
 */
public class CountersCount implements DynamicValue {

    private final CounterType counter;
    private final FilterPermanent filter;
    private final Hint hint;
    private boolean useAmong = false;

    public CountersCount(CounterType counterType) {
        this(counterType, new FilterPermanent());
    }

    public CountersCount(CounterType counter, FilterPermanent filter) {
        this.counter = counter;
        this.filter = filter;
        this.hint = new ValueHint("Total " + counter.getName() + " counters on " + filter.getMessage(), this);
    }

    protected CountersCount(final CountersCount countersCount) {
        this.counter = countersCount.counter;
        this.filter = countersCount.filter;
        this.hint = countersCount.hint;
        this.useAmong = countersCount.useAmong;
    }

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        int count = 0;
        for (Permanent permanent : game.getBattlefield().getActivePermanents(filter, sourceAbility.getControllerId(), sourceAbility, game)) {
            count += permanent.getCounters(game).getCount(counter);
        }
        return count;
    }

    public Hint getHint() {
        return hint;
    }

    public CountersCount setUseAmong(boolean useAmong) {
        this.useAmong = useAmong;
        return this;
    }

    @Override
    public CountersCount copy() {
        return new CountersCount(this);
    }

    @Override
    public String toString() {
        return "1";
    }

    @Override
    public String getMessage() {
        return counter.getName() + " counter " + (useAmong ? "among " : "on ") + filter.getMessage();
    }
}
