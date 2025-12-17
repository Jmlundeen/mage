
package mage.cards.s;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.cards.*;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.FilterCard;
import mage.game.Game;
import mage.game.MoveCardsParameters;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.common.TargetOpponent;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class StruggleForSanity extends CardImpl {

    public StruggleForSanity(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.SORCERY},"{2}{B}{B}");

        // Target opponent reveals their hand. That player exiles a card from it, then you exile a card from it. Repeat this process until all cards in that hand have been exiled. That player returns the cards they exiled this way to their hand and puts the rest into their graveyard.
        this.getSpellAbility().addEffect(new StruggleForSanityEffect());
        this.getSpellAbility().addTarget(new TargetOpponent());
    }

    private StruggleForSanity(final StruggleForSanity card) {
        super(card);
    }

    @Override
    public StruggleForSanity copy() {
        return new StruggleForSanity(this);
    }
}

class StruggleForSanityEffect extends OneShotEffect {

    StruggleForSanityEffect() {
        super(Outcome.Discard); // kind of
        this.staticText = "Target opponent reveals their hand. That player exiles a card from it, then you exile a card from it. Repeat this process until all cards in that hand have been exiled. That player returns the cards they exiled this way to their hand and puts the rest into their graveyard";
    }

    private StruggleForSanityEffect(final StruggleForSanityEffect effect) {
        super(effect);
    }

    @Override
    public StruggleForSanityEffect copy() {
        return new StruggleForSanityEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player targetPlayer = game.getPlayer(getTargetPointer().getFirst(game, source));
        MageObject sourceObject = source.getSourceObject(game);
        Player controller = game.getPlayer(source.getControllerId());
        if (targetPlayer == null || sourceObject == null || controller == null) {
            return false;
        }
        targetPlayer.revealCards(sourceObject.getIdName(), targetPlayer.getHand(), game);

        Cards cardsLeft = new CardsImpl(targetPlayer.getHand());
        Cards exiledByController = new CardsImpl();
        UUID exileZoneController = UUID.randomUUID();
        Cards exiledByOpponent = new CardsImpl();
        UUID exileZoneOpponent = UUID.randomUUID();
        boolean opponentsChoice = true;
        TargetCard target = new TargetCard(Zone.HAND, new FilterCard("a card to exile"));
        MoveCardsParameters opponentParameters = new MoveCardsParameters(Zone.EXILED)
                .setExileId(exileZoneOpponent)
                .setExileName(sourceObject.getIdName() + " exiled by " + targetPlayer.getName());
        MoveCardsParameters controllerParameters = new MoveCardsParameters(Zone.EXILED)
                .setExileId(exileZoneController)
                .setExileName(sourceObject.getIdName() + " exiled by " + controller.getName());
        while (!cardsLeft.isEmpty()) {
            if (opponentsChoice) {
                targetPlayer.choose(Outcome.ReturnToHand, cardsLeft, target, source, game);
                Card card = game.getCard(target.getFirstTarget());
                if (card != null) {
                    exiledByOpponent.add(card);
                    cardsLeft.remove(card);
                    opponentParameters.setCards(card);
                    controller.moveCards(opponentParameters, source, game);
                }
            } else {
                controller.choose(Outcome.Discard, cardsLeft, target, source, game);
                Card card = game.getCard(target.getFirstTarget());
                if (card != null) {
                    exiledByController.add(card);
                    cardsLeft.remove(card);
                    controllerParameters.setCards(card);
                    controller.moveCards(controllerParameters, source, game);
                }
            }
            target.clearChosen();
            opponentsChoice = !opponentsChoice;

        }
        targetPlayer.moveCards(exiledByOpponent, Zone.HAND, source, game);
        targetPlayer.moveCards(exiledByController, Zone.GRAVEYARD, source, game);
        return true;
    }
}
