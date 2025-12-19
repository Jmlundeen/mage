package mage.abilities.effects.common.continuous.generic;

import mage.abilities.Ability;
import mage.cards.Card;
import mage.game.Game;

@FunctionalInterface
public interface MakeAbilityFunction {
    Ability makeAbility(Card object, Ability source, Game game);
}
