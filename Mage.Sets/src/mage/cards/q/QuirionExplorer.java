
package mage.cards.q;

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
public final class QuirionExplorer extends CardImpl {

    public QuirionExplorer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{1}{G}");
        this.subtype.add(SubType.ELF);
        this.subtype.add(SubType.DRUID);
        this.subtype.add(SubType.SCOUT);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // {T}: Add one mana of any color that a land an opponent controls could produce.
        this.addAbility(AnyColorAmongManaAbility.builder(StaticTypedFilters.LAND_AN_OPPONENT_CONTROLS)
                .onlyProducibleManaTypes(true)
                .onlyColors(true)
                .ruleText("Add one mana of any color that a land an opponent controls could produce.")
                .build()
        );
    }

    private QuirionExplorer(final QuirionExplorer card) {
        super(card);
    }

    @Override
    public QuirionExplorer copy() {
        return new QuirionExplorer(this);
    }
}
