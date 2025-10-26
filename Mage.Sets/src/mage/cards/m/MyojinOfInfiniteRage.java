
package mage.cards.m;

import mage.MageInt;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.CastFromHandSourcePermanentCondition;
import mage.abilities.condition.common.SourceHasCounterCondition;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.DestroyAllEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.IndestructibleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.watchers.common.CastFromHandWatcher;

import java.util.UUID;

/**
 * @author LevelX
 */
public final class MyojinOfInfiniteRage extends CardImpl {

    public MyojinOfInfiniteRage(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{7}{R}{R}{R}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SPIRIT);

        this.power = new MageInt(7);
        this.toughness = new MageInt(4);

        this.getSpellAbility().addWatcher(new CastFromHandWatcher());

        // Myojin of Infinite Rage enters the battlefield with a divinity counter on it if you cast it from your hand.
        this.addAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.DIVINITY.createInstance()),
                CastFromHandSourcePermanentCondition.instance)
                .setText("{this} enters with a divinity counter on it if you cast it from your hand")
        ), new CastFromHandWatcher());

        // Myojin of Infinite Rage has indestructible as long as it has a divinity counter on it.
        this.addAbility(new SimpleStaticAbility(new ConditionalContinuousEffect(
                new ContinuousEffectBuilder(Outcome.AddAbility, ContinuousAffected.SOURCE)
                        .withGainedAbilities(IndestructibleAbility.getInstance()),
                new SourceHasCounterCondition(CounterType.DIVINITY),
                "{this} has indestructible as long as it has a divinity counter on it")
        ));

        // Remove a divinity counter from Myojin of Infinite Rage: Destroy all lands.
        this.addAbility(new SimpleActivatedAbility(new DestroyAllEffect(StaticFilters.FILTER_LANDS), new RemoveCountersSourceCost(CounterType.DIVINITY.createInstance())));
    }

    private MyojinOfInfiniteRage(final MyojinOfInfiniteRage card) {
        super(card);
    }

    @Override
    public MyojinOfInfiniteRage copy() {
        return new MyojinOfInfiniteRage(this);
    }
}
