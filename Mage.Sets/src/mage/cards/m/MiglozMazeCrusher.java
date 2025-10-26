package mage.cards.m;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.DestroyTargetEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.MenaceAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class MiglozMazeCrusher extends CardImpl {

    public MiglozMazeCrusher(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{R}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.PHYREXIAN);
        this.subtype.add(SubType.BEAST);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // Migloz, Maze Crusher enters the battlefield with five oil counters on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.OIL.createInstance(5))));

        // {1}, Remove an oil counter from Migloz: It gains vigilance and menace until end of turn.
        Effect gainAbilitiesEffect = new ContinuousEffectBuilder(Duration.EndOfTurn, Outcome.AddAbility, ContinuousAffected.SOURCE)
                .withGainedAbilities(VigilanceAbility.getInstance(), new MenaceAbility(false))
                .setText("it gains vigilance and menace until end of turn");
        Ability ability = new SimpleActivatedAbility(gainAbilitiesEffect, new GenericManaCost(1));
        ability.addCost(new RemoveCountersSourceCost(CounterType.OIL.createInstance()));
        this.addAbility(ability);

        // {2}, Remove two oil counters from Migloz: It gets +2/+2 until end of turn.
        Effect boostEffect = new ContinuousEffectBuilder(Duration.EndOfTurn, Outcome.BoostCreature, ContinuousAffected.SOURCE)
                .withAddPower(2)
                .withAddToughness(2)
                .setText("it gets +2/+2 until end of turn");
        ability = new SimpleActivatedAbility(boostEffect, new GenericManaCost(2));
        ability.addCost(new RemoveCountersSourceCost(CounterType.OIL.createInstance(2)));
        this.addAbility(ability);

        // {3}, Remove three oil counters from Migloz: Destroy target artifact or enchantment.
        ability = new SimpleActivatedAbility(new DestroyTargetEffect(), new GenericManaCost(3));
        ability.addCost(new RemoveCountersSourceCost(CounterType.OIL.createInstance(3)));
        ability.addTarget(new TargetPermanent(StaticFilters.FILTER_PERMANENT_ARTIFACT_OR_ENCHANTMENT));
        this.addAbility(ability);
    }

    private MiglozMazeCrusher(final MiglozMazeCrusher card) {
        super(card);
    }

    @Override
    public MiglozMazeCrusher copy() {
        return new MiglozMazeCrusher(this);
    }
}
