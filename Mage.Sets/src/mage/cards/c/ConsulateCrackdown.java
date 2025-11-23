
package mage.cards.c;

import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.delayed.OnLeaveReturnExiledAbility;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.common.FilterArtifactPermanent;
import mage.game.Game;
import mage.game.MoveCardsParameters;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author JRHerlehy
 */
public final class ConsulateCrackdown extends CardImpl {

    public ConsulateCrackdown(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{W}{W}");


        // When Consulate Crackdown enters the battlefield, exile all artifacts your opponents control until Consulate Crackdown leaves the battlefield.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new ConsulateCracksownExileEffect(), false));
    }

    private ConsulateCrackdown(final ConsulateCrackdown card) {
        super(card);
    }

    @Override
    public ConsulateCrackdown copy() {
        return new ConsulateCrackdown(this);
    }
}

class ConsulateCracksownExileEffect extends OneShotEffect {

    private static final FilterArtifactPermanent filter = new FilterArtifactPermanent("artifacts your opponents control");

    static {
        filter.add(TargetController.OPPONENT.getControllerPredicate());
    }

    public ConsulateCracksownExileEffect() {
        super(Outcome.Benefit);
        this.staticText = "exile all artifacts your opponents control until {this} leaves the battlefield";
    }

    private ConsulateCracksownExileEffect(final ConsulateCracksownExileEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);

        //If the permanent leaves the battlefield before the ability resolves, artifacts won't be exiled.
        if (permanent == null || controller == null) return false;

        Set<Card> toExile = new LinkedHashSet<>(game.getBattlefield().getActivePermanents(filter, controller.getId(), game));

        if (!toExile.isEmpty()) {
            MoveCardsParameters parameters = new MoveCardsParameters(toExile, Zone.EXILED)
                    .setExileId(CardUtil.getExileZoneId(game, source))
                    .setExileName(CardUtil.createObjectRelatedWindowTitle(source, game, null));
            controller.moveCards(parameters, source, game);
            game.addDelayedTriggeredAbility(new OnLeaveReturnExiledAbility(), source);
        }

        return true;
    }

    @Override
    public ConsulateCracksownExileEffect copy() {
        return new ConsulateCracksownExileEffect(this);
    }
}
