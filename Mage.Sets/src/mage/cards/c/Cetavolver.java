
package mage.cards.c;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.KickedCostCondition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.FirstStrikeAbility;
import mage.abilities.keyword.KickerAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;

import java.util.UUID;

/**
 *
 * @author Loki
 */
public final class Cetavolver extends CardImpl {

    public Cetavolver(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{1}{U}");
        this.subtype.add(SubType.VOLVER);

        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // Kicker {1}{R} and/or {G} (You may pay an additional {1}{R} and/or {G} as you cast this spell.)
        KickerAbility kickerAbility = new KickerAbility("{1}{R}");
        kickerAbility.addKickerCost("{G}");
        this.addAbility(kickerAbility);

        // If Cetavolver was kicked with its {1}{R} kicker, it enters with two +1/+1 counters on it and with first strike.
        Condition redKickerCondition = new KickedCostCondition("{1}{R}");
        Effect enterWithCountersEffect = new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance(2)),
                redKickerCondition)
                .setText("If {this} was kicked with its {1}{R} kicker, it enters with two +1/+1 counters on it and with first strike.");
        Effect gainFirstStrikeEffect = new ConditionalContinuousEffect(
                new ContinuousEffectBuilder(Duration.WhileOnBattlefield, Outcome.AddAbility, ContinuousAffected.SOURCE)
                        .withGainedAbilities(FirstStrikeAbility.getInstance()),
                redKickerCondition, "");
        Ability ability1 = new SimpleStaticAbility(enterWithCountersEffect);
        ability1.addEffect(gainFirstStrikeEffect);
        this.addAbility(ability1);

        // If Cetavolver was kicked with its {G} kicker, it enters with a +1/+1 counter on it and with trample.
        Condition greenKickerCondition = new KickedCostCondition("{G}");
        Effect enterWithCounterEffect = new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance()),
                greenKickerCondition)
                .setText("If {this} was kicked with its {G} kicker, it enters with a +1/+1 counter on it and with trample.");
        Effect gainTrampleEffect = new ConditionalContinuousEffect(
                new ContinuousEffectBuilder(Duration.WhileOnBattlefield, Outcome.AddAbility, ContinuousAffected.SOURCE)
                        .withGainedAbilities(TrampleAbility.getInstance()),
                greenKickerCondition, "");
        Ability ability2 = new SimpleStaticAbility(enterWithCounterEffect);
        ability2.addEffect(gainTrampleEffect);
        this.addAbility(ability2);
    }

    private Cetavolver(final Cetavolver card) {
        super(card);
    }

    @Override
    public Cetavolver copy() {
        return new Cetavolver(this);
    }
}
