package mage.abilities.condition.common;

import mage.abilities.Ability;
import mage.abilities.condition.Condition;
import mage.abilities.keyword.EscapeAbility;
import mage.game.Game;

public enum EscapedCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        int zcc = game.getState().getZoneChangeCounter(source.getSourceId());
        boolean castWithEscape = EscapeAbility.wasCastedWithEscape(game, source.getSourceId(), zcc);
        if (!castWithEscape) {
            // Replacement effects may check for escape so we need to look ahead as well
            return EscapeAbility.wasCastedWithEscape(game, source.getSourceId(), zcc + 1);
        }
        return true;
    }
}
