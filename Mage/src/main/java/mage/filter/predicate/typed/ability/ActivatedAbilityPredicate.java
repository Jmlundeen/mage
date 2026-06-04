package mage.filter.predicate.typed.ability;

import mage.abilities.Ability;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.game.Game;

public enum ActivatedAbilityPredicate implements IAbilityPredicate {
    instance;

    @Override
    public boolean apply(ObjectSourcePlayer<Ability> input, Game game) {
        return input.getObject().isActivatedAbility();
    }
}
