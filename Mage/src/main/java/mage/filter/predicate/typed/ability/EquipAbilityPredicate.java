package mage.filter.predicate.typed.ability;

import mage.abilities.Ability;
import mage.abilities.keyword.EquipAbility;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.game.Game;

public enum EquipAbilityPredicate implements IAbilityPredicate {
    instance;

    @Override
    public boolean apply(ObjectSourcePlayer<Ability> input, Game game) {
        return input.getObject() instanceof EquipAbility;
    }
}
