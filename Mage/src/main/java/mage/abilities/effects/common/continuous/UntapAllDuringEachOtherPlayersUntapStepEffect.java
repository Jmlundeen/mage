package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;

/**
 * @author LevelX2
 */
public class UntapAllDuringEachOtherPlayersUntapStepEffect extends ContinuousEffectImpl {

    private final FilterPermanent filter;

    public UntapAllDuringEachOtherPlayersUntapStepEffect(FilterPermanent filter) {
        super(Duration.WhileOnBattlefield, Layer.RulesEffects, SubLayer.NA, Outcome.Untap);
        this.filter = filter;
        staticText = setStaticText();
    }

    protected UntapAllDuringEachOtherPlayersUntapStepEffect(final UntapAllDuringEachOtherPlayersUntapStepEffect effect) {
        super(effect);
        this.filter = effect.filter;
    }

    @Override
    public UntapAllDuringEachOtherPlayersUntapStepEffect copy() {
        return new UntapAllDuringEachOtherPlayersUntapStepEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).untap(game);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Integer appliedTurn = (Integer) game.getState().getValue(source.getSourceId() + "appliedTurn");
        if (appliedTurn == null) {
            appliedTurn = 0;
        }
        if (appliedTurn < game.getTurnNum()) {
            game.getState().setValue(source.getSourceId() + "appliedTurn", game.getTurnNum());
            affectedObjects.addAll(game.getBattlefield().getAllActivePermanents(filter, source.getControllerId(), game));
        }
        return !affectedObjects.isEmpty();
    }

    private String setStaticText() {
        StringBuilder sb = new StringBuilder("Untap ");
        if (!filter.getMessage().startsWith("each")) {
            sb.append("all ");
        }
        sb.append(filter.getMessage());
        sb.append(" during each other player's untap step");
        return sb.toString();
    }
}
