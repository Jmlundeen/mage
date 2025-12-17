
package mage.cards.v;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.delayed.AtTheBeginOfYourNextUpkeepDelayedTriggeredAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.MoveCardsParameters;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCreaturePermanent;
import mage.target.targetpointer.FixedTargets;

import java.util.UUID;

/**
 * @author escplan9 (Derek Monturo - dmontur1 at gmail dot com)
 */
public final class VanishIntoMemory extends CardImpl {

    public VanishIntoMemory(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{2}{W}{U}");

        // Exile target creature. You draw cards equal to that creature's power.
        // At the beginning of your next upkeep, return that card to the battlefield under its owner's control. If you do, discard cards equal to that creature's toughness.
        this.getSpellAbility().addEffect(new VanishIntoMemoryEffect());
        this.getSpellAbility().addTarget(new TargetCreaturePermanent());
    }

    private VanishIntoMemory(final VanishIntoMemory card) {
        super(card);
    }

    @Override
    public VanishIntoMemory copy() {
        return new VanishIntoMemory(this);
    }
}

class VanishIntoMemoryEffect extends OneShotEffect {

    VanishIntoMemoryEffect() {
        super(Outcome.Detriment);
        staticText = "Exile target creature. You draw cards equal to that creature's power. At the beginning of your next upkeep, return that card to the battlefield under its owner's control. If you do, discard cards equal to that creature's toughness";
    }

    private VanishIntoMemoryEffect(final VanishIntoMemoryEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(source.getFirstTarget());
        Player controller = game.getPlayer(source.getControllerId());
        MageObject sourceObject = game.getObject(source);
        if (controller != null && permanent != null && sourceObject != null) {
            MoveCardsParameters parameters = new MoveCardsParameters(permanent, Zone.EXILED)
                    .setExileId(source.getSourceId())
                    .setExileName(sourceObject.getIdName());
            controller.moveCardsWithResult(parameters, source, game)
                    .stream()
                    .findFirst()
                    .ifPresent(card -> {
                        controller.drawCards(permanent.getPower().getValue(), source, game);
                        ExileZone exileZone = game.getExile().getExileZone(source.getSourceId());
                        if (exileZone == null) {
                            return;
                        }
                        Effect effect = new VanishIntoMemoryReturnFromExileEffect();
                        effect.setTargetPointer(new FixedTargets(exileZone, game));
                        game.addDelayedTriggeredAbility(new AtTheBeginOfYourNextUpkeepDelayedTriggeredAbility(effect), source);
                    });
            return true;
        }
        return false;
    }

    @Override
    public VanishIntoMemoryEffect copy() {
        return new VanishIntoMemoryEffect(this);
    }
}

class VanishIntoMemoryReturnFromExileEffect extends OneShotEffect {

    VanishIntoMemoryReturnFromExileEffect() {
        super(Outcome.PutCardInPlay);
        staticText = "return that card to the battlefield under its owner's control. If you do, discard cards equal to that creature's toughness";
    }

    private VanishIntoMemoryReturnFromExileEffect(final VanishIntoMemoryReturnFromExileEffect effect) {
        super(effect);
    }

    @Override
    public VanishIntoMemoryReturnFromExileEffect copy() {
        return new VanishIntoMemoryReturnFromExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Cards cards = new CardsImpl(getTargetPointer().getTargets(game, source));
        Player controller = game.getPlayer(source.getControllerId());
        if (controller != null) {
            MoveCardsParameters parameters = new MoveCardsParameters(cards.getCards(game), Zone.BATTLEFIELD)
                    .setByOwner(true);
            controller.moveCardsWithResult(parameters, source, game)
                    .stream()
                    .filter(c -> c instanceof Permanent)
                    .map(c -> (Permanent) c)
                    .findFirst()
                    .ifPresent(permanent -> controller.discard(permanent.getToughness().getValue(), false, false, source, game));
        }

        return true;
    }
}
