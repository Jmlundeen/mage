
package mage.cards.h;

import mage.MageInt;
import mage.abilities.mana.AnyColorAmongManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 *
 * @author LoneFox
 */
public final class HarvesterDruid extends CardImpl {

    public HarvesterDruid(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{1}{G}");
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.DRUID);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // {tap}: Add one mana of any color that a land you control could produce.
        this.addAbility(new AnyColorAmongManaAbility.Builder(StaticTypedFilters.LAND_YOU_CONTROL)
                .onlyProducibleManaTypes(true)
                .ruleText("Add one mana of any type that a land you control could produce.")
                .build()
        );
    }

    private HarvesterDruid(final HarvesterDruid card) {
        super(card);
    }

    @Override
    public HarvesterDruid copy() {
        return new HarvesterDruid(this);
    }
}
