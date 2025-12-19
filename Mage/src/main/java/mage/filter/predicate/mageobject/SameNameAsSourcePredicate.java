package mage.filter.predicate.mageobject;

import mage.MageObject;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.ObjectSourcePlayerPredicate;
import mage.game.Game;
import mage.util.CardUtil;

public enum SameNameAsSourcePredicate implements ObjectSourcePlayerPredicate<MageObject> {
    instance,
    NOT;

    @Override
    public boolean apply(ObjectSourcePlayer<MageObject> input, Game game) {
        MageObject source = game.getObject(input.getSourceId());
        if (this == NOT) {
            return source != null && !CardUtil.haveSameNames(input.getObject(), source);
        }
        return source != null && CardUtil.haveSameNames(input.getObject(), source);
    }

    @Override
    public String toString() {
        return "has the same name as {this}";
    }
}
