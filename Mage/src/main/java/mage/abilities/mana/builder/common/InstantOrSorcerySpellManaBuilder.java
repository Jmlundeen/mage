package mage.abilities.mana.builder.common;

import mage.ConditionalMana;
import mage.Mana;
import mage.abilities.mana.builder.ConditionalManaBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.filter.StaticFilters;

/**
 * @author LevelX2
 */
public class InstantOrSorcerySpellManaBuilder extends ConditionalManaBuilder {

    @Override
    public ConditionalMana build(Object... options) {
        return new InstantOrSorceryCastConditionalMana(this.mana);
    }

    @Override
    public String getRule() {
        return "Spend this mana only to cast an instant or sorcery spell";
    }
}

class InstantOrSorceryCastConditionalMana extends ConditionalMana {

    public InstantOrSorceryCastConditionalMana(Mana mana) {
        super(mana);
        staticText = "Spend this mana only to cast an instant or sorcery spell";
        addCondition(new FilteredSpellManaCondition(StaticFilters.FILTER_SPELL_AN_INSTANT_OR_SORCERY, staticText));
    }
}
