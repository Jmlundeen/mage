package mage.abilities.mana.providers;

import mage.abilities.Ability;
import mage.abilities.effects.Effect;
import mage.game.Game;
import mage.players.Player;
import mage.util.Copyable;

import java.io.Serializable;

/**
 * Resolves player who receives mana from composed mana effects.
 * Also resolves player who makes mana choices during resolution.
 */
@FunctionalInterface
public interface ManaPlayerProvider extends Serializable, Copyable<ManaPlayerProvider> {

    Player getManaPlayer(Game game, Ability source, Effect effect);

    default Player getChoicePlayer(Game game, Ability source, Effect effect) {
        return getManaPlayer(game, source, effect);
    }

    @Override
    default ManaPlayerProvider copy() {
        return this;
    }
}
