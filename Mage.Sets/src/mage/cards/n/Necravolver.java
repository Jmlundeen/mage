package mage.cards.n;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.DealsDamageSourceTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.KickedCostCondition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.dynamicvalue.common.SavedDamageValue;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.common.GainLifeEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.KickerAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ContinuousAffected;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 * @author LoneFox
 */
public final class Necravolver extends CardImpl {

    public Necravolver(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}");
        this.subtype.add(SubType.VOLVER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Kicker {1}{G} and/or {W}
        KickerAbility kickerAbility = new KickerAbility("{1}{G}");
        kickerAbility.addKickerCost("{W}");
        this.addAbility(kickerAbility);

        // If Necravolver was kicked with its {1}{G} kicker, it enters with two +1/+1 counters on it and with trample.
        Condition greenKickerCondition = new KickedCostCondition("{1}{G}");
        ContinuousEffect enterWithCountersEffect = new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance(2)),
                greenKickerCondition)
                .setText("If {this} was kicked with its {1}{G} kicker, it enters with two +1/+1 counters on it");
        ContinuousEffect gainTrampleEffect = new ConditionalContinuousEffect(
                new ContinuousEffectBuilder(Outcome.AddAbility, ContinuousAffected.SOURCE)
                        .withGainedAbilities(TrampleAbility.getInstance()),
                greenKickerCondition, "and with trample");
        Ability ability = new SimpleStaticAbility(enterWithCountersEffect);
        ability.addEffect(gainTrampleEffect);
        this.addAbility(ability);

        // If Necravolver was kicked with its {W} kicker, it enters with a +1/+1 counter on it and with "Whenever Necravolver deals damage, you gain that much life."
        Condition whiteKickerCondition = new KickedCostCondition("{W}");
        ContinuousEffect enterWithOneCounterEffect = new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance()),
                whiteKickerCondition)
                .setText("If {this} was kicked with its {W} kicker, it enters with a +1/+1 counter on it");
        ContinuousEffect gainLifeAbilityEffect = new ConditionalContinuousEffect(
                new ContinuousEffectBuilder(Outcome.AddAbility, ContinuousAffected.SOURCE)
                        .withGainedAbilities(new DealsDamageSourceTriggeredAbility(new GainLifeEffect(SavedDamageValue.MUCH), false)),
                whiteKickerCondition,
                "and with \"Whenever {this} deals damage, you gain that much life.\"");
        Ability ability2 = new SimpleStaticAbility(enterWithOneCounterEffect);
        ability2.addEffect(gainLifeAbilityEffect);
        this.addAbility(ability2);
    }

    private Necravolver(final Necravolver card) {
        super(card);
    }

    @Override
    public Necravolver copy() {
        return new Necravolver(this);
    }
}
