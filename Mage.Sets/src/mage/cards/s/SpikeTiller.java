
package mage.cards.s;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.target.common.TargetCreaturePermanent;
import mage.target.common.TargetLandPermanent;

import java.util.UUID;

/**
 *
 * @author L_J
 */
public final class SpikeTiller extends CardImpl {

    public SpikeTiller(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{3}{G}{G}");
        this.subtype.add(SubType.SPIKE);

        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Spike Tiller enters the battlefield with three +1/+1 counters on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.P1P1.createInstance(3))));

        // {2}, Remove a +1/+1 counter from Spike Tiller: Put a +1/+1 counter on target creature.
        Ability ability = new SimpleActivatedAbility(new AddCountersTargetEffect(CounterType.P1P1.createInstance()), new GenericManaCost(2));
        ability.addCost(new RemoveCountersSourceCost(CounterType.P1P1.createInstance()));
        ability.addTarget(new TargetCreaturePermanent());
        this.addAbility(ability);

        // {2}, Remove a +1/+1 counter from Spike Tiller: Target land becomes a 2/2 creature that's still a land. Put a +1/+1 counter on it.
        ContinuousEffect animateEffect = new ContinuousEffectBuilder(Duration.EndOfGame, Outcome.BecomeCreature)
                .withAddedCardTypes(false, CardType.CREATURE)
                .withSetPower(2)
                .withSetToughness(2)
                .setText("Target land becomes a 2/2 creature that's still a land");
        Ability ability2 = new SimpleActivatedAbility(animateEffect, new GenericManaCost(2));
        ability2.addCost(new RemoveCountersSourceCost(CounterType.P1P1.createInstance()));
        ability2.addEffect(new AddCountersTargetEffect(CounterType.P1P1.createInstance()).setText("Put a +1/+1 counter on it."));
        ability2.addTarget(new TargetLandPermanent());
        this.addAbility(ability2);
    }

    private SpikeTiller(final SpikeTiller card) {
        super(card);
    }

    @Override
    public SpikeTiller copy() {
        return new SpikeTiller(this);
    }
}
