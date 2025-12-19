
package mage.cards.d;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.KickedCostCondition;
import mage.abilities.costs.common.PayLifeCost;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.RegenerateSourceEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.FirstStrikeAbility;
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
 *
 * @author Loki
 */
public final class Degavolver extends CardImpl {

    public Degavolver(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{1}{W}");
        this.subtype.add(SubType.VOLVER);

        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

         // Kicker {1}{B} and/or {R} (You may pay an additional {1}{B} and/or {R} as you cast this spell.)
        KickerAbility kickerAbility = new KickerAbility("{1}{B}");
        kickerAbility.addKickerCost("{R}");
        this.addAbility(kickerAbility);

        // If Degavolver was kicked with its {1}{B} kicker, it enters with two +1/+1 counters on it and with "Pay 3 life: Regenerate Degavolver."
        Condition blackKickerCondition = new KickedCostCondition("{1}{B}");
        ContinuousEffect enterWithCountersEffect = new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance(2)),
                blackKickerCondition)
                .setText("If {this} was kicked with its {1}{B} kicker, it enters with two +1/+1 counters on it and with \"Pay 3 life: Regenerate this creature.\"");
        ContinuousEffect gainAbilityEffect = new ConditionalContinuousEffect(
                new ContinuousEffectBuilder(Outcome.AddAbility, ContinuousAffected.SOURCE)
                        .withGainedAbilities(new SimpleActivatedAbility(new RegenerateSourceEffect(), new PayLifeCost(3))),
                blackKickerCondition, "");
        Ability ability1 = new SimpleStaticAbility(enterWithCountersEffect);
        ability1.addEffect(gainAbilityEffect);
        this.addAbility(ability1);

        // If Degavolver was kicked with its {R} kicker, it enters with a +1/+1 counter on it and with first strike.
        Condition redKickerCondition = new KickedCostCondition("{R}");
        ContinuousEffect entersWithCounterEffect = new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance()),
                redKickerCondition)
                .setText("If {this} was kicked with its {R} kicker, it enters with a +1/+1 counter on it and with first strike");
        Effect gainFirstStrikeEffect = new ConditionalContinuousEffect(
                new ContinuousEffectBuilder(Outcome.AddAbility, ContinuousAffected.SOURCE)
                        .withGainedAbilities(FirstStrikeAbility.getInstance()),
                redKickerCondition, "");

        Ability ability2 = new SimpleStaticAbility(entersWithCounterEffect);
        ability2.addEffect(gainFirstStrikeEffect);
        this.addAbility(ability2);
    }

    private Degavolver(final Degavolver card) {
        super(card);
    }

    @Override
    public Degavolver copy() {
        return new Degavolver(this);
    }
}
