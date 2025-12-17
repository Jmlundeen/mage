package mage.cards.w;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.LeavesBattlefieldTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ReturnFromExileForSourceEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.MoveCardsParameters;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class WormfangBehemoth extends CardImpl {

    public WormfangBehemoth(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{U}{U}");

        this.subtype.add(SubType.NIGHTMARE);
        this.subtype.add(SubType.FISH);
        this.subtype.add(SubType.BEAST);
        this.power = new MageInt(5);
        this.toughness = new MageInt(5);

        // When Wormfang Behemoth enters the battlefield, exile all cards from your hand.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new WormfangBehemothEffect()));

        // When Wormfang Behemoth leaves the battlefield, return the exiled cards to their owner's hand.
        this.addAbility(new LeavesBattlefieldTriggeredAbility(
                new ReturnFromExileForSourceEffect(Zone.HAND).withText(true, false, false), false
        ));
    }

    private WormfangBehemoth(final WormfangBehemoth card) {
        super(card);
    }

    @Override
    public WormfangBehemoth copy() {
        return new WormfangBehemoth(this);
    }
}

class WormfangBehemothEffect extends OneShotEffect {

    WormfangBehemothEffect() {
        super(Outcome.Benefit);
        staticText = "exile all cards from your hand";
    }

    private WormfangBehemothEffect(final WormfangBehemothEffect effect) {
        super(effect);
    }

    @Override
    public WormfangBehemothEffect copy() {
        return new WormfangBehemothEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null || player.getHand().isEmpty()) {
            return false;
        }
        MoveCardsParameters parameters = new MoveCardsParameters(player.getHand().getCards(game), Zone.EXILED)
                .setExileId(CardUtil.getExileZoneId(game, source))
                .setExileName(CardUtil.createObjectRelatedWindowTitle(source, game, null));
        player.moveCards(parameters, source, game);
        return true;
    }
}
