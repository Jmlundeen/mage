package mage.abilities.mana.providers;

import mage.abilities.Ability;
import mage.abilities.condition.Condition;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.ChooseCreatureTypeEffect;
import mage.abilities.mana.conditional.SubtypeManaCondition;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.game.Game;

import java.util.List;

/**
 * Snapshots chosen creature type into mana spending conditions.
 */
public class ChosenCreatureTypeConditionProvider implements ManaConditionProvider {

    private final String typePostfix;
    private final FilterTyped filter;

    public ChosenCreatureTypeConditionProvider(FilterTyped filter) {
        this("_type", filter);
    }

    public ChosenCreatureTypeConditionProvider(String typePostfix, FilterTyped filter) {
        this.typePostfix = typePostfix;
        this.filter = filter;
    }

    @Override
    public List<Condition> getConditions(Game game, Ability source, Effect effect) {
        if (game == null || source == null) {
            return List.of();
        }
        SubType subType = ChooseCreatureTypeEffect.getChosenCreatureType(source.getSourceId(), game, typePostfix);
        return List.of(new SubtypeManaCondition(subType, filter));
    }
}


