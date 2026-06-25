package mage.filter.predicate;

public interface TypedPredicate<T> extends ObjectSourcePlayerPredicate<T> {
    Class<T> getObjectClass();

    /**
     * Checks if the input object is of the expected type
     */
    default boolean canApply(ObjectSourcePlayer<?> input) {
        return getObjectClass().isInstance(input.getObject());
    }
}
