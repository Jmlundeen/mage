package mage.filter.predicate;

import mage.game.Game;

public interface TypedPredicate<T extends ObjectSourcePlayer<?>> {

    Class<?> getObjectClass();

    boolean apply(T input, Game game);

    /**
     * Checks if the input object is of the expected type
     */
    default boolean canApply(ObjectSourcePlayer<?> input) {
        return getObjectClass().isInstance(input.getObject());
    }

    /**
     * Attempts to apply the predicate if the input object is of the expected type. If not, it returns true (predicate does not apply).
     * @param input
     * @param game
     * @return
     */
    @SuppressWarnings("unchecked")
    default boolean tryApply(ObjectSourcePlayer<?> input, Game game) {
        if (!canApply(input)) {
            return true; // If the object is not of the expected type, we consider it a match (predicate does not apply)
        }
        return apply((T) input, game);
    }

    /**
     * Attempts to apply the predicate if the input object is of the expected type. If not, it returns false (predicate does not match).
     * @param input
     * @param game
     * @return
     */
    @SuppressWarnings("unchecked")
    default boolean forceApply(ObjectSourcePlayer<?> input, Game game) {
        if (!canApply(input)) {
            return false;
        }
        return apply((T) input, game);
    }
}
