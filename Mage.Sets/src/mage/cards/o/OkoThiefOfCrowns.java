package mage.cards.o;

import mage.abilities.Ability;
import mage.abilities.LoyaltyAbility;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.continuous.BecomesCreatureTargetEffect;
import mage.abilities.effects.common.continuous.ExchangeControlTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.filter.StaticFilters;
import mage.filter.common.FilterOpponentsCreaturePermanent;
import mage.filter.predicate.mageobject.PowerPredicate;
import mage.game.permanent.token.FoodToken;
import mage.game.permanent.token.custom.CreatureToken;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class OkoThiefOfCrowns extends CardImpl {

    private static final FilterPermanent filter
            = new FilterOpponentsCreaturePermanent("creature an opponent controls with power 3 or less");

    static {
        filter.add(new PowerPredicate(ComparisonType.FEWER_THAN, 4));
    }

    public OkoThiefOfCrowns(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.PLANESWALKER}, "{1}{G}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.OKO);
        this.setStartingLoyalty(4);

        // +2: Create a Food token.
        this.addAbility(new LoyaltyAbility(new CreateTokenEffect(new FoodToken()), 2));

        // +1: Target artifact or creature loses all abilities and becomes a green Elk creature with base power and toughness 3/3.
        Ability ability = new LoyaltyAbility(new BecomesCreatureTargetEffect(
                new CreatureToken(3, 3, "green Elk creature with base power and toughness 3/3")
                        .withColor("G")
                        .withSubType(SubType.ELK), true, false, Duration.Custom
        ), 1);
        ability.addTarget(new TargetPermanent(StaticFilters.FILTER_PERMANENT_ARTIFACT_OR_CREATURE));
        this.addAbility(ability);

        // −5: Exchange control of target artifact or creature you control and target creature an opponent controls with power 3 or less.
        ability = new LoyaltyAbility(new ExchangeControlTargetEffect(
                Duration.EndOfGame, "exchange control of target artifact or creature you control " +
                "and target creature an opponent controls with power 3 or less", false, true
        ), -5);
        ability.addTarget(new TargetPermanent(StaticFilters.FILTER_CONTROLLED_PERMANENT_ARTIFACT_OR_CREATURE));
        ability.addTarget(new TargetPermanent(filter));
        this.addAbility(ability);
    }

    private OkoThiefOfCrowns(final OkoThiefOfCrowns card) {
        super(card);
    }

    @Override
    public OkoThiefOfCrowns copy() {
        return new OkoThiefOfCrowns(this);
    }
}
