package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;

public class CreaturesBecomeOtherTypeEffect extends ContinuousEffectImpl {

    protected final FilterPermanent filter;
    protected final SubType subType;

    public CreaturesBecomeOtherTypeEffect(FilterPermanent filter, SubType subType, Duration duration) {
        super(duration, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Neutral);
        this.filter = filter;
        this.subType = subType;

        this.dependendToTypes.add(DependencyType.BecomeCreature);  // Opalescence and Starfield of Nyx
        this.staticText = this.filter.getMessage() + " is " + this.subType.getIndefiniteArticle()
                + " " + this.subType + " in addition to its other types";
    }

    protected CreaturesBecomeOtherTypeEffect(final CreaturesBecomeOtherTypeEffect effect) {
        super(effect);
        this.filter = effect.filter;
        this.subType = effect.subType;
    }

    @Override
    public CreaturesBecomeOtherTypeEffect copy() {
        return new CreaturesBecomeOtherTypeEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).addSubType(game, this.subType);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        if (!source.getAffectedObjects().isEmpty()) {
            affectedObjects.addAll(source.getAffectedObjects());
        } else {
            for (Permanent permanent : game.getBattlefield().getActivePermanents(filter, source.getControllerId(), source, game)) {
                affectedObjects.add(permanent);
                source.getAffectedObjects().add(permanent);
            }
        }
        return !affectedObjects.isEmpty();
    }
}
