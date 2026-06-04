package mage.filter.predicate.typed.mageObject.object;

import mage.MageObject;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.game.Game;
import mage.util.CardUtil;

public enum SameNameAsSourcePredicate implements IMageObjectPredicate {
    instance;

    @Override
    public boolean apply(ObjectSourcePlayer<MageObject> input, Game game) {
        MageObject sourceObject = game.getObject(input.getSourceId());

        return sourceObject != null && CardUtil.haveSameNames(input.getObject(), sourceObject);
    }
}
