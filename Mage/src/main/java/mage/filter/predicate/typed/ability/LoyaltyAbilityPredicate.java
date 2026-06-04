package mage.filter.predicate.typed.ability;

import mage.abilities.Ability;
import mage.abilities.LoyaltyAbility;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.game.Game;

public enum LoyaltyAbilityPredicate implements IAbilityPredicate {
    instance;

    @Override
    public boolean apply(ObjectSourcePlayer<Ability> input, Game game) {
        return input.getObject() instanceof LoyaltyAbility;
    }
}
