package mage.abilities.dynamicvalue.common;

import mage.abilities.Ability;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.game.Game;
import mage.util.CardUtil;

public enum SourceXCostValue implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        return CardUtil.getSourceCostsTagX(game, sourceAbility, 0);
    }

    @Override
    public SourceXCostValue copy() {
        return instance;
    }

    @Override
    public String toString() {
        return "X";
    }

    @Override
    public String getMessage() {
        return "X";
    }
}
