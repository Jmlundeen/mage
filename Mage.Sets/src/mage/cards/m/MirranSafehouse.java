package mage.cards.m;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.continuous.layers.L6_Abilities.GainAbilitiesOfEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class MirranSafehouse extends CardImpl {

    public MirranSafehouse(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{3}");

        // As long as Mirran Safehouse is on the battlefield, it has all activated abilities of all land cards in all graveyards.
        this.addAbility(new SimpleStaticAbility(new GainAbilitiesOfEffect(StaticFilters.FILTER_ACTIVATED_ABILITY,
                "As long as {this} is on the battlefield, it has all activated abilities of all land cards in all graveyards")
                .fromCardsInZones(StaticFilters.FILTER_CARD_LAND, Zone.GRAVEYARD)
                .setCardWithAbilityController(TargetController.EACH_PLAYER)
        ));
    }

    private MirranSafehouse(final MirranSafehouse card) {
        super(card);
    }

    @Override
    public MirranSafehouse copy() {
        return new MirranSafehouse(this);
    }
}
