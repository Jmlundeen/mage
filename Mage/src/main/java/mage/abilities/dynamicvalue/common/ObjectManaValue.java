package mage.abilities.dynamicvalue.common;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.game.Game;
import mage.game.stack.Spell;

public enum ObjectManaValue implements DynamicValue {

    SPELL,
    PERMANENT;

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect, MageObject mageObject) {
        if (mageObject != null) {
            if (this == SPELL && !(mageObject instanceof Spell)) {
                mageObject = game.getSpellOrLKIStack(mageObject.getId());
                if (mageObject == null) {
                    return 0;
                }
            }
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
        return this;
    }

    @Override
    public String getMessage() {
        return "its mana value";
    }
}
