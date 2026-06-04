package mage.filter.predicate.typed.stackObject;

import mage.filter.predicate.ObjectSourcePlayer;
import mage.game.Game;
import mage.game.stack.StackObject;

public enum StackObjectPredicate implements IStackObjectPredicate {
    instance;

    @Override
    public boolean apply(ObjectSourcePlayer<StackObject> input, Game game) {
        return input.getObject() != null;
    }
}
