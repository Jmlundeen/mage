package mage.filter.predicate.typed.mageObject;

import mage.MageObject;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.TypedPredicate;

public interface IMageObjectPredicate extends TypedPredicate<ObjectSourcePlayer<MageObject>> {

    @Override
    default Class<?> getObjectClass() {
        return MageObject.class;
    }
}
