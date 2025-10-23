package mage.cards.a;

import mage.abilities.Ability;
import mage.abilities.LoyaltyAbility;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.continuous.CastAsThoughItHadFlashAllEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.DayboundAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.FilterCard;
import mage.filter.StaticFilters;
import mage.filter.common.FilterCreatureCard;
import mage.game.permanent.token.WolfToken;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class ArlinnThePacksHope extends CardImpl {

    private static final FilterCard filter = new FilterCreatureCard("creature spells");

    public ArlinnThePacksHope(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.PLANESWALKER}, "{2}{R}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ARLINN);
        this.setStartingLoyalty(4);
        this.secondSideCardClazz = mage.cards.a.ArlinnTheMoonsFury.class;

        // Daybound
        this.addAbility(new DayboundAbility());

        // +1: Until your next turn, you may cast creature spells as though they had flash, and each creature you control enters the battlefield with an additional +1/+1 counter on it.
        Ability ability = new LoyaltyAbility(new CastAsThoughItHadFlashAllEffect(
                Duration.UntilYourNextTurn, filter
        ).setText("until your next turn, you may cast creature spells as though they had flash"), 1);
        ability.addEffect(new EntersWithCountersEffect(Duration.UntilYourNextTurn, ContinuousAffected.STATIC_OR_DYNAMIC, CounterType.P1P1.createInstance())
                .setFilter(StaticFilters.FILTER_CONTROLLED_CREATURE).concatBy(", and"));
        this.addAbility(ability);

        // −3: Create two 2/2 green Wolf creature tokens.
        this.addAbility(new LoyaltyAbility(new CreateTokenEffect(new WolfToken(), 2), -3));
    }

    private ArlinnThePacksHope(final ArlinnThePacksHope card) {
        super(card);
    }

    @Override
    public ArlinnThePacksHope copy() {
        return new ArlinnThePacksHope(this);
    }
}
