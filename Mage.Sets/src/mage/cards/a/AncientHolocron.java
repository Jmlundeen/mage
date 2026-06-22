
package mage.cards.a;

import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.AnyColorManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.mageObject.color.MultiColoredPredicate;

import java.util.UUID;

/**
 *
 * @author Styxo
 */
public final class AncientHolocron extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("multicolored spells")
            .add(MultiColoredPredicate.instance);

    public AncientHolocron(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ARTIFACT},"{3}");

        // {T}: Add one mana of any color to your manapool.
        this.addAbility(new AnyColorManaAbility());

        // {T}: Add two mana of any color to your manapool. Spend this mana only to cast multicolored spells.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyColor(2)
                .condition(new FilteredSpellManaCondition(filter))
                .ruleText("Add two mana of any color. Spend this mana only to cast multicolored spells.")
                .build()
        );

    }

    private AncientHolocron(final AncientHolocron card) {
        super(card);
    }

    @Override
    public AncientHolocron copy() {
        return new AncientHolocron(this);
    }
}
