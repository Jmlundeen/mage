
package mage.cards.s;

import mage.MageInt;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.StaticTypedFilters;

import java.util.UUID;


/**
 * @author noxx
 */
public final class SomberwaldSage extends CardImpl {

    public SomberwaldSage(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{2}{G}");
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.DRUID);

        this.power = new MageInt(0);
        this.toughness = new MageInt(1);

        // {tap}: Add three mana of any one color. Spend this mana only to cast creature spells.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyColor(3)
                .condition(new FilteredSpellManaCondition(StaticTypedFilters.A_CREATURE_SPELL))
                .ruleText("Add three mana of any one color. Spend this mana only to cast creature spells.")
                .build()
        );
    }

    private SomberwaldSage(final SomberwaldSage card) {
        super(card);
    }

    @Override
    public SomberwaldSage copy() {
        return new SomberwaldSage(this);
    }
}
