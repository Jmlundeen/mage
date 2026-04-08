package mage.abilities.mana.value;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.effects.Effect;
import mage.constants.ManaType;
import mage.game.Game;
import mage.util.Copyable;

import java.util.List;
import java.util.Set;

/**
 * Interface for mana value types that can be composed together in a ComposedManaAbility.
 * 
 * <p>Each implementation defines how mana is calculated:
 * <ul>
 *   <li>{@link StaticManaValue} - fixed amounts that don't change</li>
 *   <li>{@link DynamicManaValue} - amounts derived from game state</li>
 *   <li>{@link AnyColorManaValue} - special case for any-color mana</li>
 * </ul>
 */
public interface ManaValue extends Copyable<ManaValue> {

    /**
     * Evaluates this mana value in the given game state.
     *
     * @param game        the current game (may be null for AI score calculations)
     * @param source      the ability producing this mana
     * @param manaEffect
     * @param produceMana
     * @return list of possible mana produced; may contain multiple options for choice-based values
     */
    List<Mana> evaluate(Game game, Ability source, Effect manaEffect, boolean produceMana);

    /**
     * Returns the set of mana types this value can produce.
     * Used for determining producible mana types without full evaluation.
     * 
     * @return set of producible mana types
     */
    Set<ManaType> getProducibleTypes();

    /**
     * Returns true if this value is static (does not depend on game state).
     * Static values can be cached for net mana calculations.
     * 
     * @return true if static, false if dynamic
     */
    default boolean isStatic() {
        return false;
    }

    @Override
    ManaValue copy();
}
