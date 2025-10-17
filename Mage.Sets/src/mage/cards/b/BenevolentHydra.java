package mage.cards.b;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.abilities.effects.common.replacement.ReplaceCounterEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author Grath
 */
public final class BenevolentHydra extends CardImpl {

    public BenevolentHydra(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{X}{G}{G}");

        this.subtype.add(SubType.HYDRA);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // Hungering Hydra enters the battlefield with X +1/+1 counters on it.
        this.addAbility(new EntersBattlefieldAbility(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance())
        ));

        // If one or more +1/+1 counters would be put on another creature you control, that many plus one +1/+1 counters are put on it instead.
        this.addAbility(new SimpleStaticAbility(new ReplaceCounterEffect(ReplaceCounterEffect.ModificationType.ADD, 1)
                        .addValidCounterTypes(CounterType.P1P1)
                        .setPermanentFilter(StaticFilters.FILTER_ANOTHER_CREATURE_YOU_CONTROL)
                        .setText("If one or more +1/+1 counters would be put on another creature you control, that many plus one +1/+1 counters are put on it instead.")
        ));

        // {T}, Remove a +1/+1 counter from Benevolent Hydra: Put a +1/+1 counter on another target creature you control.
        Ability ability = new SimpleActivatedAbility(new AddCountersTargetEffect(CounterType.P1P1.createInstance()).setText("Put a +1/+1 counter on another target creature you control"), new TapSourceCost());
        ability.addCost(new RemoveCountersSourceCost(CounterType.P1P1.createInstance()));
        ability.addTarget(new TargetPermanent(StaticFilters.FILTER_CONTROLLED_ANOTHER_CREATURE));
        this.addAbility(ability);
    }

    private BenevolentHydra(final BenevolentHydra card) {
        super(card);
    }

    @Override
    public BenevolentHydra copy() {
        return new BenevolentHydra(this);
    }
}
