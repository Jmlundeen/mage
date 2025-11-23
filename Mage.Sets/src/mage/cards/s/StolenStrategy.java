package mage.cards.s;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.MoveCardsParameters;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class StolenStrategy extends CardImpl {

    public StolenStrategy(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{4}{R}");

        // At the beginning of your upkeep, exile the top card of each opponent's library. Until end of turn, you may cast nonland cards from among those exiled cards, and you may spend mana as though it were mana of any color to cast those spells.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(new StolenStrategyEffect()));
    }

    private StolenStrategy(final StolenStrategy card) {
        super(card);
    }

    @Override
    public StolenStrategy copy() {
        return new StolenStrategy(this);
    }
}

class StolenStrategyEffect extends OneShotEffect {

    StolenStrategyEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "exile the top card of each opponent's library. "
                + "Until end of turn, you may cast nonland cards from among those exiled cards, "
                + "and you may spend mana as though it were mana of any color to cast those spells";
    }

    private StolenStrategyEffect(final StolenStrategyEffect effect) {
        super(effect);
    }

    @Override
    public StolenStrategyEffect copy() {
        return new StolenStrategyEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        for (UUID playerId : game.getState().getPlayersInRange(controller.getId(), game)) {
            if (!controller.hasOpponent(playerId, game)) {
                continue;
            }
            Player damagedPlayer = game.getPlayer(playerId);
            if (damagedPlayer == null) {
                continue;
            }
            Card card = damagedPlayer.getLibrary().getFromTop(game);
            if (card != null) {
                // move card to exile
                MoveCardsParameters parameters = new MoveCardsParameters(card, Zone.EXILED)
                        .setExileId(CardUtil.getExileZoneId(game, source))
                        .setExileName(CardUtil.createObjectRelatedWindowTitle(source, game, null));
                controller.moveCards(parameters, source, game);
                // Add effects only if the card has a spellAbility (e.g. not for lands).
                if (!card.isLand(game) && card.getSpellAbility() != null) {
                    // allow to cast the card
                    // and you may spend mana as though it were mana of any color to cast it
                    CardUtil.makeCardPlayable(game, source, card, true, Duration.EndOfTurn, true);
                }
            }
        }
        return true;
    }
}
