package mage.cards.s;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.ExileTargetEffect;
import mage.abilities.effects.common.continuous.layers.L6_Abilities.GainAbilitiesOfEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.filter.StaticTypedFilters;
import mage.filter.common.FilterLandCard;
import mage.filter.predicate.typed.ability.type.ActivatedAbilityPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.UUID;

/**
 *
 * @author Jmlundeen
 */
public final class StewardOfTheHarvest extends CardImpl {

    private static final FilterLandCard filter = new FilterLandCard("land cards from your graveyard");
    private static final FilterTyped abilityFilter = new FilterTyped("activated abilities of a land")
            .add(IMageObjectPredicate.getOSPPredicate(CardType.LAND.getPredicate()))
            .add(ActivatedAbilityPredicate.instance);

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
        this.addAbility(new SimpleStaticAbility(new GainAbilitiesOfEffect(Duration.WhileOnBattlefield, StaticTypedFilters.CREATURE_YOU_CONTROL)
                .setAbilityFilter(abilityFilter)
                .fromSourceExiled()
                .setText("Creatures you control have all activated abilities of all land cards exiled with {this}")
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
