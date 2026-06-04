package mage.filter.predicate.typed.permanent;

import mage.filter.predicate.ObjectSourcePlayer;
import mage.game.Game;
import mage.game.permanent.Permanent;

public enum PermanentPredicate implements IPermanentPredicate{
    instance;

    @Override
    public boolean apply(ObjectSourcePlayer<Permanent> input, Game game) {
        return input.getObject() != null;
    }
}
