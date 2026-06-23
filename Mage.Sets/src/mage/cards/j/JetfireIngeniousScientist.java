package mage.cards.j;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.RemoveVariableCountersTargetCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.dynamicvalue.common.cost.VariableCostValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.TransformSourceEffect;
import mage.abilities.effects.keyword.AdaptEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.LivingMetalAbility;
import mage.abilities.keyword.MoreThanMeetsTheEyeAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.abilities.mana.conditional.InvertedManaCondition;
import mage.abilities.mana.providers.common.player.TargetPointerManaPlayerProvider;
import mage.cards.CardSetInfo;
import mage.cards.TransformingDoubleFacedCard;
import mage.constants.CardType;
import mage.constants.ManaType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.filter.StaticTypedFilters;
import mage.target.TargetPlayer;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class JetfireIngeniousScientist extends TransformingDoubleFacedCard {

    public JetfireIngeniousScientist(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, new SubType[]{SubType.ROBOT}, "{4}{U}",
                "Jetfire, Air Guardian",
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.ARTIFACT}, new SubType[]{SubType.VEHICLE}, "U");

        // Jetfire, Ingenious Scientist
        this.getLeftHalfCard().setPT(3, 4);

        // More Than Meets the Eye {3}{U}
        this.getLeftHalfCard().addAbility(new MoreThanMeetsTheEyeAbility(this, "{3}{U}"));

        // Flying
        this.getLeftHalfCard().addAbility(FlyingAbility.getInstance());

        // Remove one or more +1/+1 counters from among artifacts you control: Target player adds that much {C}. This mana can't be spent to cast nonartifact spells. Convert Jetfire.
        Effect manaEffect = ComposedManaAbilityBuilder.builder()
                .addDynamic(VariableCostValue.instance, ManaType.COLORLESS)
                .playerProvider(TargetPointerManaPlayerProvider.instance)
                .condition(new InvertedManaCondition(new FilteredSpellManaCondition(StaticTypedFilters.A_NON_ARTIFACT_SPELL)))
                .ruleText("target player adds that much {C}. This mana can't be spent to cast a nonartifact spell. Convert {this}.")
                .buildEffect();
        Ability ability = new SimpleActivatedAbility(
                manaEffect,
                new RemoveVariableCountersTargetCost(
                        StaticFilters.FILTER_CONTROLLED_PERMANENT_ARTIFACTS,
                        CounterType.P1P1, "one or more", 1
                )
        );
        ability.addEffect(new TransformSourceEffect().setText("convert {this}"));
        ability.addTarget(new TargetPlayer());
        this.getLeftHalfCard().addAbility(ability);

        // Jetfire, Air Guardian
        this.getRightHalfCard().setPT(3, 4);

        // Living metal
        this.getRightHalfCard().addAbility(new LivingMetalAbility());

        // Flying
        this.getRightHalfCard().addAbility(FlyingAbility.getInstance());

        // {U}{U}{U}: Convert Jetfire, then adapt 3.
        Ability backAbility = new SimpleActivatedAbility(
                new TransformSourceEffect()
                        .setText("convert {this}"),
                new ManaCostsImpl<>("{U}{U}{U}")
        );
        backAbility.addEffect(new AdaptEffect(3).concatBy(", then"));
        this.getRightHalfCard().addAbility(backAbility);
    }

    private JetfireIngeniousScientist(final JetfireIngeniousScientist card) {
        super(card);
    }

    @Override
    public JetfireIngeniousScientist copy() {
        return new JetfireIngeniousScientist(this);
    }
}

