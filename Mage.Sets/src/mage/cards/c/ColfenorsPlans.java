
package mage.cards.c;

import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.SkipDrawStepEffect;
import mage.abilities.effects.common.continuous.CantCastMoreThanOneSpellEffect;
import mage.abilities.effects.common.continuous.LookAtCardsExiledWithThisEffect;
import mage.abilities.effects.common.continuous.MayPlayCardsExiledWithThisEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.MoveCardsParameters;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.Set;
import java.util.UUID;

/**
 *
 * @author Styxo
 */
public final class ColfenorsPlans extends CardImpl {

    public ColfenorsPlans(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{B}{B}");

        // When Colfenor's Plans enters the battlefield, exile the top seven cards of your library face down.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new ColfenorsPlansExileEffect(), false));

        // You may look at and play cards exiled with Colfenor's Plans.
        this.addAbility(new SimpleStaticAbility(new MayPlayCardsExiledWithThisEffect()));
        this.addAbility(new SimpleStaticAbility(new LookAtCardsExiledWithThisEffect()));

        // Skip your draw step.
        this.addAbility(new SimpleStaticAbility(new SkipDrawStepEffect()));

        // You can't cast more than one spell each turn.
        this.addAbility(new SimpleStaticAbility(new CantCastMoreThanOneSpellEffect(TargetController.YOU)));

    }

    private ColfenorsPlans(final ColfenorsPlans card) {
        super(card);
    }

    @Override
    public ColfenorsPlans copy() {
        return new ColfenorsPlans(this);
    }
}

class ColfenorsPlansExileEffect extends OneShotEffect {

    ColfenorsPlansExileEffect() {
        super(Outcome.DrawCard);
        staticText = "exile the top seven cards of your library face down";
    }

    private ColfenorsPlansExileEffect(final ColfenorsPlansExileEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller != null) {
            Set<Card> toExile = controller.getLibrary().getTopCards(game, 7);
            UUID exileId = CardUtil.getCardExileZoneId(game, source);
            MoveCardsParameters parameters = new MoveCardsParameters(toExile, Zone.EXILED)
                    .setCanLookFaceDownInExile(true)
                    .setFaceDown(true)
                    .setExileId(exileId)
                    .setExileName(CardUtil.createObjectRelatedWindowTitle(source, game, null));
            controller.moveCards(parameters, source, game);
            return true;
        }
        return false;
    }

    @Override
    public ColfenorsPlansExileEffect copy() {
        return new ColfenorsPlansExileEffect(this);
    }
}