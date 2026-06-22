
package mage.cards.g;

import mage.abilities.mana.AnyColorManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;

import java.util.UUID;

/**
 *
 * @author Loki
 */
public final class GildedLotus extends CardImpl {

    public GildedLotus(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ARTIFACT},"{5}");

        // {tap}: Add three mana of any one color.
        this.addAbility(new AnyColorManaAbility(3));
    }

    private GildedLotus(final GildedLotus card) {
        super(card);
    }

    @Override
    public GildedLotus copy() {
        return new GildedLotus(this);
    }
}
