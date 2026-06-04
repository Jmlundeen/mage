package mage.filter.predicate.typed.Spell.object;

import mage.abilities.keyword.KickerAbility;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.typed.Spell.ISpellPredicate;
import mage.game.Game;
import mage.game.stack.Spell;

public enum KickedSpellPredicate implements ISpellPredicate {
    instance;

    @Override
    public boolean apply(ObjectSourcePlayer<Spell> input, Game game) {
        return KickerAbility.getKickedCounter(game, input.getObject().getSpellAbility()) >= 0;
    }
}
