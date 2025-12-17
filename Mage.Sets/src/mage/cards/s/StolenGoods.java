package mage.cards.s;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.asthought.PlayFromNotOwnHandZoneTargetEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetOpponent;

import java.util.UUID;

/**
 * @author noxx
 */
public final class StolenGoods extends CardImpl {

    public StolenGoods(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{3}{U}");

        // Target opponent exiles cards from the top of their library until they exile a nonland card. Until end of turn, you may cast that card without paying its mana cost.
        this.getSpellAbility().addEffect(new StolenGoodsEffect());
        this.getSpellAbility().addTarget(new TargetOpponent());
    }

    private StolenGoods(final StolenGoods card) {
        super(card);
    }

    @Override
    public StolenGoods copy() {
        return new StolenGoods(this);
    }
}

class StolenGoodsEffect extends OneShotEffect {

    StolenGoodsEffect() {
        super(Outcome.Detriment);
        this.staticText = "Target opponent exiles cards from the top of their library until they exile a nonland card. Until end of turn, you may cast that card without paying its mana cost";
    }

    private StolenGoodsEffect(final StolenGoodsEffect effect) {
        super(effect);
    }

    @Override
    public StolenGoodsEffect copy() {
        return new StolenGoodsEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player opponent = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (opponent == null) {
            return false;
        }
        Card card;
        do {
            card = opponent.getLibrary().getFromTop(game);
            if (card == null) {
                break;
            }
            if (card.isLand(game)) {
                if (!opponent.moveCards(card, Zone.EXILED, source, game)) {
                    break;
                }
            } else {
                PlayFromNotOwnHandZoneTargetEffect.exileAndPlayFromExile(game, source, card, TargetController.YOU, Duration.EndOfTurn, true, false, true);
                break;
            }
        } while (card.isLand(game));

        return true;
    }
}
