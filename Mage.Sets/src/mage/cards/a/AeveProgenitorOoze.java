package mage.cards.a;

import mage.MageInt;
import mage.abilities.common.EntersBattlefieldAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.SourceMatchesFilterCondition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.dynamicvalue.common.PermanentsOnBattlefieldCount;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.StormAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.filter.predicate.permanent.TokenPredicate;

import java.util.UUID;

/**
 * @author weirddan455
 */
public final class AeveProgenitorOoze extends CardImpl {

    private static final FilterControlledPermanent filter = new FilterControlledPermanent(SubType.OOZE, "other Ooze you control");
    private static final FilterPermanent tokenFilter = new FilterCreaturePermanent();
    private static final Condition isToken = new SourceMatchesFilterCondition(tokenFilter);

    static {
        filter.add(AnotherPredicate.instance);
        tokenFilter.add(TokenPredicate.TRUE);
    }

    public AeveProgenitorOoze(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}{G}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.OOZE);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Storm
        this.addAbility(new StormAbility());

        // Aeve, Progenitor Ooze isn't legendary if it's a token.
        this.addAbility(new SimpleStaticAbility(new ConditionalContinuousEffect(
                new ContinuousEffectBuilder(Duration.WhileOnBattlefield, Outcome.Benefit, ContinuousAffected.SOURCE)
                        .withRemovedSuperTypes(SuperType.LEGENDARY),
                isToken,
                "{this} isn't legendary if it's a token"
        )));

        // Aeve enters the battlefield with a +1/+1 counter on it for each other Ooze you control.
        this.addAbility(new EntersBattlefieldAbility(new EntersWithCountersEffect(CounterType.P1P1, new PermanentsOnBattlefieldCount(filter))));
    }

    private AeveProgenitorOoze(final AeveProgenitorOoze card) {
        super(card);
    }

    @Override
    public AeveProgenitorOoze copy() {
        return new AeveProgenitorOoze(this);
    }
}
