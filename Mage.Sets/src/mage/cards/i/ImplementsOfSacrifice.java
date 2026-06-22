
package mage.cards.i;

import mage.abilities.costs.common.SacrificeSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.mana.AnyColorManaAbility;
import mage.abilities.mana.ComposedManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;

import java.util.UUID;

/**
 *
 * @author fireshoes
 */
public final class ImplementsOfSacrifice extends CardImpl {

    public ImplementsOfSacrifice(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ARTIFACT},"{2}");

        // {1}, {tap}, Sacrifice Implements of Sacrifice: Add two mana of any one color.
        ComposedManaAbility ability = new AnyColorManaAbility(2, new GenericManaCost(1));
        ability.addCost(new TapSourceCost());
        ability.addCost(new SacrificeSourceCost());
        this.addAbility(ability);
    }

    private ImplementsOfSacrifice(final ImplementsOfSacrifice card) {
        super(card);
    }

    @Override
    public ImplementsOfSacrifice copy() {
        return new ImplementsOfSacrifice(this);
    }
}
