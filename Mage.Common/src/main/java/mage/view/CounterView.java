package mage.view;

import mage.counters.Counter;
import mage.ws.view.ViewProto;

import java.io.Serializable;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class CounterView implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private int count;

    public CounterView(String name, int count) {
        this.name = name;
        this.count = count;
    }

    public CounterView(Counter counter) {
        this.name = counter.getName();
        this.count = counter.getCount();
    }

    public CounterView(final CounterView view) {
        this.name = view.name;
        this.count = view.count;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public ViewProto.CounterView toProto() {
        return ViewProto.CounterView.newBuilder()
                .setName(name)
                .setCount(count)
                .build();
    }

    public static CounterView fromProto(ViewProto.CounterView proto) {
        return new CounterView(proto.getName(), proto.getCount());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof CounterView)) {
            return false;
        }
        CounterView oth = (CounterView) other;
        return
                (count == oth.count) &&
                        (name.equals(oth.name));
    }
}
