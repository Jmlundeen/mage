
package mage.cards.f;

import mage.MageInt;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.dynamicvalue.common.SourceXCostValue;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.TargetController;
import mage.counters.CounterType;

import java.util.UUID;

/**
 *
 * @author Plopman
 */
public final class FeralHydra extends CardImpl {

    public FeralHydra(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{X}{G}");
        this.subtype.add(SubType.HYDRA);
        this.subtype.add(SubType.BEAST);

        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Feral Hydra enters the battlefield with X +1/+1 counters on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.P1P1, SourceXCostValue.instance)));

        // {3}: Put a +1/+1 counter on Feral Hydra. Any player may activate this ability.
        SimpleActivatedAbility ability = new SimpleActivatedAbility(new AddCountersSourceEffect(CounterType.P1P1.createInstance()), new ManaCostsImpl<>("{3}"));
        ability.setMayActivate(TargetController.ANY);
        ability.addEffect(new InfoEffect("Any player may activate this ability"));
        this.addAbility(ability);
    }

    private FeralHydra(final FeralHydra card) {
        super(card);
    }

    @Override
    public FeralHydra copy() {
        return new FeralHydra(this);
    }
}
