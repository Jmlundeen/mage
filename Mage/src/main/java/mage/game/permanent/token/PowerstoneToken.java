package mage.game.permanent.token;

import mage.Mana;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.abilities.mana.conditional.InvertedManaCondition;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.StaticTypedFilters;

/**
 * @author TheElk801
 */
public final class PowerstoneToken extends TokenImpl {

    public PowerstoneToken() {
        super("Powerstone Token", "Powerstone token");
        cardType.add(CardType.ARTIFACT);
        subtype.add(SubType.POWERSTONE);

        // {T}: Add {C}. This mana can't be spent to cast a nonartifact spell.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.ColorlessMana(1))
                .condition(new InvertedManaCondition(new FilteredSpellManaCondition(StaticTypedFilters.A_NON_ARTIFACT_SPELL)))
                .ruleText("Add {C}. This mana can't be spent to cast a nonartifact spell.")
                .build()
        );
    }

    private PowerstoneToken(final PowerstoneToken token) {
        super(token);
    }

    public PowerstoneToken copy() {
        return new PowerstoneToken(this);
    }

}
