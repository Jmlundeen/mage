package mage.abilities.effects.common.continuous.layers.L6_Abilities;

import mage.abilities.Ability;
import mage.game.Game;

@FunctionalInterface
public interface ModifyAbilityFunction {
    void apply(Game game, Ability source, Ability abilityToModify);
}
