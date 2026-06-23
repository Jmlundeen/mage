package mage.filter.predicate.typed.ability.source;

import mage.MageObject;
import mage.abilities.Ability;
import mage.filter.FilterTyped;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.typed.ability.IAbilityPredicate;
import mage.game.Game;

public class AbilitySourcePredicate implements IAbilityPredicate {

    private final FilterTyped sourceFilter;

    public AbilitySourcePredicate(FilterTyped sourceFilter) {
        this.sourceFilter = sourceFilter;
    }

    @Override
    public boolean apply(ObjectSourcePlayer<Ability> input, Game game) {
        MageObject object = input.getObject().getSourceObject(game);
        if (object == null) {
            return false;
        }
        return sourceFilter.match(object, input.getPlayerId(), input.getSource(), game);
    }
}
