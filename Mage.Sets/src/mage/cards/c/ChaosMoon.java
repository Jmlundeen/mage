package mage.cards.c;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateDelayedTriggeredAbilityEffect;
import mage.abilities.effects.common.continuous.generic.GenericContinuousEffect;
import mage.abilities.effects.mana.ReplaceManaEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.DelayedTriggeredManaAbility;
import mage.abilities.mana.providers.common.player.TargetPointerManaPlayerProvider;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterTyped;
import mage.filter.StaticFilters;
import mage.filter.StaticTypedFilters;
import mage.filter.predicate.typed.mageObject.color.ColorPredicate;
import mage.game.Game;
import mage.players.Player;

import java.util.UUID;

/**
 * @author L_J
 */
public final class ChaosMoon extends CardImpl {

    public ChaosMoon(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{R}");

        // At the beginning of each upkeep, count the number of permanents. If the number is odd, until end of turn, red creatures get +1/+1 and whenever a player taps a Mountain for mana, that player adds {R} (in addition to the mana the land produces). If the number is even, until end of turn, red creatures get -1/-1 and if a player taps a Mountain for mana, that Mountain produces colorless mana instead of any other type.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(TargetController.ANY, new ChaosMoonEffect(), false));
    }

    private ChaosMoon(final ChaosMoon card) {
        super(card);
    }

    @Override
    public ChaosMoon copy() {
        return new ChaosMoon(this);
    }
}

class ChaosMoonEffect extends OneShotEffect {

    private static final FilterTyped filter = new FilterTyped("red creatures")
            .addAll(
                    CardType.CREATURE.getPredicate(),
                    ColorPredicate.RED
            );

    ChaosMoonEffect() {
        super(Outcome.Neutral);
        this.staticText = "count the number of permanents. If the number is odd, " +
                "until end of turn, red creatures get +1/+1 and whenever a player taps a Mountain for mana, " +
                "that player adds an additional {R}. If the number is even, " +
                "until end of turn, red creatures get -1/-1 and if a player taps a Mountain for mana, " +
                "that Mountain produces colorless mana instead of any other type";
    }

    private ChaosMoonEffect(final ChaosMoonEffect effect) {
        super(effect);
    }

    @Override
    public ChaosMoonEffect copy() {
        return new ChaosMoonEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        int permanentsInPlay = game.getBattlefield().count(
                StaticFilters.FILTER_PERMANENT, source.getControllerId(), source, game
        );
        // Odd
        GenericContinuousEffect boostEffect = new GenericContinuousEffect(Duration.EndOfTurn, Outcome.BoostCreature, filter);
        if (permanentsInPlay % 2 == 1) {
            game.addEffect(boostEffect
                    .withAddPower(1)
                    .withAddToughness(1),
                    source);
            new CreateDelayedTriggeredAbilityEffect(
                    new DelayedTriggeredManaAbility(
                            "whenever a player taps a Mountain for mana, ",
                            StaticTypedFilters.A_MOUNTAIN,
                            ComposedManaAbilityBuilder.builder()
                                    .addStatic(Mana.RedMana(1))
                                    .playerProvider(TargetPointerManaPlayerProvider.instance)
                                    .ruleText("that player adds an additional {R}")
                                    .buildEffect(),
                            Duration.EndOfTurn,
                            false
                    )
                            .withSetTargetPointer(SetTargetPointer.TRIGGERED_CONTROLLER)
            ).apply(game, source);
        } // Even
        else {
            game.addEffect(boostEffect
                    .withAddPower(-1)
                    .withAddToughness(-1),
                    source);
            game.addEffect(ReplaceManaEffect.produced(Duration.EndOfTurn,
                    Outcome.Neutral, ReplaceManaEffect.replaceAllWithColor(ManaType.COLORLESS)), source);
        }
        return true;
    }
}
