package mage.abilities.effects.common.combat;

import mage.MageItem;
import mage.MageObjectReference;
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
 * @author TheElk801
 */
public class GoadAllEffect extends ContinuousEffectImpl {

    private final FilterPermanent filter;

    public GoadAllEffect(FilterPermanent filter) {
        this(Duration.UntilYourNextTurn, filter);
    }

    public GoadAllEffect(Duration duration, FilterPermanent filter) {
        this(duration, filter, true);
    }

    public GoadAllEffect(Duration duration, FilterPermanent filter, boolean affectedObjectsSet) {
        super(duration, Layer.RulesEffects, SubLayer.NA, Outcome.Detriment);
        this.filter = filter;
        this.setAffectedObjectsSet(affectedObjectsSet);
    }

    private GoadAllEffect(final GoadAllEffect effect) {
        super(effect);
        this.filter = effect.filter;
    }

    @Override
    public GoadAllEffect copy() {
        return new GoadAllEffect(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        if (getAffectedObjectsSet()) {
            game.getBattlefield()
                    .getActivePermanents(
                            filter, source.getControllerId(), source, game
                    ).stream()
                    .map(permanent -> new MageObjectReference(permanent, game))
                    .forEach(this.affectedObjectList::add);
        }
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).addGoadingPlayer(source.getControllerId());
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        if (getAffectedObjectsSet()) {
            this.affectedObjectList.removeIf(mor -> !mor.zoneCounterIsCurrent(game)
                    || mor.getPermanent(game) == null);
            if (affectedObjectList.isEmpty()) {
                discard();
                return false;
            }
            for (MageObjectReference mor : this.affectedObjectList) {
                affectedObjects.add(mor.getPermanent(game));
            }
        } else {
            if (!source.getAffectedObjects().isEmpty()) {
                affectedObjects.addAll(source.getAffectedObjects());
            } else {
                for (Permanent permanent : game.getBattlefield().getActivePermanents(filter, source.getControllerId(), source, game)) {
                    affectedObjects.add(permanent);
                    source.getAffectedObjects().add(permanent);
                }
            }
        }
        return !affectedObjects.isEmpty();
    }

    @Override
    public String getText(Mode mode) {
        if (staticText != null && !staticText.isEmpty()) {
            return staticText;
        }
        return "Goad all " + filter.getMessage() + ". <i>(Until your next turn, those creatures attack each combat if able and attack a player other than you if able.)</i>";
    }
}
