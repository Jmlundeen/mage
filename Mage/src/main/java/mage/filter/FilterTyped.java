package mage.filter;

import mage.MageObject;
import mage.abilities.Ability;
import mage.cards.Card;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.TypedPredicate;
import mage.filter.predicate.typed.LogicalPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.game.stack.StackObject;
import mage.players.Player;
import mage.util.Copyable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FilterTyped implements Copyable<FilterTyped> {

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

    public boolean match(Card card, UUID controllerId, Ability source, Game game) {
        return match(new ObjectSourcePlayer<>(card, controllerId, source), game);
    }

    public boolean match(Ability ability, UUID controllerId, Ability source, Game game) {
        return match(new ObjectSourcePlayer<>(ability, controllerId, source), game);
    }

    public boolean match(MageObject object, UUID controllerId, Ability source, Game game) {
        return match(new ObjectSourcePlayer<>(object, controllerId, source), game);
    }

    public boolean match(Spell spell, UUID controllerId, Ability source, Game game) {
        return match(new ObjectSourcePlayer<>(spell, controllerId, source), game);
    }

    public boolean match(Permanent permanent, UUID controllerId, Ability source, Game game) {
        return match(new ObjectSourcePlayer<>(permanent, controllerId, source), game);
    }

    public boolean match(StackObject stackObject, UUID controllerId, Ability source, Game game) {
        return match(new ObjectSourcePlayer<>(stackObject, controllerId, source), game);
    }

    public boolean match(Player player, UUID controllerId, Ability source, Game game) {
        return match(new ObjectSourcePlayer<>(player, controllerId, source), game);
    }

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
            if (!predicate.tryApply(input, game)) {
                return false;
            }
        }
        return applied;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }

    public FilterTyped setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public FilterTyped copy() {
        return new FilterTyped(this);
    }

    protected FilterTyped setLocked(boolean locked) {
        this.locked = locked;
        return this;
    }
}
