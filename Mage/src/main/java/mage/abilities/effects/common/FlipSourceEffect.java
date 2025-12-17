package mage.abilities.effects.common;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.constants.Outcome;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

/**
 * @author Loki
 */
public class FlipSourceEffect extends OneShotEffect {

    public FlipSourceEffect() {
        super(Outcome.BecomeCreature);
        staticText = "flip {this}";
    }

    protected FlipSourceEffect(final FlipSourceEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        Player controller = game.getPlayer(source.getControllerId());
        if (permanent != null && controller != null) {
            return permanent.flip(game);
        }
        return false;
    }

    @Override
    public FlipSourceEffect copy() {
        return new FlipSourceEffect(this);
    }

}
