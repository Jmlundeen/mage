
package mage.cards.l;

import mage.abilities.costs.common.DiscardHandCost;
import mage.abilities.costs.common.SacrificeSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;

import java.util.UUID;

/**
 *
 * @author North
 */
public final class LionsEyeDiamond extends CardImpl {

    public LionsEyeDiamond(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{0}");

        // Sacrifice Lion's Eye Diamond, Discard your hand: Add three mana of any one color. Activate this ability only any time you could cast an instant.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new DiscardHandCost())
                .cost(new SacrificeSourceCost())
                .addChoiceAnyOneColor(3)
                .ruleText("Add three mana of any one color. Activate only as an instant")
                .build()
                .setOnlyAsInstant(true)
        );
    }

    private LionsEyeDiamond(final LionsEyeDiamond card) {
        super(card);
    }

    @Override
    public LionsEyeDiamond copy() {
        return new LionsEyeDiamond(this);
    }
}
