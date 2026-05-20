package mage.filter.predicate.ability;

import mage.abilities.Ability;
import mage.abilities.keyword.EquipAbility;
import mage.filter.predicate.Predicate;
import mage.game.Game;

public enum EquipAbilityPredicate implements Predicate<Ability> {
    instance;

    @Override
    public boolean apply(Ability input, Game game) {
        return input instanceof EquipAbility;
    }

    @Override
    public String toString() {
        return "Equip ability";
    }
}
