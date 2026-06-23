package mage.filter.predicate.typed.ability.type;

import mage.abilities.Ability;
import mage.abilities.keyword.ForetellAbility;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.typed.ability.IAbilityPredicate;
import mage.game.Game;

public enum ForetellAbilityPredicate implements IAbilityPredicate {
    instance;

    @Override
    public boolean apply(ObjectSourcePlayer<Ability> input, Game game) {
        return input.getObject() instanceof ForetellAbility;
    }
}
