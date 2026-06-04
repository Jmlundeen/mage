package mage.filter.predicate.typed;

import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.TypedPredicate;
import mage.game.Game;

import java.util.Arrays;
import java.util.List;

public final class LogicalPredicate implements TypedPredicate<ObjectSourcePlayer<?>> {

    private enum Operator {
        AND, OR, NOT
    }

    private final Operator operator;
    private final List<TypedPredicate<?>> predicates;

    private LogicalPredicate(Operator operator, List<TypedPredicate<?>> predicates) {
        this.operator = operator;
        this.predicates = List.copyOf(predicates);
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

    /**
     * Applies the logical operator to the predicates.
     * predicates
     * predic
     * @param input
     * @param game
     * @return
     */
    @Override
    public boolean apply(ObjectSourcePlayer<?> input, Game game) {
        return switch (operator) {
            case AND -> applyAnd(input, game);
            case OR -> applyOr(input, game);
            case NOT -> applyNot(input, game);
        };
    }

    private boolean applyAnd(ObjectSourcePlayer<?> input, Game game) {
        for (TypedPredicate<?> predicate : predicates) {
            if (!predicate.forceApply(input, game)) {
                return false;
            }
        }
        return true;
    }

    private boolean applyOr(ObjectSourcePlayer<?> input, Game game) {
        for (TypedPredicate<?> predicate : predicates) {
            if (predicate.forceApply(input, game)) {
                return true;
            }
        }
        return false;
    }

    private boolean applyNot(ObjectSourcePlayer<?> input, Game game) {
        for (TypedPredicate<?> predicate : predicates) {
            if (predicate.forceApply(input, game)) {
                return false;
            }
        }
        return true;
    }
}
