package mage.filter.predicate.typed.Spell.ability;

import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.typed.Spell.ISpellPredicate;
import mage.game.Game;
import mage.game.stack.Spell;

public class SpellHasAbilityPredicate implements ISpellPredicate {

    private final Class<?> abilityClass;

    public SpellHasAbilityPredicate(Class<?> abilityClass) {
        this.abilityClass = abilityClass;
    }

    @Override
    public boolean apply(ObjectSourcePlayer<Spell> input, Game game) {
        return input.getObject() != null && input.getObject().getAbilities(game).stream()
                .anyMatch(abilityClass::isInstance);
    }
}
