package mage.cards.s;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.ExileTargetEffect;
import mage.abilities.effects.common.continuous.layers.L6_Abilities.GainAbilitiesOfEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.filter.common.FilterLandCard;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.UUID;

/**
 *
 * @author Jmlundeen
 */
public final class StewardOfTheHarvest extends CardImpl {

    private static final FilterLandCard filter = new FilterLandCard("land cards from your graveyard");

    public StewardOfTheHarvest(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}");
        
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.DRUID);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // When this creature enters, exile up to three target land cards from your graveyard.
        Ability ability = new EntersBattlefieldTriggeredAbility(new ExileTargetEffect().setToSourceExileZone(true));
        ability.addTarget(new TargetCardInYourGraveyard(0, 3, filter));
        this.addAbility(ability);

        // Creatures you control have all activated abilities of all land cards exiled with this creature.
        this.addAbility(new SimpleStaticAbility(new GainAbilitiesOfEffect(Duration.WhileOnBattlefield, ContinuousAffected.STATIC_OR_DYNAMIC,
                StaticFilters.FILTER_ACTIVATED_ABILITY,
                "Creatures you control have all activated abilities of all land cards exiled with this creature")
                .fromSourceExiled()
                .setCardWithAbilityFilter(StaticFilters.FILTER_CARD_LAND)
                .setAffectedZones(Zone.BATTLEFIELD)
                .setPermanentFilter(StaticFilters.FILTER_PERMANENT_CREATURE)
        ));
    }

    private StewardOfTheHarvest(final StewardOfTheHarvest card) {
        super(card);
    }

    @Override
    public StewardOfTheHarvest copy() {
        return new StewardOfTheHarvest(this);
    }
}
