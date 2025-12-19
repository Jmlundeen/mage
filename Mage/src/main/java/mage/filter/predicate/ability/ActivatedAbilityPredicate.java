package mage.filter.predicate.ability;

import mage.abilities.Ability;
import mage.filter.predicate.Predicate;
import mage.game.Game;

public enum ActivatedAbilityPredicate implements Predicate<Ability> {
    instance;

    @Override
    public boolean apply(Ability input, Game game) {
        return input.isActivatedAbility();
    }

    @Override
    public String toString() {
        return "Activated ability";
    }
}
