package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.game.Game;
import mage.players.Player;

import java.util.List;
import java.util.UUID;

/**
 * @author nantuko
 */
public class PlayWithTheTopCardRevealedEffect extends ContinuousEffectImpl {

    protected boolean allPlayers;

    public PlayWithTheTopCardRevealedEffect() {
        this(false);
    }

    public PlayWithTheTopCardRevealedEffect(boolean allPlayers) {
        super(Duration.WhileOnBattlefield, Layer.PlayerEffects, SubLayer.NA, Outcome.Detriment);
        this.allPlayers = allPlayers;
        if (allPlayers) {
            staticText = "Players play with the top card of their libraries revealed.";
        } else {
            staticText = "Play with the top card of your library revealed";
        }
    }

    protected PlayWithTheTopCardRevealedEffect(final PlayWithTheTopCardRevealedEffect effect) {
        super(effect);
        this.allPlayers = effect.allPlayers;
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Player) object).setTopCardRevealed(true);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        if (allPlayers) {
            for (UUID playerId : game.getState().getPlayersInRange(controller.getId(), game)) {
                Player player = game.getPlayer(playerId);
                if (player != null && canLookAtNextTopLibraryCard(game)) {
                    affectedObjects.add(player);
                }
            }
        } else if (canLookAtNextTopLibraryCard(game)) {
            affectedObjects.add(controller);
        }
        return !affectedObjects.isEmpty();
    }

    @Override
    public PlayWithTheTopCardRevealedEffect copy() {
        return new PlayWithTheTopCardRevealedEffect(this);
    }

}
