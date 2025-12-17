package mage.cards.e;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.common.FilterCreatureCard;
import mage.game.Game;
import mage.game.MoveCardsParameters;
import mage.players.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author escplan9 (Derek Monturo - dmontur1 at gmail dot com)
 */
public final class EmptyTheCatacombs extends CardImpl {

    public EmptyTheCatacombs(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{3}{B}");

        // Each player returns all creature cards from their graveyard to their hand.
        this.getSpellAbility().addEffect(new EmptyTheCatacombsEffect());
    }

    private EmptyTheCatacombs(final EmptyTheCatacombs card) {
        super(card);
    }

    @Override
    public EmptyTheCatacombs copy() {
        return new EmptyTheCatacombs(this);
    }
}

class EmptyTheCatacombsEffect extends OneShotEffect {

    private static final FilterCreatureCard filter = new FilterCreatureCard("creature");

    public EmptyTheCatacombsEffect() {
        super(Outcome.ReturnToHand);
        staticText = "Each player returns all creature cards from their graveyard to their hand";
    }

    private EmptyTheCatacombsEffect(final EmptyTheCatacombsEffect effect) {
        super(effect);
    }

    @Override
    public EmptyTheCatacombsEffect copy() {
        return new EmptyTheCatacombsEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Set<Card> toHand = new HashSet<>();
        Player controller = game.getPlayer(source.getControllerId());
        if (controller != null) {
            for (UUID playerId : game.getState().getPlayersInRange(source.getControllerId(), game)) {
                Player player = game.getPlayer(playerId);
                if (player != null) {
                    for (Card card : player.getGraveyard().getCards(filter, game)) {
                        if (card != null) {
                            toHand.add(card);
                        }
                    }
                }
            }

            // must happen simultaneously Rule 101.4
            MoveCardsParameters parameters = new MoveCardsParameters(toHand, Zone.HAND)
                    .setByOwner(true);
            controller.moveCards(parameters, source, game);
            return true;
        }
        return false;
    }
}
