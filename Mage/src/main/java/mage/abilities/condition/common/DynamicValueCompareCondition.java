package mage.abilities.condition.common;

import mage.abilities.Ability;
import mage.abilities.condition.Condition;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.constants.ComparisonType;
import mage.game.Game;

public class DynamicValueCompareCondition implements Condition {

    private final ComparisonType comparisonType;
    private final DynamicValue value;
    private final int compareTo;

    public DynamicValueCompareCondition(DynamicValue value, ComparisonType comparisonType, int compareTo) {
        this.value = value;
        this.comparisonType = comparisonType;
        this.compareTo = compareTo;
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return ComparisonType.compare(value.calculate(game, source, null), comparisonType, compareTo);
    }
}
