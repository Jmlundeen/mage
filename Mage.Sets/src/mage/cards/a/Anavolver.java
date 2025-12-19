

package mage.cards.a;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.KickedCostCondition;
import mage.abilities.costs.common.PayLifeCost;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.RegenerateSourceEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.KickerAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ContinuousAffected;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 * @author Loki
 */
public final class Anavolver extends CardImpl {

    public Anavolver(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{3}{G}");
        this.subtype.add(SubType.VOLVER);

        this.power = new MageInt(3);
        this.toughness = new MageInt(3);
        
        // Kicker {1}{U} and/or {B} (You may pay an additional {1}{U} and/or {B} as you cast this spell.)
        KickerAbility kickerAbility = new KickerAbility("{1}{U}");
        kickerAbility.addKickerCost("{B}");
        this.addAbility(kickerAbility);

        // If Anavolver was kicked with its {1}{U} kicker, it enters with two +1/+1 counters on it and with flying.
        Condition blueKickerCondition = new KickedCostCondition("{1}{U}");
        Effect enterWithCountersEffect = new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance(2)),
                blueKickerCondition)
                .setText("If {this} was kicked with its {1}{U} kicker, it enters with two +1/+1 counters on it and with flying.");
        Effect gainFlyingEffect = new ConditionalContinuousEffect(
                new ContinuousEffectBuilder(Outcome.AddAbility, ContinuousAffected.SOURCE)
                        .withGainedAbilities(FlyingAbility.getInstance()),
                blueKickerCondition, "");
        Ability ability1 = new SimpleStaticAbility(enterWithCountersEffect);
        ability1.addEffect(gainFlyingEffect);
        this.addAbility(ability1);

        // If Anavolver was kicked with its {B} kicker, it enters with a +1/+1 counter on it and with "Pay 3 life: Regenerate Anavolver."
        Condition blackKickerCondition = new KickedCostCondition("{B}");
        Effect enterWithCounterEffect = new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance(1)),
                blackKickerCondition)
                .setText("If {this} was kicked with its {B} kicker, it enters with a +1/+1 counter on it and with \"Pay 3 life: Regenerate this creature.\"");
        Effect gainRegenerateEffect = new ConditionalContinuousEffect(
                new ContinuousEffectBuilder(Outcome.AddAbility, ContinuousAffected.SOURCE)
                        .withGainedAbilities(new SimpleActivatedAbility(new RegenerateSourceEffect(), new PayLifeCost(3))),
                blackKickerCondition, "");
        Ability ability2 = new SimpleStaticAbility(enterWithCounterEffect);
        ability2.addEffect(gainRegenerateEffect);
        this.addAbility(ability2);
    }

    private Anavolver(final Anavolver card) {
        super(card);
    }

    @Override
    public Anavolver copy() {
        return new Anavolver(this);
    }
}
