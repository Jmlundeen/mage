package mage.abilities.mana.providers;

import mage.abilities.Ability;
import mage.abilities.condition.Condition;
import mage.abilities.effects.Effect;
import mage.game.Game;
import mage.util.Copyable;

import java.io.Serializable;
import java.util.List;

/**
 * Resolves spending conditions for mana at evaluation/production time.
 */
@FunctionalInterface
public interface ManaConditionProvider extends Serializable, Copyable<ManaConditionProvider> {

    List<Condition> getConditions(Game game, Ability source, Effect effect);

    @Override
    default ManaConditionProvider copy() {
        return this;
    }
}

