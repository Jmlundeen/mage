
package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.filter.FilterPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;

/**
 * @author LevelX2
 */
public class LoseAllAbilitiesAllEffect extends ContinuousEffectImpl {

    private final FilterPermanent filter;

    public LoseAllAbilitiesAllEffect(FilterPermanent filter, Duration duration) {
        super(duration, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.LoseAbility);
        this.filter = filter;
    }

    protected LoseAllAbilitiesAllEffect(final LoseAllAbilitiesAllEffect effect) {
        super(effect);
        this.filter = effect.filter.copy();
    }

    @Override
    public LoseAllAbilitiesAllEffect copy() {
        return new LoseAllAbilitiesAllEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).removeAllAbilities(source.getSourceId(), game);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        affectedObjects.addAll(game.getBattlefield().getActivePermanents(filter, source.getControllerId(), source, game));
        return !affectedObjects.isEmpty();
    }

    @Override
    public String getText(Mode mode) {
        if (staticText != null && !staticText.isEmpty()) {
            return staticText;
        }
        StringBuilder sb = new StringBuilder();
        if (duration == Duration.EndOfTurn) {
            sb.append(duration.toString()).append(", ");
        }
        sb.append(filter.getMessage()).append(" lose all abilities.");
        return sb.toString();
    }

}
