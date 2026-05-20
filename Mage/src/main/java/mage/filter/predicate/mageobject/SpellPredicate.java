package mage.filter.predicate.mageobject;

import mage.MageObject;
import mage.filter.predicate.Predicate;
import mage.game.Game;
import mage.game.stack.Spell;

public enum SpellPredicate implements Predicate<MageObject> {
    instance;

    @Override
    public boolean apply(MageObject input, Game game) {
        return input instanceof Spell;
    }

    @Override
    public String toString() {
        return "Spell";
    }
}
