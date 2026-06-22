
package mage.cards.s;

import mage.abilities.ActivatedAbilityImpl;
import mage.abilities.condition.common.PermanentsOnTheBattlefieldCondition;
import mage.abilities.costs.common.PayLifeCost;
import mage.abilities.mana.AnyColorManaAbility;
import mage.abilities.mana.ColorlessManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.common.FilterControlledArtifactPermanent;

import java.util.UUID;

/**
 *
 * @author Styxo
 */
public final class SpireOfIndustry extends CardImpl {

    public SpireOfIndustry(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}, Pay 1 life: Add one mana of any color. Activate this ability only if you control an artifact.
        ActivatedAbilityImpl ability = new AnyColorManaAbility();
        ability.addCost(new PayLifeCost(1));
        ability.setCondition(new PermanentsOnTheBattlefieldCondition(new FilterControlledArtifactPermanent("you control an artifact")));
        ability.appendToRule(" Activate this ability only if you control an artifact.");
        this.addAbility(ability);
    }

    private SpireOfIndustry(final SpireOfIndustry card) {
        super(card);
    }

    @Override
    public SpireOfIndustry copy() {
        return new SpireOfIndustry(this);
    }
}
