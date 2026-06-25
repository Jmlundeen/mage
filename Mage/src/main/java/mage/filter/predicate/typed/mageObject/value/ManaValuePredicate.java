package mage.filter.predicate.typed.mageObject.value;

import mage.MageObject;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.constants.ComparisonType;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.typed.TypedIntComparePredicate;
import mage.game.Game;

public class ManaValuePredicate implements TypedIntComparePredicate<MageObject> {

    private final ComparisonType comparisonType;
    private final DynamicValue value;

    public ManaValuePredicate(ComparisonType comparisonType, int value) {
        this.comparisonType = comparisonType;
        this.value = StaticValue.get(value);
    }

    public ManaValuePredicate(ComparisonType comparisonType, DynamicValue value) {
        this.comparisonType = comparisonType;
        this.value = value;
    }

    @Override
    public int getInputValue(ObjectSourcePlayer<MageObject> input, Game game) {
        return input.getObject().getManaValue();
    }

    @Override
    public ComparisonType getComparisonType() {
        return comparisonType;
    }

    @Override
    public int getValue(ObjectSourcePlayer<MageObject> input, Game game) {
        return value.calculate(game, input.getSource(), null, input.getObject());
    }

    @Override
    public String toString() {
        return "ManaValue" + comparisonType + value.getMessage();
    }

    @Override
    public Class<MageObject> getObjectClass() {
        return MageObject.class;
    }
}
