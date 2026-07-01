package mage.filter;

import mage.abilities.Ability;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.Predicate;
import mage.filter.predicate.TypedPredicate;
import mage.filter.predicate.typed.LogicalPredicate;
import mage.game.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FilterTyped implements Filter<ObjectSourcePlayer<?>> {

    private final List<TypedPredicate<?>> predicates = new ArrayList<>();
    private String message;
    private boolean locked;

    public FilterTyped(String message) {
        this.message = message;
    }

    protected FilterTyped(final FilterTyped filter) {
        this.message = filter.message;
        this.locked = false; // filter can be changed after copy
        this.predicates.addAll(filter.predicates);
    }

    public FilterTyped add(TypedPredicate<?> predicate) {
        if (locked) {
            throw new UnsupportedOperationException("Filter is locked and cannot be modified");
        }
        this.predicates.add(predicate);
        return this;
    }

    public FilterTyped addAll(TypedPredicate<?>... predicates) {
        return add(LogicalPredicate.and(predicates));
    }

    public FilterTyped addAny(TypedPredicate<?>... predicates) {
        return add(LogicalPredicate.or(predicates));
    }

    public <T> boolean match(T object, UUID controllerId, Ability source, Game game) {
        return match(new ObjectSourcePlayer<>(object, controllerId, source), game);
    }

    @SuppressWarnings("unchecked")
    public boolean match(ObjectSourcePlayer<?> input, Game game) {
        if (predicates.isEmpty()) {
            return true;
        }

        boolean applied = false;
        for (TypedPredicate<?> predicate : predicates) {
            if (!predicate.canApply(input)) {
                continue;
            }
            applied = true;
            if (!((TypedPredicate) predicate).apply(input, game)) {
                return false;
            }
        }
        return applied;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Filter<ObjectSourcePlayer<?>> add(Predicate<? super ObjectSourcePlayer<?>> predicate) {
        if (predicate instanceof TypedPredicate) {
            this.predicates.add((TypedPredicate) predicate);
        }
        return this;
    }

    @Override
    public boolean checkObjectClass(Object object) {
        for (TypedPredicate<?> predicate : predicates) {
            if (predicate.getObjectClass().isInstance(object)) {
                return true;
            }
        }
        return false;
    }

    public boolean canApplyToClass(Class<?> clazz) {
        for (TypedPredicate<?> predicate : predicates) {
            if (predicate.getObjectClass().isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public FilterTyped copy() {
        return new FilterTyped(this);
    }

    @Override
    public boolean isLockedFilter() {
        return false;
    }

    @Override
    public void setLockedFilter(boolean lockedFilter) {

    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Predicate<? super ObjectSourcePlayer<?>>> getPredicates() {
        return (List<Predicate<? super ObjectSourcePlayer<?>>>) (List<?>) predicates;
    }

    protected FilterTyped setLocked(boolean locked) {
        this.locked = locked;
        return this;
    }
}
