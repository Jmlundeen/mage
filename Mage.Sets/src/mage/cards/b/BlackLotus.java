
package mage.cards.b;

import mage.abilities.costs.common.SacrificeSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class BlackLotus extends CardImpl {

    public BlackLotus(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ARTIFACT},"{0}");

        // {tap}, Sacrifice Black Lotus: Add three mana of any one color.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                        .cost(new TapSourceCost())
                        .cost(new SacrificeSourceCost())
                        .addAnyColor(3)
                        .ruleText("Add three mana of any one color")
                .build()
        );
    }

    private BlackLotus(final BlackLotus card) {
        super(card);
    }

    @Override
    public BlackLotus copy() {
        return new BlackLotus(this);
    }
}
