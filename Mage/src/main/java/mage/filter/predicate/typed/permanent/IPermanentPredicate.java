package mage.filter.predicate.typed.permanent;

import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.TypedPredicate;
import mage.game.permanent.Permanent;

public interface IPermanentPredicate extends TypedPredicate<ObjectSourcePlayer<Permanent>> {

    @Override
    default Class<?> getObjectClass() {
        return Permanent.class;
    }
}
