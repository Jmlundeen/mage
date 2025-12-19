package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.*;
import mage.game.Game;
import mage.players.Player;

import java.util.List;

/**
 * @author TheElk801
 */
public class YouDontLoseManaEffect extends ContinuousEffectImpl {

    private final ManaType manaType;

    public YouDontLoseManaEffect(ManaType manaType) {
        this(Duration.WhileOnBattlefield, manaType);
    }

    public YouDontLoseManaEffect(Duration duration, ManaType manaType) {
        super(duration, Layer.RulesEffects, SubLayer.NA, Outcome.Detriment);
        staticText = (duration == Duration.EndOfTurn ? "until end of turn, " : "") +
                "you don't lose unspent " + manaType + " mana as steps and phases end";
        this.manaType = manaType;
    }

    private YouDontLoseManaEffect(final YouDontLoseManaEffect effect) {
        super(effect);
        this.manaType = effect.manaType;
    }

    @Override
    public YouDontLoseManaEffect copy() {
        return new YouDontLoseManaEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Player player = (Player) object;
            player.getManaPool().addDoNotEmptyManaType(manaType);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player player = game.getPlayer(source.getControllerId());
        if (player != null) {
            affectedObjects.add(player);
            return true;
        }
        return false;
    }
}
