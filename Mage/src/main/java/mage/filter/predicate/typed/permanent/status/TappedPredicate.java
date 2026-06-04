package mage.filter.predicate.typed.permanent.status;

import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.typed.permanent.IPermanentPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;

public enum TappedPredicate implements IPermanentPredicate {
    UNTAPPED(false),
    TAPPED(true);

    private final boolean tapped;

    TappedPredicate(boolean tapped) {
        this.tapped = tapped;
    }

    @Override
    public boolean apply(ObjectSourcePlayer<Permanent> input, Game game) {
        return input.getObject().isTapped() == tapped;
    }
}
