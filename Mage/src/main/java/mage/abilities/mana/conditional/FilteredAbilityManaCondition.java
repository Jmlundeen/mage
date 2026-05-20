package mage.abilities.mana.conditional;

import mage.abilities.Ability;
import mage.abilities.costs.Cost;
import mage.filter.FilterAbility;
import mage.game.Game;

import java.util.UUID;

public class FilteredAbilityManaCondition extends ManaCondition {

    private final FilterAbility filter;
    private final String manaText;

    public FilteredAbilityManaCondition(FilterAbility filter, String manaText) {
        this.filter = filter.copy();
        this.manaText = manaText;
    }

    @Override
    public boolean apply(Game game, Ability source, UUID originalId, Cost costToPay) {
        return filter.match(source, game);
    }

    @Override
    public String getManaText() {
        return manaText;
    }
}
