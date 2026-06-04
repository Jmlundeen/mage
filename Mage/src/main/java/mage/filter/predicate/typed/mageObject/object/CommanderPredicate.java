package mage.filter.predicate.typed.mageObject.object;

import mage.MageObject;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.game.Game;
import mage.players.Player;

public enum CommanderPredicate implements IMageObjectPredicate {
    instance;

    @Override
    public boolean apply(ObjectSourcePlayer<MageObject> input, Game game) {
        Player owner = game.getPlayer(game.getOwnerId(input.getObject()));
        return owner != null && game.isCommanderObject(owner, input.getObject());
    }
}
