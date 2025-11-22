package mage.cards.c;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
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
import mage.target.TargetCard;
import mage.target.common.TargetOpponent;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Styxo
 */
public final class CunningAbduction extends CardImpl {

    public CunningAbduction(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{1}{U}{B}");

        // Target opponent reveals their hand. You choose a nonland card from that player's hand and exile it. You may cast that card for as long as it remains exiled, and you may spend mana as though it were mana of any color to cast that spell.
        this.getSpellAbility().addTarget(new TargetOpponent());
        this.getSpellAbility().addEffect(new CunningAbductionExileEffect());
    }

    private CunningAbduction(final CunningAbduction card) {
        super(card);
    }

    @Override
    public CunningAbduction copy() {
        return new CunningAbduction(this);
    }
}

class CunningAbductionExileEffect extends OneShotEffect {

    private static final FilterNonlandCard filter = new FilterNonlandCard();

    public CunningAbductionExileEffect() {
        super(Outcome.Benefit);
        this.staticText = "Target opponent reveals their hand. You choose a nonland card from that player's hand and exile it. You may cast that card for as long as it remains exiled, and you may spend mana as though it were mana of any color to cast that spell";
    }

    private CunningAbductionExileEffect(final CunningAbductionExileEffect effect) {
        super(effect);
    }

    @Override
    public CunningAbductionExileEffect copy() {
        return new CunningAbductionExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player opponent = game.getPlayer(getTargetPointer().getFirst(game, source));
        MageObject sourceObject = game.getObject(source);
        if (opponent != null && sourceObject != null) {
            opponent.revealCards(sourceObject.getName(), opponent.getHand(), game);
            Player controller = game.getPlayer(source.getControllerId());
            if (controller != null) {
                int cardsHand = opponent.getHand().count(filter, game);
                Card card = null;
                if (cardsHand > 0) {
                    TargetCard target = new TargetCard(Zone.HAND, filter);
                    if (controller.choose(Outcome.Benefit, opponent.getHand(), target, source, game)) {
                        card = opponent.getHand().get(target.getFirstTarget(), game);
                    }
                }
                if (card != null) {
                    // move card to exile
                    MoveCardsParameters parameters = new MoveCardsParameters(card, Zone.EXILED)
                            .setExileId(CardUtil.getExileZoneId(game, source))
                            .setExileName(CardUtil.createObjectRelatedWindowTitle(source, game, null));
                    controller.moveCards(parameters, source, game);
                    // allow to cast the card
                    CardUtil.makeCardPlayable(game, source, card, true, Duration.Custom, true);
                }
                return true;
            }
        }
        return false;
    }
}
