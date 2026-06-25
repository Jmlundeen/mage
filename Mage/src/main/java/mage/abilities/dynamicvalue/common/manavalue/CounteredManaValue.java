package mage.abilities.dynamicvalue.common.manavalue;

import mage.abilities.Ability;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.game.Game;

public enum CounteredManaValue implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        return sourceAbility.getEffects().getFirst().getValue("counteredManaValue") instanceof Integer
                ? (Integer) sourceAbility.getEffects().getFirst().getValue("counteredManaValue")
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
