package mage.cards.d;

import mage.MageInt;
import mage.abilities.costs.common.DiscardHandCost;
import mage.abilities.costs.common.SacrificeSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class DiamondLion extends CardImpl {

    public DiamondLion(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{2}");

        this.subtype.add(SubType.CAT);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // {T}, Discard your hand, Sacrifice Diamond Lion: Add three mana of any one color. Activate only as an instant.
        this.addAbility(new ComposedManaAbilityBuilder()
                .addAnyColor(3)
                .cost(new TapSourceCost())
                .cost(new DiscardHandCost())
                .cost(new SacrificeSourceCost())
                .ruleText("Add three mana of any one color. Activate only as an instant.")
                .build()
                .setOnlyAsInstant(true)
        );
    }

    private DiamondLion(final DiamondLion card) {
        super(card);
    }

    @Override
    public DiamondLion copy() {
        return new DiamondLion(this);
    }
}
