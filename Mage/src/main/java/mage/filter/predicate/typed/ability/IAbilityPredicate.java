package mage.filter.predicate.typed.ability;

import mage.abilities.Ability;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.TypedPredicate;

public interface IAbilityPredicate extends TypedPredicate<ObjectSourcePlayer<Ability>> {

    @Override
    default Class<?> getObjectClass() {
        return Ability.class;
    }
}
