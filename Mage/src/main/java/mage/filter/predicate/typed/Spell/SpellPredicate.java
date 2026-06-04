package mage.filter.predicate.typed.Spell;

import mage.filter.predicate.ObjectSourcePlayer;
import mage.game.Game;
import mage.game.stack.Spell;

public enum SpellPredicate implements ISpellPredicate {
    instance;

    @Override
    public boolean apply(ObjectSourcePlayer<Spell> input, Game game) {
        return input.getObject() != null;
    }
}
