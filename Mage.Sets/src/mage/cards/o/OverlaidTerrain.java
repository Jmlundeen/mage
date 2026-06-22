package mage.cards.o;

import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.SacrificeAllControllerEffect;
import mage.abilities.effects.common.continuous.generic.GenericContinuousEffect;
import mage.abilities.mana.AnyColorManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 * @author fireshoes
 */
public final class OverlaidTerrain extends CardImpl {

    public OverlaidTerrain(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{G}{G}");

        // As Overlaid Terrain enters the battlefield, sacrifice all lands you control.
        this.addAbility(new AsEntersBattlefieldAbility(new SacrificeAllControllerEffect(StaticFilters.FILTER_LANDS)));

        // Lands you control have "{T}: Add two mana of any one color."
        this.addAbility(new SimpleStaticAbility(new GenericContinuousEffect(Outcome.AddAbility, StaticTypedFilters.LAND_YOU_CONTROL, Zone.BATTLEFIELD)
                .withGainedAbilities(new AnyColorManaAbility(2))
                .setText("Lands you control have \"{T}: Add two mana of any one color.\"")
        ));
    }

    private OverlaidTerrain(final OverlaidTerrain card) {
        super(card);
    }

    @Override
    public OverlaidTerrain copy() {
        return new OverlaidTerrain(this);
    }
}