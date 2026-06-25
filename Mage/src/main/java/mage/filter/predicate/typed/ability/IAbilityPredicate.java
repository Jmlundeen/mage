package mage.filter.predicate.typed.ability;

import mage.abilities.Ability;
import mage.filter.predicate.TypedPredicate;

public interface IAbilityPredicate extends TypedPredicate<Ability> {

    @Override
    default Class<Ability> getObjectClass() {
        return Ability.class;
    }
}
