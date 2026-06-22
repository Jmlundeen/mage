
package mage.cards.l;

import mage.abilities.Ability;
import mage.abilities.costs.common.SacrificeSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.keyword.SuspendAbility;
import mage.abilities.mana.AnyColorManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class LotusBloom extends CardImpl {

    public LotusBloom(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ARTIFACT},"");

        // Suspend 3-{0}
        this.addAbility(new SuspendAbility(3, new GenericManaCost(0), this));

        // {tap}, Sacrifice Lotus Bloom: Add three mana of any one color.
        Ability ability = new AnyColorManaAbility(3);
        ability.addCost(new SacrificeSourceCost());
        this.addAbility(ability);

    }

    private LotusBloom(final LotusBloom card) {
        super(card);
    }

    @Override
    public LotusBloom copy() {
        return new LotusBloom(this);
    }
}
