package mage.filter.predicate.typed;

import mage.constants.ComparisonType;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.TypedPredicate;
import mage.game.Game;

public interface TypedIntComparePredicate<T> extends TypedPredicate<T> {

    ComparisonType getComparisonType();

    int getValue(ObjectSourcePlayer<T> input, Game game);

    int getInputValue(ObjectSourcePlayer<T> input, Game game);

    @Override
    default boolean apply(ObjectSourcePlayer<T> input, Game game) {
        return ComparisonType.compare(
                getInputValue(input, game),
                getComparisonType(),
                getValue(input, game)
        );
    }
}