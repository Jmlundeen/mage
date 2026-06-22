
package mage.cards.s;

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
 * @author Plopman
 */
public final class SylvokExplorer extends CardImpl {

    public SylvokExplorer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{1}{G}");
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.DRUID);

        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // {T}: Add one mana of any color that a land an opponent controls could produce.
        this.addAbility(AnyColorAmongManaAbility.builder(StaticTypedFilters.LAND_AN_OPPONENT_CONTROLS)
                .onlyProducibleManaTypes(true)
                .onlyColors(true)
                .ruleText("Add one mana of any color that a land an opponent controls could produce")
                .build()
        );
    }

    private SylvokExplorer(final SylvokExplorer card) {
        super(card);
    }

    @Override
    public SylvokExplorer copy() {
        return new SylvokExplorer(this);
    }
}
