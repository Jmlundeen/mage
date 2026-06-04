package mage.filter.predicate.typed.mageObject.color;

import mage.MageObject;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.game.Game;

public enum ColorPredicate implements IMageObjectPredicate {

    WHITE, BLUE, BLACK, RED, GREEN;

    @Override
    public boolean apply(ObjectSourcePlayer<MageObject> input, Game game) {
        return switch (this) {
            case WHITE -> input.getObject().getColor(game).isWhite();
            case BLUE -> input.getObject().getColor(game).isBlue();
            case BLACK -> input.getObject().getColor(game).isBlack();
            case RED -> input.getObject().getColor(game).isRed();
            case GREEN -> input.getObject().getColor(game).isGreen();
        };
    }
}
