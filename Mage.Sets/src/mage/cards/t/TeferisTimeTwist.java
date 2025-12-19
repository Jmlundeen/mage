package mage.cards.t;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetControlledPermanent;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class TeferisTimeTwist extends CardImpl {

    public TeferisTimeTwist(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{U}");

        // Exile target permanent you control. Return that card to the battlefield under its owner's control at the beginning of the next end step. If it enters the battlefield as a creature, it enters with an additional +1/+1 counter on it.
        this.getSpellAbility().addEffect(new TeferisTimeTwistEffect());
        this.getSpellAbility().addTarget(new TargetControlledPermanent());
    }

    private TeferisTimeTwist(final TeferisTimeTwist card) {
        super(card);
    }

    @Override
    public TeferisTimeTwist copy() {
        return new TeferisTimeTwist(this);
    }
}

class TeferisTimeTwistEffect extends OneShotEffect {

    TeferisTimeTwistEffect() {
        super(Outcome.Benefit);
        staticText = "Exile target permanent you control. Return that card to the battlefield " +
                "under its owner's control at the beginning of the next end step. " +
                "If it enters the battlefield as a creature, it enters with an additional +1/+1 counter on it.";
    }

    private TeferisTimeTwistEffect(final TeferisTimeTwistEffect effect) {
        super(effect);
    }

    @Override
    public TeferisTimeTwistEffect copy() {
        return new TeferisTimeTwistEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(source.getFirstTarget());
        Player player = game.getPlayer(source.getControllerId());
        if (permanent == null || player == null) {
            return false;
        }
        Effect effect = new TeferisTimeTwistReturnEffect(new MageObjectReference(
                permanent.getId(), permanent.getZoneChangeCounter(game) + 1, game
        ));
        if (!player.moveCards(permanent, Zone.EXILED, source, game)) {
            return false;
        }
        game.addDelayedTriggeredAbility(new AtTheBeginOfNextEndStepDelayedTriggeredAbility(effect), source);
        return true;
    }
}

class TeferisTimeTwistReturnEffect extends OneShotEffect {

    private final MageObjectReference mor;

    TeferisTimeTwistReturnEffect(MageObjectReference mor) {
        super(Outcome.Benefit);
        staticText = "return the exiled card to the battlefield";
        this.mor = mor;
    }

    private TeferisTimeTwistReturnEffect(final TeferisTimeTwistReturnEffect effect) {
        super(effect);
        this.mor = effect.mor;
    }

    @Override
    public TeferisTimeTwistReturnEffect copy() {
        return new TeferisTimeTwistReturnEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Card card = mor.getCard(game);
        if (card == null) {
            return false;
        }
        Player player = game.getPlayer(card.getOwnerId());
        if (player == null) {
            return false;
        }
        ContinuousEffect creatureCounter = new EntersWithCountersEffect(Duration.EndOfTurn, ContinuousAffected.STATIC_OR_DYNAMIC, CounterType.P1P1.createInstance())
                .setFilter(StaticFilters.FILTER_PERMANENT_CREATURE);
        creatureCounter.setTargetPointer(new FixedTarget(card.getId(), game));
        ContinuousEffect planeswalkerCounter = new EntersWithCountersEffect(Duration.EndOfTurn, ContinuousAffected.STATIC_OR_DYNAMIC, CounterType.LOYALTY.createInstance())
                .setFilter(StaticFilters.FILTER_PERMANENT_PLANESWALKER);
        planeswalkerCounter.setTargetPointer(new FixedTarget(card.getId(), game));
        game.addEffect(creatureCounter, source);
        game.addEffect(planeswalkerCounter, source);
        player.moveCards(card, Zone.BATTLEFIELD, source, game);
        return true;
    }
}
