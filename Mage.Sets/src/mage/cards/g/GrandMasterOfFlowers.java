package mage.cards.g;

import mage.abilities.Ability;
import mage.abilities.LoyaltyAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.SourceHasCounterCondition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.common.combat.CantAttackTargetEffect;
import mage.abilities.effects.common.combat.CantBlockTargetEffect;
import mage.abilities.effects.common.continuous.BecomesCreatureSourceEffect;
import mage.abilities.effects.common.search.SearchLibraryGraveyardPutInHandEffect;
import mage.abilities.keyword.*;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.FilterCard;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.AbilityPredicate;
import mage.filter.predicate.mageobject.NamePredicate;
import mage.game.permanent.token.custom.CreatureToken;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class GrandMasterOfFlowers extends CardImpl {

    private static final FilterPermanent filter
            = new FilterCreaturePermanent("creature without first strike, double strike, or vigilance");
    private static final FilterCard filter2
            = new FilterCard("Monk of the Open Hand");

    static {
        filter.add(Predicates.not(new AbilityPredicate(FirstStrikeAbility.class)));
        filter.add(Predicates.not(new AbilityPredicate(DoubleStrikeAbility.class)));
        filter.add(Predicates.not(new AbilityPredicate(VigilanceAbility.class)));
        filter2.add(new NamePredicate("Monk of the Open Hand"));
    }

    public GrandMasterOfFlowers(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.PLANESWALKER}, "{2}{W}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.BAHAMUT);
        this.setStartingLoyalty(3);

        // As long as Grand Master of Flowers has seven or more loyalty counters on him, he's a 7/7 Dragon God creature with flying and indestructible.
        ContinuousEffect effect = new BecomesCreatureSourceEffect(
                new CreatureToken(7, 7, "7/7 Dragon God creature with flying and indestructible")
                        .withSubType(SubType.DRAGON)
                        .withSubType(SubType.GOD)
                        .withAbility(FlyingAbility.getInstance())
                        .withAbility(IndestructibleAbility.getInstance()),
                CardType.PLANESWALKER,Duration.WhileOnBattlefield
        );
        this.addAbility(new SimpleStaticAbility(new ConditionalContinuousEffect(
                effect, new SourceHasCounterCondition(CounterType.LOYALTY, ComparisonType.OR_GREATER, 7),
                "as long as {this} has seven or more loyalty counters on him, he's a 7/7 Dragon God creature with flying and indestructible"
        )));

        // +1: Target creature without first strike, double strike, or vigilance can't attack or block until your next turn.
        Ability ability = new LoyaltyAbility(new CantAttackTargetEffect(Duration.UntilYourNextTurn)
                .setText("target creature without first strike, double strike"), 1);
        ability.addEffect(new CantBlockTargetEffect(Duration.UntilYourNextTurn)
                .setText(", or vigilance can't attack or block until your next turn"));
        ability.addTarget(new TargetPermanent(filter));
        this.addAbility(ability);

        // +1: Search your library and/or graveyard for a card named Monk of the Open Hand, reveal it, and put it into your hand. If you search your library this way, shuffle it.
        this.addAbility(new LoyaltyAbility(new SearchLibraryGraveyardPutInHandEffect(
                filter2, false, false
        ), 1));
    }

    private GrandMasterOfFlowers(final GrandMasterOfFlowers card) {
        super(card);
    }

    @Override
    public GrandMasterOfFlowers copy() {
        return new GrandMasterOfFlowers(this);
    }
}
