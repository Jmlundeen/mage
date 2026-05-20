package mage.abilities.mana.conditional;

import mage.abilities.Ability;
import mage.abilities.condition.Condition;
import mage.abilities.costs.Cost;
import mage.game.Game;

import java.util.UUID;

/**
 * @author TheElk801
 */
public class ActivatedAbilityManaCondition extends ManaCondition implements Condition {

    @Override
    public boolean apply(Game game, Ability source) {
        return source != null
                && !source.isActivated()
                && source.isActivatedAbility();
    }

    @Override
    public boolean apply(Game game, Ability source, UUID originalId, Cost costsToPay) {
        return apply(game, source);
    }
}