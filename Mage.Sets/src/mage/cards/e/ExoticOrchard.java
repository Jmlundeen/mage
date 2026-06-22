
package mage.cards.e;

import mage.abilities.mana.AnyColorAmongManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class ExoticOrchard extends CardImpl {

    public ExoticOrchard(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.LAND},"");

        // {T}: Add one mana of any color that a land an opponent controls could produce.
        this.addAbility(AnyColorAmongManaAbility.builder(StaticTypedFilters.LAND_AN_OPPONENT_CONTROLS)
                .onlyProducibleManaTypes(true)
                .ruleText("Add one mana of any color that a land an opponent controls could produce")
                .build()
        );
    }

    private ExoticOrchard(final ExoticOrchard card) {
        super(card);
    }

    @Override
    public ExoticOrchard copy() {
        return new ExoticOrchard(this);
    }
}
