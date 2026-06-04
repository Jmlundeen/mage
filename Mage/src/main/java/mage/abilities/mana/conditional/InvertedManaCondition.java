package mage.abilities.mana.conditional;

import mage.abilities.Ability;
import mage.abilities.costs.Cost;
import mage.game.Game;

import java.util.UUID;

public class InvertedManaCondition extends ManaCondition {

    private final ManaCondition condition;

    public InvertedManaCondition(ManaCondition condition) {
        this.condition = condition;
    }

    @Override
    public boolean apply(Game game, Ability source, UUID originalId, Cost costToPay) {
        return !condition.apply(game, source, originalId, costToPay);
    }
}
