package mage.filter.predicate.typed;

import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.TypedPredicate;
import mage.game.Game;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;

public final class LogicalPredicate implements TypedPredicate<Object> {

    private static final Logger logger = Logger.getLogger(LogicalPredicate.class);

    private enum Operator {
        AND, OR, NOT
    }

    private final Operator operator;
    private final List<TypedPredicate<?>> predicates;

    private LogicalPredicate(Operator operator, List<TypedPredicate<?>> predicates) {
        this.operator = operator;
        this.predicates = List.copyOf(predicates);
        checkTypeCompatibility();
    }

    private void checkTypeCompatibility() {
        if (predicates.size() < 2) {
            return;
        }
        Class<?> base = predicates.getFirst().getObjectClass();
        for (int i = 1; i < predicates.size(); i++) {
            Class<?> current = predicates.get(i).getObjectClass();
            if (base != current && !base.isAssignableFrom(current) && !current.isAssignableFrom(base)) {
                logger.warn("LogicalPredicate: incompatible predicate types " + base.getSimpleName()
                        + " and " + current.getSimpleName() + " — runtime fallback");
            }
        }
    }

    public static LogicalPredicate and(TypedPredicate<?>... predicates) {
        return new LogicalPredicate(Operator.AND, Arrays.asList(predicates));
    }

    public static LogicalPredicate and(TypedPredicate<?> predicate1, TypedPredicate<?> predicate2) {
        return new LogicalPredicate(Operator.AND, List.of(predicate1, predicate2));
    }

    public static LogicalPredicate or(TypedPredicate<?>... predicates) {
        return new LogicalPredicate(Operator.OR, Arrays.asList(predicates));
    }

    public static LogicalPredicate or(TypedPredicate<?> predicate1, TypedPredicate<?> predicate2) {
        return new LogicalPredicate(Operator.OR, List.of(predicate1, predicate2));
    }

    public static LogicalPredicate not(TypedPredicate<?>... predicates) {
        return new LogicalPredicate(Operator.NOT, Arrays.asList(predicates));
    }

    public static LogicalPredicate not(TypedPredicate<?> predicate) {
        return new LogicalPredicate(Operator.NOT, List.of(predicate));
    }

    public static LogicalPredicate none(TypedPredicate<?>... predicates) {
        return new LogicalPredicate(Operator.NOT, Arrays.asList(predicates));
    }

    @Override
    public Class<Object> getObjectClass() {
        return Object.class;
    }

    @Override
    public boolean canApply(ObjectSourcePlayer<?> input) {
        if (predicates.isEmpty()) {
            return false;
        }

        return switch (operator) {
            case AND -> predicates.stream().allMatch(predicate -> predicate.canApply(input));
            case OR, NOT -> predicates.stream().anyMatch(predicate -> predicate.canApply(input));
        };
    }

    @Override
    public boolean apply(ObjectSourcePlayer<Object> input, Game game) {
        return switch (operator) {
            case AND -> applyAnd(input, game);
            case OR -> applyOr(input, game);
            case NOT -> applyNot(input, game);
        };
    }

    @SuppressWarnings("unchecked")
    private boolean applyAnd(ObjectSourcePlayer<?> input, Game game) {
        for (TypedPredicate<?> predicate : predicates) {
            if (!predicate.canApply(input) || !((TypedPredicate) predicate).apply(input, game)) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean applyOr(ObjectSourcePlayer<?> input, Game game) {
        for (TypedPredicate<?> predicate : predicates) {
            if (predicate.canApply(input) && ((TypedPredicate) predicate).apply(input, game)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean applyNot(ObjectSourcePlayer<?> input, Game game) {
        for (TypedPredicate<?> predicate : predicates) {
            if (predicate.canApply(input) && ((TypedPredicate) predicate).apply(input, game)) {
                return false;
            }
        }
        return true;
    }
}
