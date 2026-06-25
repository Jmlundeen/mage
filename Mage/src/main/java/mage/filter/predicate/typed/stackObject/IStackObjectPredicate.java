package mage.filter.predicate.typed.stackObject;

import mage.filter.predicate.TypedPredicate;
import mage.game.stack.StackObject;

public interface IStackObjectPredicate extends TypedPredicate<StackObject> {

    @Override
    default Class<StackObject> getObjectClass() {
        return StackObject.class;
    }
}
