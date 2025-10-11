package mage.filter.predicate.ability;

import mage.abilities.Ability;
import mage.abilities.LoyaltyAbility;
import mage.filter.predicate.Predicate;
import mage.game.Game;

public enum LoyaltyAbilityPredicate implements Predicate<Ability> {
    instance;

    @Override
    public boolean apply(Ability input, Game game) {
        return input instanceof LoyaltyAbility;
    }

    @Override
    public String toString() {
        return "Loyalty ability";
    }
}
