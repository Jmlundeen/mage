package mage.filter.predicate.ability;

import mage.abilities.Ability;
import mage.filter.predicate.Predicate;
import mage.game.Game;

public enum TriggeredAbilityPredicate implements Predicate<Ability> {
    instance;

    @Override
    public boolean apply(Ability input, Game game) {
        return input.isTriggeredAbility();
    }

    @Override
    public String toString() {
        return "Triggered ability";
    }
}
