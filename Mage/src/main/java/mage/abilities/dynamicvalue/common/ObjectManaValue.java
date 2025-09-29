package mage.abilities.dynamicvalue.common;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.game.Game;

public enum ObjectManaValue implements DynamicValue {

    instance;

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect, MageObject mageObject) {
        if (mageObject != null) {
            return mageObject.getManaValue();
        }
        return 0;
    }

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        return 0;
    }

    @Override
    public DynamicValue copy() {
        return ObjectManaValue.instance;
    }

    @Override
    public String getMessage() {
        return "its mana value";
    }
}
