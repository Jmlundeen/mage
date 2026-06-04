package mage.filter.predicate.typed.mageObject.color;

import mage.MageObject;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.game.Game;

public enum MultiColoredPredicate implements IMageObjectPredicate {
    instance;

    @Override
    public boolean apply(ObjectSourcePlayer<MageObject> input, Game game) {
        return input.getObject().getColor(game).isMulticolored();
    }
}
