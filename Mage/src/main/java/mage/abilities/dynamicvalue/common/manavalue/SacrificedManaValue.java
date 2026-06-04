package mage.abilities.dynamicvalue.common.manavalue;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.costs.Cost;
import mage.abilities.costs.common.SacrificeTargetCost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.game.Game;

/**
 * @author jmlundeen
 */
public enum SacrificedManaValue implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        for (Cost cost : sourceAbility.getCosts()) {
            if (cost instanceof SacrificeTargetCost sacrificeTargetCost) {
                return sacrificeTargetCost.getPermanents().stream()
                        .mapToInt(MageObject::getManaValue)
                        .sum();
            }
        }
        return 0;
    }

    @Override
    public DynamicValue copy() {
        return instance;
    }

    @Override
    public String getMessage() {
        return "Sacrificed permanents' mana value";
    }
}
