package mage.abilities.dynamicvalue.common.cost;

import mage.abilities.Ability;
import mage.abilities.costs.VariableCost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.game.Game;
import mage.util.CardUtil;

/**
 * @author jmlundeen
 */
public enum VariableCostValue implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        return CardUtil.castStream(sourceAbility.getCosts().stream(), VariableCost.class)
                .mapToInt(VariableCost::getAmount)
                .sum();
    }

    @Override
    public DynamicValue copy() {
        return instance;
    }

    @Override
    public String getMessage() {
        return "Sum of variable costs";
    }
}
