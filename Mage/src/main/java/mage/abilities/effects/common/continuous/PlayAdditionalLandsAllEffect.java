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
import mage.util.CardUtil;

import java.util.List;

/**
 * Each player may play an additional land on each of their turns.
 *
 * @author nantuko
 */
public class PlayAdditionalLandsAllEffect extends ContinuousEffectImpl {

    private int numExtraLands = 1;

    public PlayAdditionalLandsAllEffect() {
        super(Duration.WhileOnBattlefield, Layer.PlayerEffects, SubLayer.NA, Outcome.Benefit);
        staticText = "Each player may play an additional land on each of their turns";
        numExtraLands = 1;
    }

    public PlayAdditionalLandsAllEffect(int numExtraLands) {
        super(Duration.WhileOnBattlefield, Layer.PlayerEffects, SubLayer.NA, Outcome.Benefit);
        this.numExtraLands = numExtraLands;
        if (numExtraLands == Integer.MAX_VALUE) {
            staticText = "Each player may play any number of additional lands on each of their turns";
        } else {
            staticText = "Each player may play an additional " + numExtraLands + " lands on each of their turns";
        }
    }

    protected PlayAdditionalLandsAllEffect(final PlayAdditionalLandsAllEffect effect) {
        super(effect);
        this.numExtraLands = effect.numExtraLands;
        this.staticText = effect.staticText;
    }

    @Override
    public PlayAdditionalLandsAllEffect copy() {
        return new PlayAdditionalLandsAllEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Player) object).setLandsPerTurn(CardUtil.overflowInc(((Player) object).getLandsPerTurn(), numExtraLands));
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player player = game.getPlayer(game.getActivePlayerId());
        if (player != null) {
            affectedObjects.add(player);
            return true;
        } else {
            return false;
        }
    }
}
