package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;

/**
 * @author fireshoes
 */
public class UntapSourceDuringEachOtherPlayersUntapStepEffect extends ContinuousEffectImpl {

    public UntapSourceDuringEachOtherPlayersUntapStepEffect() {
        super(Duration.WhileOnBattlefield, Layer.RulesEffects, SubLayer.NA, Outcome.Untap);
        staticText = "Untap {this} during each other player's untap step";
    }

    protected UntapSourceDuringEachOtherPlayersUntapStepEffect(final UntapSourceDuringEachOtherPlayersUntapStepEffect effect) {
        super(effect);
    }

    @Override
    public UntapSourceDuringEachOtherPlayersUntapStepEffect copy() {
        return new UntapSourceDuringEachOtherPlayersUntapStepEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).untap(game);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = game.getPermanent(source.getSourceId());
        if (permanent == null) {
            return false;
        }
        boolean applied = Boolean.TRUE.equals(game.getState().getValue(source.getSourceId() + "applied"));
        if (!applied && !source.isControlledBy(game.getActivePlayerId()) && game.getTurnStepType() == PhaseStep.UNTAP) {
            affectedObjects.add(permanent);
        } else if (applied && game.getTurnStepType() == PhaseStep.END_TURN) {
            game.getState().setValue(source.getSourceId() + "applied", false);
        }
        return !affectedObjects.isEmpty();
    }
}
