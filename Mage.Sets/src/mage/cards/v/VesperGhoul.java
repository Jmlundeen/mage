
package mage.cards.v;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.costs.common.PayLifeCost;
import mage.abilities.mana.AnyColorManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;

import java.util.UUID;

/**
 *
 * @author LoneFox
 */
public final class VesperGhoul extends CardImpl {

    public VesperGhoul(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{2}{B}");
        this.subtype.add(SubType.ZOMBIE);
        this.subtype.add(SubType.DRUID);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // {T}, Pay 1 life: Add one mana of any color.
        Ability ability = new AnyColorManaAbility();
        ability.addCost(new PayLifeCost(1));
        this.addAbility(ability);
    }

    private VesperGhoul(final VesperGhoul card) {
        super(card);
    }

    @Override
    public VesperGhoul copy() {
        return new VesperGhoul(this);
    }
}
