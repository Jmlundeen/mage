package mage.cards.s;

import mage.abilities.Ability;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.common.delayed.AtTheBeginOfPlayersNextEndStepDelayedTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ReturnFromExileEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.MoveCardsParameters;
import mage.players.Player;
import mage.target.TargetPlayer;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author xenohedron
 */
public final class Suppress extends CardImpl {

    public Suppress(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{2}{B}");

        // Target player exiles all cards from their hand face down.
        // At the beginning of the end step of that player's next turn, that player returns those cards to their hand.
        this.getSpellAbility().addEffect(new SuppressEffect());
        this.getSpellAbility().addTarget(new TargetPlayer());
    }

    private Suppress(final Suppress card) {
        super(card);
    }

    @Override
    public Suppress copy() {
        return new Suppress(this);
    }
}

class SuppressEffect extends OneShotEffect {

    SuppressEffect() {
        super(Outcome.Detriment);
        this.staticText = "target player exiles all cards from their hand face down. " +
                "At the beginning of the end step of that player's next turn, that player returns those cards to their hand";
    }

    private SuppressEffect(final SuppressEffect effect) {
        super(effect);
    }

    @Override
    public SuppressEffect copy() {
        return new SuppressEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (player == null || player.getHand().isEmpty()) {
            return false;
        }
        Cards cards = new CardsImpl(player.getHand());
        MoveCardsParameters parameters = new MoveCardsParameters(cards.getCards(game), Zone.EXILED)
                .setFaceDown(true)
                .setExileId(CardUtil.getExileZoneId(game, source))
                .setExileName(CardUtil.createObjectRelatedWindowTitle(source, game, null));
        player.moveCards(parameters, source, game);
        cards.retainZone(Zone.EXILED, game);
        DelayedTriggeredAbility ability = new AtTheBeginOfPlayersNextEndStepDelayedTriggeredAbility(
                new ReturnFromExileEffect(Zone.HAND).setText("that player returns those cards to their hand"), player.getId()
        ).setTriggerPhrase("At the beginning of the end step of that player's next turn, ");
        game.addDelayedTriggeredAbility(ability, source);
        return true;
    }
}
