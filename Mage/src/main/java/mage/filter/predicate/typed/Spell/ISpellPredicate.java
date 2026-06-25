package mage.filter.predicate.typed.Spell;

import mage.filter.predicate.TypedPredicate;
import mage.game.stack.Spell;

public interface ISpellPredicate extends TypedPredicate<Spell> {

    @Override
    default Class<Spell> getObjectClass() {
        return Spell.class;
    }
}
