
package mage.cards.p;

import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.keyword.HexproofAbility;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 * Custom version of Pillar of the Paruns.
 * Tweaked for the needs of the Pillar of the Paruns custom mode.
 *
 * Has Hexproof and "{T}: Add {1}"
 *
 * @author Susucr
 */
public final class PillarOfTheParunsCustom extends CardImpl {

    public PillarOfTheParunsCustom(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // Custom: Hexproof
        this.addAbility(HexproofAbility.getInstance());

        // Custom: {T}: Add {1}
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add one mana of any color. Spend this mana only to cast a multicolored spell.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyColor(1)
                .condition(new FilteredSpellManaCondition(StaticTypedFilters.A_MULTICOLORED_SPELL))
                .ruleText("Add one mana of any color. Spend this mana only to cast a multicolored spell.")
                .build()
        );
    }

    private PillarOfTheParunsCustom(final PillarOfTheParunsCustom card) {
        super(card);
    }

    @Override
    public PillarOfTheParunsCustom copy() {
        return new PillarOfTheParunsCustom(this);
    }
}
