package mage.filter.predicate.typed.stackObject;

import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.TypedPredicate;
import mage.game.stack.StackObject;

public interface IStackObjectPredicate extends TypedPredicate<ObjectSourcePlayer<StackObject>> {

    @Override
    default Class<?> getObjectClass() {
        return StackObject.class;
    }
}
