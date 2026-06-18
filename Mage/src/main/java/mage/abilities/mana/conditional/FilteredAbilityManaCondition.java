package mage.abilities.mana.conditional;

import mage.abilities.Ability;
import mage.abilities.costs.Cost;
import mage.filter.FilterTyped;
import mage.game.Game;

import java.util.UUID;

public class FilteredAbilityManaCondition extends ManaCondition {

    private final FilterTyped filter;
    private final String manaText;

    public FilteredAbilityManaCondition(FilterTyped filter) {
        this.filter = filter.copy();
        this.manaText = filter.getMessage();
    }

    @Override
    public boolean apply(Game game, Ability source, UUID originalId, Cost costToPay) {
        return !source.isActivated() && filter.match(source, source.getControllerId(), source, game);
    }

    @Override
    public String getManaText() {
        return manaText;
    }
}
