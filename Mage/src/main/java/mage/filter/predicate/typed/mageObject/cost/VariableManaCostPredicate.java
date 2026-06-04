package mage.filter.predicate.typed.mageObject.cost;

import mage.MageObject;
import mage.abilities.costs.mana.VariableManaCost;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.game.Game;

public enum VariableManaCostPredicate implements IMageObjectPredicate {
    instance;

    @Override
    public boolean apply(ObjectSourcePlayer<MageObject> input, Game game) {
        return input.getObject().getManaCost().stream().anyMatch(VariableManaCost.class::isInstance);
    }
}
