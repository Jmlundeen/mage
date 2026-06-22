
package mage.cards.r;

import mage.abilities.mana.AnyColorAmongManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 *
 * @author jeffwadsworth
 */
public final class ReflectingPool extends CardImpl {

    public ReflectingPool(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // {T}: Add one mana of any type that a land you control could produce.
        this.addAbility(AnyColorAmongManaAbility.builder(StaticTypedFilters.LAND_YOU_CONTROL)
                .onlyProducibleManaTypes(true)
                .ruleText("Add one mana of any type that a land you control could produce.")
                .build()
        );
    }

    private ReflectingPool(final ReflectingPool card) {
        super(card);
    }

    @Override
    public ReflectingPool copy() {
        return new ReflectingPool(this);
    }
}
