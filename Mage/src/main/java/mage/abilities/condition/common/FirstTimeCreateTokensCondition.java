package mage.abilities.condition.common;

import mage.abilities.Ability;
import mage.abilities.condition.Condition;
import mage.game.Game;
import mage.watchers.common.CreatedTokenWatcher;

/**
 * Condition for "the first time you would create one or more tokens each turn"
 * or "the first time you would create one or more tokens during each of your turns".
 * Use with CreatedTokenWatcher.
 * @author Jmlundeen
 */
public enum FirstTimeCreateTokensCondition implements Condition {
    EACH_TURN,
    YOUR_TURN;

    @Override
    public boolean apply(Game game, Ability source) {
        boolean result = !CreatedTokenWatcher.checkPlayer(source.getControllerId(), game);
        if (this == YOUR_TURN) {
            result = result && game.isActivePlayer(source.getControllerId());
        }
        return result;
    }

    @Override
    public String toString() {
        switch (this) {
            case EACH_TURN:
                return "the first time you would create one or more tokens each turn";
            case YOUR_TURN:
                return "the first time you would create one or more tokens during each of your turns";
        }
        return "";
    }
}
