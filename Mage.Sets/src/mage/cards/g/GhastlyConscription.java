package mage.cards.g;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.keyword.ManifestEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.MoveCardsParameters;
import mage.players.Player;
import mage.target.TargetPlayer;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class GhastlyConscription extends CardImpl {

    public GhastlyConscription(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{5}{B}{B}");

        // Exile all creature cards from target player's graveyard in a face-down pile, shuffle that pile, then manifest those cards.<i> (To manifest a card, put it onto the battlefield face down as a 2/2 creature. Turn it face up at any time for its mana cost if it's a creature card.)</i>
        this.getSpellAbility().addEffect(new GhastlyConscriptionEffect());
        this.getSpellAbility().addTarget(new TargetPlayer());
    }

    private GhastlyConscription(final GhastlyConscription card) {
        super(card);
    }

    @Override
    public GhastlyConscription copy() {
        return new GhastlyConscription(this);
    }
}

class GhastlyConscriptionEffect extends OneShotEffect {

    GhastlyConscriptionEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "Exile all creature cards from target player's graveyard in a face-down pile, " +
                "shuffle that pile, then manifest those cards. " +
                "<i>(To manifest a card, put it onto the battlefield face down as a 2/2 creature. " +
                "Turn it face up at any time for its mana cost if it's a creature card.)</i>";
    }

    private GhastlyConscriptionEffect(final GhastlyConscriptionEffect effect) {
        super(effect);
    }

    @Override
    public GhastlyConscriptionEffect copy() {
        return new GhastlyConscriptionEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Player targetPlayer = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (controller == null || targetPlayer == null) {
            return false;
        }
        Cards cardsToManifest = new CardsImpl(targetPlayer.getGraveyard().getCards(StaticFilters.FILTER_CARD_CREATURE, game));
        MoveCardsParameters parameters = new MoveCardsParameters(cardsToManifest.getCards(game), Zone.EXILED)
                .setFaceDown(true);
        controller.moveCards(parameters, source, game);
        if (cardsToManifest.isEmpty()) {
            return true;
        }
        cardsToManifest.retainZone(Zone.EXILED, game);
        cardsToManifest.shuffle();
        game.informPlayers(controller.getLogName() + " shuffles the face-down pile");
        game.processAction();
        ManifestEffect.doManifestCards(game, source, controller, cardsToManifest.getCards(game));
        return true;
    }
}
