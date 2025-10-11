package mage.abilities.effects.common.continuous.layers.L6_Abilities;

import mage.abilities.Ability;

@FunctionalInterface
public interface ModifyAbilityFunction {
    void apply(Ability abilityToModify);
}
