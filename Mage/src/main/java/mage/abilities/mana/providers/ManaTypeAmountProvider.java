package mage.abilities.mana.providers;

import mage.abilities.Ability;
import mage.abilities.effects.Effect;
import mage.constants.ManaType;
import mage.game.Game;
import mage.util.Copyable;

import java.io.Serializable;
import java.util.Map;

/**
 * Resolves mana amounts for each possible mana type at runtime.
 */
@FunctionalInterface
public interface ManaTypeAmountProvider extends Serializable, Copyable<ManaTypeAmountProvider> {

    Map<ManaType, Integer> getManaAmounts(Game game, Ability source, Effect effect);

    @Override
    default ManaTypeAmountProvider copy() {
        return this;
    }
}
