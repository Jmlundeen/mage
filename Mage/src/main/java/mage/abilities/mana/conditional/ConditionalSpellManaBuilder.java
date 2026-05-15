package mage.abilities.mana.conditional;

import mage.ConditionalMana;
import mage.Mana;
import mage.abilities.mana.builder.ConditionalManaBuilder;
import mage.filter.FilterSpell;

import java.util.Objects;

/**
 * @author LevelX2
 */
public class ConditionalSpellManaBuilder extends ConditionalManaBuilder {

    private final FilterSpell filter;

    public ConditionalSpellManaBuilder(FilterSpell filter) {
        this.filter = filter;
    }

    @Override
    public ConditionalMana build(Object... options) {
        this.mana.setFlag(true); // indicates that the mana is from second ability
        return new SpellCastConditionalMana(this.mana, filter);
    }

    @Override
    public String getRule() {
        return "Spend this mana only to cast " + filter.getMessage() + '.';
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.filter);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }

        return Objects.equals(this.filter, ((ConditionalSpellManaBuilder) obj).filter);
    }
}

class SpellCastConditionalMana extends ConditionalMana {

    SpellCastConditionalMana(Mana mana, FilterSpell filter) {
        super(mana);
        staticText = "Spend this mana only to cast " + filter.getMessage() + '.';
        addCondition(new FilteredSpellManaCondition(filter, staticText));
    }

}
