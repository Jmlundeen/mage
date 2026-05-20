package mage.abilities.mana.providers.common.player;

import mage.abilities.Ability;
import mage.abilities.effects.Effect;
import mage.abilities.mana.providers.ManaPlayerProvider;
import mage.game.Game;
import mage.players.Player;

import java.util.UUID;

/**
 * Uses effect target pointer to resolve mana recipient and chooser.
 */
public enum TargetPointerManaPlayerProvider implements ManaPlayerProvider {
    instance;

    @Override
    public Player getManaPlayer(Game game, Ability source, Effect effect) {
        if (game == null || source == null || effect == null || effect.getTargetPointer() == null) {
            return null;
        }
        UUID playerId = effect.getTargetPointer().getFirst(game, source);
        return playerId == null ? null : game.getPlayer(playerId);
    }
}
