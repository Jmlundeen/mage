package mage.abilities.effects.common.continuous.replacement;

import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.SourceXCostValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.constants.ContinuousAffected;
import mage.constants.Duration;
import mage.constants.MultiAmountType;
import mage.constants.Outcome;
import mage.counters.Counter;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class EntersWithCountersEffect extends ReplacementEffectImpl {

    /**
     * Functional interface for helping to filter events.
     */
    public interface EventCondition {
        boolean apply(GameEvent event, Ability source, Game game, Effect effect);
    }

    protected ContinuousAffected affected;
    protected List<Counter> counters = new ArrayList<>();
    protected List<CounterType> counterTypes = new ArrayList<>();
    protected DynamicValue amount;
    protected boolean useXText = false;
    protected boolean useNumberOfText = false;
    protected boolean chooseCounter = false;
    protected int chooseAmount = 1;
    protected EventCondition eventCondition;
    protected FilterPermanent filter;

    /**
     * Constructor for a single predefined counter on the source permanent.
     *
     * @param counter The counter to add
     */
    public EntersWithCountersEffect(Counter counter) {
        this(ContinuousAffected.SOURCE, counter);
    }

    /**
     * Constructor for a single predefined counter with a specified affected entity. Typically
     * used with {@link ContinuousAffected#STATIC_OR_DYNAMIC}
     *
     * @param affected {@link ContinuousAffected}
     * @param counter  The counter to add
     */
    public EntersWithCountersEffect(ContinuousAffected affected, Counter counter) {
        this(Duration.WhileOnBattlefield, affected, counter);
    }

    /**
     * Constructor for a single predefined counter with a specified duration and affected entity.
     *
     * @param duration The duration of the effect
     * @param affected {@link ContinuousAffected}
     * @param counter  The counter to add
     */
    public EntersWithCountersEffect(Duration duration, ContinuousAffected affected, Counter counter) {
        super(duration, Outcome.BoostCreature, affected == ContinuousAffected.SOURCE);
        this.affected = affected;
        this.counters.add(counter);
    }

    /**
     * Constructor for dynamic counters based on a counter type and dynamic value for the source permanent.
     *
     * @param counterType The type of counter
     * @param amount      The dynamic value for the number of counters
     */
    public EntersWithCountersEffect(CounterType counterType, DynamicValue amount) {
        this(ContinuousAffected.SOURCE, counterType, amount);
    }

    /**
     * Constructor for dynamic counters with a specified affected entity. Typically
     * used with {@link ContinuousAffected#STATIC_OR_DYNAMIC}
     *
     * @param affected    {@link ContinuousAffected}
     * @param counterType The type of counter
     * @param amount      The dynamic value for the number of counters
     */
    public EntersWithCountersEffect(ContinuousAffected affected, CounterType counterType, DynamicValue amount) {
        this(Duration.WhileOnBattlefield, affected, counterType, amount);
    }

    /**
     * Constructor for dynamic counters with a specified duration and affected entity.
     *
     * @param duration    The duration of the effect.
     * @param affected    {@link ContinuousAffected}
     * @param counterType The type of counter.
     * @param amount      The dynamic value for the number of counters.
     */
    public EntersWithCountersEffect(Duration duration, ContinuousAffected affected, CounterType counterType, DynamicValue amount) {
        super(duration, Outcome.BoostCreature, affected == ContinuousAffected.SOURCE);
        this.affected = affected;
        this.counterTypes.add(counterType);
        this.amount = amount;
    }

    protected EntersWithCountersEffect(final EntersWithCountersEffect effect) {
        super(effect);
        this.affected = effect.affected;
        this.counterTypes.addAll(effect.counterTypes);
        this.counters.addAll(effect.counters);
        this.amount = effect.amount;
        this.useXText = effect.useXText;
        this.useNumberOfText = effect.useNumberOfText;
        this.chooseCounter = effect.chooseCounter;
        this.chooseAmount = effect.chooseAmount;
        this.eventCondition = effect.eventCondition;
        this.filter = effect.filter;
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Permanent permanent = game.getPermanent(event.getTargetId());
        if (permanent == null) {
            permanent = game.getPermanentEntering(event.getTargetId());
        }
        if (permanent == null) {
            return false;
        }
        if (!counters.isEmpty()) {
            if (chooseCounter) {
                Player controller = game.getPlayer(source.getControllerId());
                List<String> choices = counters.stream().map(Counter::getName).collect(Collectors.toList());
                List<Integer> chosen = controller.getMultiAmount(Outcome.BoostCreature, choices, 1, chooseAmount, chooseAmount, MultiAmountType.ENTER_WITH_COUNTERS, game);
                for (int i = 0; i < chosen.size(); i++) {
                    Counter counter = counters.get(i);
                    game.addEnterWithCounters(permanent.getId(), counter);
                }
                return false;
            }
            for (Counter counter : counters) {
                game.addEnterWithCounters(permanent.getId(), counter);
            }
        } else if (!counterTypes.isEmpty()) {
            int amountValue = amount.calculate(game, source, this);
            if (chooseCounter) {
                Player controller = game.getPlayer(source.getControllerId());
                List<String> choices = counterTypes.stream().map(CounterType::getName).collect(Collectors.toList());
                List<Integer> chosen = controller.getMultiAmount(Outcome.BoostCreature, choices, 1, chooseAmount, chooseAmount, MultiAmountType.ENTER_WITH_COUNTERS, game);
                for (int i = 0; i < chosen.size(); i++) {
                    CounterType counterType = counterTypes.get(i);
                    if (chosen.get(i) > 0) {
                        game.addEnterWithCounters(permanent.getId(), counterType.createInstance(amountValue));
                    }
                }
                return false;
            }
            for (CounterType counterType : counterTypes) {
                if (amountValue > 0) {
                    game.addEnterWithCounters(permanent.getId(), counterType.createInstance(amountValue));
                }
            }
        }
        return false;
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ENTERS_THE_BATTLEFIELD;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        switch (affected) {
            case SOURCE:
                if (!event.getTargetId().equals(source.getSourceId())) {
                    return false;
                }
                break;
            case STATIC_OR_DYNAMIC:
                for (UUID targetId : getTargetPointer().getTargets(game, source)) {
                    if (event.getTargetId().equals(targetId)) {
                        return true;
                    }
                }
                if (filter != null) {
                    Permanent permanent = game.getPermanentEntering(event.getTargetId());
                    if (permanent == null || !filter.match(permanent, source.getControllerId(), source, game)) {
                        return false;
                    }
                }
                break;
            default:
                return false;
        }
        if (eventCondition != null) {
            return eventCondition.apply(event, source, game, this);
        }
        return true;
    }

    @Override
    public EntersWithCountersEffect copy() {
        return new EntersWithCountersEffect(this);
    }

    /**
     * Sets a filter for the affected permanents.
     */
    public EntersWithCountersEffect setFilter(FilterPermanent filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Adds a counter type to the effect.
     */
    public EntersWithCountersEffect withAdditionalCounters(CounterType counterType) {
        this.counterTypes.add(counterType);
        return this;
    }

    /**
     * Adds a predefined counter to the effect.
     */
    public EntersWithCountersEffect withAdditionalCounters(Counter counter) {
        this.counters.add(counter);
        return this;
    }

    /**
     * Enables the use of "where X is" in the effect text.
     */
    public EntersWithCountersEffect withXText() {
        this.useXText = true;
        return this;
    }

    /**
     * Enables the use of "number of" in the effect text.
     */
    public EntersWithCountersEffect withNumberOfText() {
        this.useNumberOfText = true;
        return this;
    }

    /**
     * Sets a custom event condition for the effect.
     */
    public EntersWithCountersEffect withEventCondition(EventCondition eventCondition) {
        this.eventCondition = eventCondition;
        return this;
    }

    /**
     * Enables the controller to choose the counter type.
     */
    public EntersWithCountersEffect withChooseCounter() {
        this.chooseCounter = true;
        return this;
    }

    /**
     * Enables the controller to choose the counter type, specifying the amount of counters to choose.
     */
    public EntersWithCountersEffect withChooseCounter(int amount) {
        this.chooseCounter = true;
        this.chooseAmount = amount;
        return this;
    }

    @Override
    public String getText(Mode mode) {
        if (staticText != null && !staticText.isEmpty()) {
            return staticText;
        }
        StringBuilder sb = new StringBuilder();
        appendPrefix(sb);

        if (!counters.isEmpty()) {
            appendCounters(sb);
        } else if (!counterTypes.isEmpty()) {
            appendCounterTypes(sb);
        }
        return sb.toString();
    }

    private void appendPrefix(StringBuilder sb) {
        if (affected == ContinuousAffected.SOURCE) {
            sb.append("{this} enters with ");
        } else if (affected == ContinuousAffected.STATIC_OR_DYNAMIC) {
            sb.append(filter != null
                    ? "each " + filter.getMessage() + " enters with "
                    : "it enters with ");
        }
    }

    private void appendCounters(StringBuilder sb) {
        String additional = getAdditionalText();

        if (chooseCounter && chooseAmount > 1) {
            sb.append("your choice of ")
                    .append(CardUtil.numberToText(chooseAmount))
                    .append(" different counters on it from among ");
            for (int i = 0; i < counters.size(); i++) {
                if (i > 0) {
                    sb.append(i == counters.size() - 1 ? ", and " : ", ");
                }
                sb.append(counters.get(i).getName());
            }
            return;
        }

        if (chooseCounter) {
            sb.append("your choice of ");
        }
        for (int i = 0; i < counters.size(); i++) {
            appendSeparator(sb, i, counters.size());
            Counter counter = counters.get(i);
            String forOneText = affected == ContinuousAffected.STATIC_OR_DYNAMIC
                    ? "an"
                    : CounterType.findArticle(counter.getName());
            String add = affected == ContinuousAffected.STATIC_OR_DYNAMIC ? additional : " ";
            sb.append(CardUtil.numberToText(counter.getCount(), forOneText))
                    .append(add)
                    .append(counter.getName())
                    .append(" counter")
                    .append(counter.getCount() == 1 ? "" : "s");
        }
        sb.append(" on it");
    }

    private void appendCounterTypes(StringBuilder sb) {
        boolean xText = (useXText || amount instanceof SourceXCostValue) && !useNumberOfText;
        String additional = getAdditionalText();

        if (chooseCounter && chooseAmount > 1) {
            sb.append("your choice of ")
                    .append(CardUtil.numberToText(chooseAmount))
                    .append(" different counters on it from among ");
            for (int i = 0; i < counterTypes.size(); i++) {
                if (i > 0) {
                    sb.append(i == counterTypes.size() - 1 ? ", and " : ", ");
                }
                sb.append(counterTypes.get(i).getName());
            }
            return;
        }

        if (chooseCounter) {
            sb.append("your choice of ");
        }

        for (int i = 0; i < counterTypes.size(); i++) {
            appendSeparator(sb, i, counterTypes.size());
            CounterType type = counterTypes.get(i);
            if (xText) {
                sb.append("X ").append(type.getName()).append(" counters");
            } else {
                String forOneText = affected == ContinuousAffected.STATIC_OR_DYNAMIC
                        ? "an"
                        : type.getArticle();
                String add = affected == ContinuousAffected.STATIC_OR_DYNAMIC ? additional : " ";
                sb.append(useNumberOfText ? "a number of" : CardUtil.numberToText(amount.getSign(), forOneText))
                        .append(add)
                        .append(type.getName())
                        .append(" counter")
                        .append(amount.getSign() > 1 || useNumberOfText ? "s" : "");
            }
        }
        sb.append(" on it");
        appendAmountSuffix(sb, xText);
    }

    private void appendSeparator(StringBuilder sb, int index, int size) {
        if (chooseCounter) {
            if (index > 0) {
                sb.append(index == size - 1 ? " or " : ", ");
            }
            return;
        }
        if (index > 0) {
            sb.append(index == size - 1 ? ", and " : ", ");
        }
    }

    private void appendAmountSuffix(StringBuilder sb, boolean xText) {
        if (xText && !(amount instanceof SourceXCostValue)) {
            sb.append(", where X is ").append(amount.getMessage());
        } else if (useNumberOfText) {
            sb.append(" equal to the number of ").append(makePlural(amount.getMessage()));
        } else if (!xText) {
            sb.append(" for each ").append(amount.getMessage());
        }
    }

    private String makePlural(String message) {
        return message.replace("creature", "creatures");
    }

    private String getAdditionalText() {
        return useNumberOfText ? " number of additional " : " additional ";
    }

    @Override
    public EntersWithCountersEffect setText(String staticText) {
        return (EntersWithCountersEffect) super.setText(staticText);
    }
}
