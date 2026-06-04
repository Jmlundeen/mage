package mage.filter.predicate.typed.mageObject;

import mage.MageObject;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.game.Game;

public enum MageObjectPredicate implements IMageObjectPredicate {
    instance;

    @Override
    public boolean apply(ObjectSourcePlayer<MageObject> input, Game game) {
        return input.getObject() != null;
    }
}
