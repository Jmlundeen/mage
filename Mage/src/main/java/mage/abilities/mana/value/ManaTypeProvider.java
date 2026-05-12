package mage.abilities.mana.value;

import mage.abilities.Ability;
import mage.abilities.effects.Effect;
import mage.constants.ManaType;
import mage.game.Game;
import mage.util.Copyable;

import java.io.Serializable;
import java.util.Set;

/**
 * Resolves mana types at runtime for composed mana values.
 */
@FunctionalInterface
public interface ManaTypeProvider extends Serializable, Copyable<ManaTypeProvider> {

    Set<ManaType> getManaTypes(Game game, Ability source, Effect effect);

    @Override
    default ManaTypeProvider copy() {
        return this;
    }
}
