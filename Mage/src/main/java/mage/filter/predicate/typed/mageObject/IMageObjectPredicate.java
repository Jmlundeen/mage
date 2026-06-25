package mage.filter.predicate.typed.mageObject;

import mage.MageObject;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.Predicate;
import mage.filter.predicate.TypedPredicate;
import mage.game.Game;

public interface IMageObjectPredicate extends TypedPredicate<MageObject> {

    @Override
    default Class<MageObject> getObjectClass() {
        return MageObject.class;
    }

    static TypedPredicate<MageObject> getOSPPredicate(Predicate<MageObject> predicate) {
        return new TypedPredicate<MageObject>() {
            @Override
            public Class<MageObject> getObjectClass() {
                return MageObject.class;
            }

            @Override
            public boolean apply(ObjectSourcePlayer<MageObject> input, Game game) {
                return predicate.apply(input.getObject(), game);
            }
        };
    }
}
