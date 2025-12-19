

package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.filter.common.FilterCreaturePermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.Iterator;
import java.util.List;

/**
 * @author LevelX2
 */

public class SwitchPowerToughnessAllEffect extends ContinuousEffectImpl {

    static FilterCreaturePermanent filter = new FilterCreaturePermanent();

    public SwitchPowerToughnessAllEffect(Duration duration) {
        super(duration, Layer.PTChangingEffects_7, SubLayer.SwitchPT_e, Outcome.BoostCreature);
        this.staticText = "Switch each creature's power and toughness" + (duration.toString().isEmpty() ? "" : " " + duration.toString());
    }

    protected SwitchPowerToughnessAllEffect(final SwitchPowerToughnessAllEffect effect) {
        super(effect);
    }

    @Override
    public SwitchPowerToughnessAllEffect copy() {
        return new SwitchPowerToughnessAllEffect(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        if (getAffectedObjectsSet() && game.getPlayer(source.getControllerId()) != null) {
            for (Permanent perm : game.getBattlefield().getActivePermanents(filter, source.getControllerId(), source, game)) {
                affectedObjectList.add(new MageObjectReference(perm, game));
            }
        }
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).switchPowerToughness();
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        if (getAffectedObjectsSet()) {
            for (Iterator<MageObjectReference> it = affectedObjectList.iterator(); it.hasNext(); ) { // filter may not be used again, because object can have changed filter relevant attributes but still gets boost
                Permanent creature = it.next().getPermanent(game);
                if (creature == null) {
                    it.remove();
                    continue;
                }
                affectedObjects.add(creature);
            }
        } else {
            affectedObjects.addAll(game.getBattlefield().getActivePermanents(filter, source.getControllerId(), source, game));
        }
        return !affectedObjects.isEmpty();
    }
}
