package mage.filter.predicate.typed.permanent;

import mage.filter.predicate.TypedPredicate;
import mage.game.permanent.Permanent;

public interface IPermanentPredicate extends TypedPredicate<Permanent> {

    @Override
    default Class<Permanent> getObjectClass() {
        return Permanent.class;
    }
}
