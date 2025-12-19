package mage.cards.h;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldOrAttacksSourceTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.ExileTargetEffect;
import mage.abilities.effects.common.continuous.layers.L6_Abilities.GainAbilitiesOfEffect;
import mage.abilities.keyword.MenaceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.filter.common.FilterControlledPermanent;
import mage.game.permanent.token.FoodToken;
import mage.target.common.TargetCardInGraveyard;

import java.util.UUID;

/**
 * @author PurpleCrowbar
 */
public final class HazelsBrewmaster extends CardImpl {

    private static final FilterControlledPermanent filter = new FilterControlledPermanent(SubType.FOOD, "Foods you control");

    public HazelsBrewmaster(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{B}");
        this.subtype.add(SubType.SQUIRREL, SubType.WARLOCK);
        this.power = new MageInt(3);
        this.toughness = new MageInt(4);

        // Menace
        this.addAbility(new MenaceAbility(false));

        // Whenever Hazel's Brewmaster enters or attacks, exile up to one target card from a graveyard and create a Food token.
        Ability ability = new EntersBattlefieldOrAttacksSourceTriggeredAbility(new ExileTargetEffect().setToSourceExileZone(true));
        ability.addTarget(new TargetCardInGraveyard(0, 1));
        ability.addEffect(new CreateTokenEffect(new FoodToken()).concatBy("and"));
        this.addAbility(ability);

        // Foods you control have all activated abilities of all creature cards exiled with Hazel's Brewmaster.
        this.addAbility(new SimpleStaticAbility(new GainAbilitiesOfEffect(
                Duration.WhileOnBattlefield,
                ContinuousAffected.STATIC_OR_DYNAMIC,
                StaticFilters.FILTER_ACTIVATED_ABILITY,
                "Foods you control have all activated abilities of all creature cards exiled with {this}")
                .fromSourceExiled()
                .setCardWithAbilityFilter(StaticFilters.FILTER_CARD_CREATURE)
                .setAffectedZones(Zone.BATTLEFIELD)
                .setPermanentFilter(filter)
        ));
    }

    private HazelsBrewmaster(final HazelsBrewmaster card) {
        super(card);
    }

    @Override
    public HazelsBrewmaster copy() {
        return new HazelsBrewmaster(this);
    }
}
