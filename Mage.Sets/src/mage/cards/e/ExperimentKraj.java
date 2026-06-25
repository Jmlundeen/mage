
package mage.cards.e;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.continuous.layers.L6_Abilities.GainAbilitiesOfEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.ability.type.ActivatedAbilityPredicate;
import mage.filter.predicate.typed.card.ICardPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.filter.predicate.typed.mageObject.object.AnotherPredicate;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class ExperimentKraj extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("activated abilities of each other creature with a +1/+1 counter on it")
            .addAll(
                    IMageObjectPredicate.getOSPPredicate(CardType.CREATURE.getPredicate()),
                    ICardPredicate.getOSPPredicate(CounterType.P1P1.getPredicate()),
                    AnotherPredicate.instance
            )
            .add(ActivatedAbilityPredicate.instance);


    public ExperimentKraj(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}{G}{U}{U}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.OOZE);
        this.subtype.add(SubType.MUTANT);

        this.power = new MageInt(4);
        this.toughness = new MageInt(6);

        // Experiment Kraj has all activated abilities of each other creature with a +1/+1 counter on it.
        this.addAbility(new SimpleStaticAbility(new GainAbilitiesOfEffect()
                .setAbilityFilter(filter, Zone.BATTLEFIELD)
                .setText("{this} has all activated abilities of each other creature with a +1/+1 counter on it")
        ));

        // {tap}: Put a +1/+1 counter on target creature.
        Ability ability = new SimpleActivatedAbility(new AddCountersTargetEffect(CounterType.P1P1.createInstance()), new TapSourceCost());
        ability.addTarget(new TargetCreaturePermanent());
        this.addAbility(ability);
    }

    private ExperimentKraj(final ExperimentKraj card) {
        super(card);
    }

    @Override
    public ExperimentKraj copy() {
        return new ExperimentKraj(this);
    }
}
