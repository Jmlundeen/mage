package mage.cards.t;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.continuous.layers.L6_Abilities.GainAbilitiesOfEffect;
import mage.abilities.keyword.DeathtouchAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class TrazynTheInfinite extends CardImpl {

    public TrazynTheInfinite(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{4}{B}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.NECRON);
        this.power = new MageInt(4);
        this.toughness = new MageInt(6);

        // Deathtouch
        this.addAbility(DeathtouchAbility.getInstance());

        // Prismatic Gallery -- As long as Trazyn the Infinite is on the battlefield, it has all activated abilities of all artifact cards in your graveyard.
        this.addAbility(new SimpleStaticAbility(new GainAbilitiesOfEffect(StaticFilters.FILTER_ACTIVATED_ABILITY,
                "As long as Trazyn the Infinite is on the battlefield, it has all activated abilities of all artifact cards in your graveyard")
                .fromCardsInZones(StaticFilters.FILTER_CARD_ARTIFACT, Zone.GRAVEYARD)
        ).withFlavorWord("Prismatic Gallery"));
    }

    private TrazynTheInfinite(final TrazynTheInfinite card) {
        super(card);
    }

    @Override
    public TrazynTheInfinite copy() {
        return new TrazynTheInfinite(this);
    }
}
