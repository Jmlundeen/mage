package mage.filter.predicate.mageobject;

import mage.MageObject;
import mage.abilities.effects.common.ChooseCreatureTypeEffect;
import mage.constants.SubType;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.ObjectSourcePlayerPredicate;
import mage.game.Game;

public enum ChosenCreatureTypePredicate implements ObjectSourcePlayerPredicate<MageObject> {
    TRUE(true),
    FALSE(false);

    private final boolean value;

    ChosenCreatureTypePredicate(boolean value) {
        this.value = value;
    }


    @Override
    public boolean apply(ObjectSourcePlayer<MageObject> input, Game game) {
        SubType choice = ChooseCreatureTypeEffect.getChosenCreatureType(input.getSourceId(), game);
        if (choice == null) {
            return !value;
        }
        return input.getObject().hasSubtype(choice, game) == value;
    }
}
