package mage.abilities.mana.value;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.abilities.mana.ComposedManaEffect;
import mage.constants.ManaType;
import mage.game.Game;
import mage.players.Player;
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

    default Player getChoicePlayer(Game game, Ability source, Effect manaEffect) {
        if (manaEffect instanceof ComposedManaEffect composedManaEffect) {
            return composedManaEffect.getChoicePlayer(game, source);
        }
        return game.getPlayer(source.getControllerId());
    }

    /**
     * Returns the set of mana types this value can produce.
     * Used for determining producible mana types without full evaluation.
     * 
     * @return set of producible mana types
     */
    Set<ManaType> getProducibleTypes();

    /**
     * Returns the set of mana types this value can produce in current runtime context.
     * Dynamic implementations may inspect effect state such as already-produced mana.
     *
     * @param game current game
     * @param source ability producing mana
     * @param manaEffect mana effect being evaluated
     * @return set of producible mana types for current context
     */
    default Set<ManaType> getProducibleTypes(Game game, Ability source, Effect manaEffect) {
        return getProducibleTypes();
    }

    default int calculateAmount(Game game, Ability source, Effect manaEffect, boolean produceMana, DynamicValue amount) {
        return calculateAmount(game, source, manaEffect, produceMana, amount, 0);
    }

    default int calculateAmount(Game game, Ability source, Effect manaEffect, boolean produceMana, DynamicValue amount, int baseAmount) {
        Integer overrideAmount = getPlayableAmountOverride(game, source, manaEffect, produceMana);
        if (overrideAmount != null) {
            return overrideAmount;
        }
        int calculatedAmount = baseAmount;
        if (game != null && amount != null) {
            calculatedAmount += amount.calculate(game, source, manaEffect);
        }
        return calculatedAmount;
    }

    private Integer getPlayableAmountOverride(Game game, Ability source, Effect manaEffect, boolean produceMana) {
        if (produceMana || game == null || !(manaEffect instanceof ComposedManaEffect composedManaEffect)) {
            return null;
        }
        DynamicValue capacityOverride = composedManaEffect.getCapacityOverride();
        return capacityOverride == null ? null : Math.max(0, capacityOverride.calculate(game, source, manaEffect));
    }

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
