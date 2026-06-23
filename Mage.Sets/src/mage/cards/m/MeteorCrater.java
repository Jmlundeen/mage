package mage.cards.m;

import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.providers.common.manaType.ColorsAmongPermanentsTypeProvider;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;

import java.util.UUID;

/**
 * @author anonymous
 */
public final class MeteorCrater extends CardImpl {

    public MeteorCrater(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // {tap}: Choose a color of a permanent you control. Add one mana of that color.
        this.addAbility(new ComposedManaAbilityBuilder()
                .addChoice(ColorsAmongPermanentsTypeProvider.YOU_CONTROL, 1)
                .cost(new TapSourceCost())
                .ruleText("Choose a color of a permanent you control. Add one mana of that color")
                .build()
        );
    }

    private MeteorCrater(final MeteorCrater card) {
        super(card);
    }

    @Override
    public MeteorCrater copy() {
        return new MeteorCrater(this);
    }
}
