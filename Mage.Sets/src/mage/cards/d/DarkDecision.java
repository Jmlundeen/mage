package mage.cards.d;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.costs.common.PayLifeCost;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.asthought.PlayFromNotOwnHandZoneTargetEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.common.FilterNonlandCard;
import mage.game.Game;
import mage.game.MoveCardsParameters;
import mage.players.Player;
import mage.target.common.TargetCardInLibrary;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.UUID;

/**
 *
 * @author Styxo
 */
public final class DarkDecision extends CardImpl {

    public DarkDecision(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{B}{R}");

        // As an additional cost to cast Dark Decision, pay 1 life.
        this.getSpellAbility().addCost(new PayLifeCost(1));

        // Search the top 10 cards of your library for a nonland card, exile it, then shuffle your library. Until end of turn, you may cast that card.
        this.getSpellAbility().addEffect(new DarkDecisionEffect());
    }

    private DarkDecision(final DarkDecision card) {
        super(card);
    }

    @Override
    public DarkDecision copy() {
        return new DarkDecision(this);
    }
}

class DarkDecisionEffect extends OneShotEffect {

    DarkDecisionEffect() {
        super(Outcome.DrawCard);
        this.staticText = "Search the top 10 cards of your library for a nonland card, exile it, then shuffle. Until end of turn, you may cast that card";
    }

    private DarkDecisionEffect(final DarkDecisionEffect effect) {
        super(effect);
    }

    @Override
    public DarkDecisionEffect copy() {
        return new DarkDecisionEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        MageObject sourceObject = source.getSourceObject(game);
        if (controller != null && sourceObject != null) {
            TargetCardInLibrary target = new TargetCardInLibrary(new FilterNonlandCard());
            target.setCardLimit(10);
            if (controller.searchLibrary(target, source, game)) {
                UUID targetId = target.getFirstTarget();
                Card card = game.getCard(targetId);
                if (card != null) {
                    MoveCardsParameters parameters = new MoveCardsParameters(card, Zone.EXILED)
                            .setExileId(CardUtil.getExileZoneId(game, source))
                            .setExileName(CardUtil.createObjectRelatedWindowTitle(source, game, null));
                    controller.moveCards(parameters, source, game);
                    ContinuousEffect effect = new PlayFromNotOwnHandZoneTargetEffect(Zone.EXILED, Duration.EndOfTurn);
                    effect.setTargetPointer(new FixedTarget(card.getId(), game));
                    game.addEffect(effect, source);
                }
                controller.shuffleLibrary(source, game);
            }
            return true;
        }
        return false;
    }

}
