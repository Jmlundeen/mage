package mage.filter.predicate.typed.Spell;

import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.TypedPredicate;
import mage.game.stack.Spell;

public interface ISpellPredicate extends TypedPredicate<ObjectSourcePlayer<Spell>> {

    @Override
    default Class<?> getObjectClass() {
        return Spell.class;
    }
}
