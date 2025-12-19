package mage.cards.r;

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
 * @author LoneFox
 */
public final class Rakavolver extends CardImpl {

    public Rakavolver(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{R}");
        this.subtype.add(SubType.VOLVER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Kicker {1}{W} and/or {U}
        KickerAbility kickerAbility = new KickerAbility("{1}{W}");
        kickerAbility.addKickerCost("{U}");
        this.addAbility(kickerAbility);

        // If Rakavolver was kicked with its {1}{W} kicker, it enters with two +1/+1 counters on it and with "Whenever Rakavolver deals damage, you gain that much life."
        Condition whiteKickerCondition = new KickedCostCondition("{1}{W}");
        ContinuousEffect enterWithCountersEffect = new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance(2)),
                whiteKickerCondition)
                .setText("If {this} was kicked with its {1}{W} kicker, it enters with two +1/+1 counters on it");
        ContinuousEffect gainAbilityEffect = new ConditionalContinuousEffect(
                new ContinuousEffectBuilder(Outcome.AddAbility, ContinuousAffected.SOURCE)
                        .withGainedAbilities(new DealsDamageSourceTriggeredAbility(new GainLifeEffect(SavedDamageValue.MUCH), false)),
                whiteKickerCondition, "and with \"Whenever {this} deals damage, you gain that much life.\""
        );
        Ability ability = new SimpleStaticAbility(enterWithCountersEffect);
        ability.addEffect(gainAbilityEffect);
        this.addAbility(ability);

        // If Rakavolver was kicked with its {U} kicker, it enters with a +1/+1 counter on it and with flying.
        Condition blueKickerCondition = new KickedCostCondition("{U}");
        ContinuousEffect enterWithCounterEffect = new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance(1)),
                blueKickerCondition)
                .setText("If {this} was kicked with its {U} kicker, it enters with a +1/+1 counter on it");
        ContinuousEffect gainFlyingEffect = new ConditionalContinuousEffect(
                new ContinuousEffectBuilder(Outcome.AddAbility, ContinuousAffected.SOURCE)
                        .withGainedAbilities(FlyingAbility.getInstance()),
                blueKickerCondition, "and with flying."
        );
        ability = new SimpleStaticAbility(enterWithCounterEffect);
        ability.addEffect(gainFlyingEffect);
        this.addAbility(ability);
    }

    private Rakavolver(final Rakavolver card) {
        super(card);
    }

    @Override
    public Rakavolver copy() {
        return new Rakavolver(this);
    }
}
