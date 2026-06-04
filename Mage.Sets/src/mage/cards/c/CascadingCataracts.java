
package mage.cards.c;

import mage.abilities.Ability;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.keyword.IndestructibleAbility;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;

import java.util.UUID;

/**
 *
 * @author spjspj
 */
public final class CascadingCataracts extends CardImpl {

    public CascadingCataracts(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // Indestructibles
        this.addAbility(IndestructibleAbility.getInstance());

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {5}, {T}: Add five mana in any combination of colors.
        Ability manaAbility = new ComposedManaAbilityBuilder()
                .addAnyCombination(5)
                .cost(new GenericManaCost(5))
                .ruleText("Add five mana in any combination of colors.")
                .build();
        manaAbility.addCost(new TapSourceCost());
        this.addAbility(manaAbility);
    }

    private CascadingCataracts(final CascadingCataracts card) {
        super(card);
    }

    @Override
    public CascadingCataracts copy() {
        return new CascadingCataracts(this);
    }
}
