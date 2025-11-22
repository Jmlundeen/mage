package mage.cards.p;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.counters.Counters;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.MoveCardsParameters;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author jeffwadsworth
 */
public final class PlanarIncision extends CardImpl {

    public PlanarIncision(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{U}");

        // Exile target artifact or creature, then return it to the battlefield under its owner’s control with a +1/+1 counter on it.
        this.getSpellAbility().addEffect(new PlanarIncisionEffect());
        this.getSpellAbility().addTarget(new TargetPermanent(StaticFilters.FILTER_PERMANENT_ARTIFACT_OR_CREATURE));
    }

    private PlanarIncision(final PlanarIncision card) {
        super(card);
    }

    @Override
    public PlanarIncision copy() {
        return new PlanarIncision(this);
    }
}

class PlanarIncisionEffect extends OneShotEffect {

    PlanarIncisionEffect() {
        super(Outcome.Benefit);
        staticText = "Exile target artifact or creature, then return it to the battlefield under its owner's control with a +1/+1 counter on it";
    }

    private PlanarIncisionEffect(final PlanarIncisionEffect effect) {
        super(effect);
    }

    @Override
    public PlanarIncisionEffect copy() {
        return new PlanarIncisionEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(source.getFirstTarget());
        Player controller = game.getPlayer(source.getControllerId());
        if (permanent != null && controller != null) {
            controller.moveCardsWithResult(new MoveCardsParameters(permanent, Zone.EXILED), source, game)
                    .stream()
                    .findFirst()
                    .ifPresent(card -> {
                        Counters countersToAdd = new Counters();
                        countersToAdd.addCounter(CounterType.P1P1.createInstance());
                        game.setEnterWithCounters(card.getId(), countersToAdd);
                        MoveCardsParameters parameters = new MoveCardsParameters(card, Zone.BATTLEFIELD)
                                .setByOwner(true);
                        controller.moveCards(parameters, source, game);
                    });
        }
        return false;
    }
}
