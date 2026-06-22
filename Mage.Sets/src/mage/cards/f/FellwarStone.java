
package mage.cards.f;

import mage.abilities.mana.AnyColorAmongManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 *
 * @author Plopman
 */
public final class FellwarStone extends CardImpl {

    public FellwarStone(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ARTIFACT},"{2}");

        // {T}: Add one mana of any color that a land an opponent controls could produce.
        this.addAbility(AnyColorAmongManaAbility.builder(StaticTypedFilters.LAND_AN_OPPONENT_CONTROLS)
                .onlyProducibleManaTypes(true)
                .onlyColors(true)
                .ruleText("Add one mana of any color that a land an opponent controls could produce")
                .build()
        );
    }

    private FellwarStone(final FellwarStone card) {
        super(card);
    }

    @Override
    public FellwarStone copy() {
        return new FellwarStone(this);
    }
}
