package mage.abilities.dynamicvalue.common.manavalue;

import mage.abilities.Ability;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.game.Game;

/**
 * @author jmlundeen
 */
public enum CounteredManaValue implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        String key = sourceAbility.getSourceId() + "_counteredManaValue";
        return sourceAbility.getEffects().getFirst().getValue(key) instanceof Integer
                ? (Integer) sourceAbility.getEffects().getFirst().getValue(key)
                : 0;
    }

    @Override
    public DynamicValue copy() {
        return instance;
    }

    @Override
    public String getMessage() {
        return "total mana value of countered spells";
    }
}
