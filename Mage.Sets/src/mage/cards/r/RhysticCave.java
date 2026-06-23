package mage.cards.r;

import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;

import java.util.UUID;

/**
 * @author jerekwilson
 */
public final class RhysticCave extends CardImpl {

    public RhysticCave(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // {T}: Choose a color. Add one mana of that color unless any player pays {1}. Activate this ability only any time you could cast an instant.
        this.addAbility(new ComposedManaAbilityBuilder()
                .addAnyColor(1)
                .cost(new TapSourceCost())
                .addAnyPlayerPaysCost(new GenericManaCost(1))
                .ruleText("Choose a color. Add one mana of that color unless any player pays {1}. Activate only as an instant.")
                .build()
                .setOnlyAsInstant(true)
        );
    }

    private RhysticCave(final RhysticCave card) {
        super(card);
    }

    @Override
    public RhysticCave copy() {
        return new RhysticCave(this);
    }
}
