package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;

public class VehiclesBecomeArtifactCreatureEffect extends ContinuousEffectImpl {

    public VehiclesBecomeArtifactCreatureEffect(Duration duration) {
        super(duration, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.BecomeCreature);
        staticText = "Vehicles you control become artifact creatures until end of turn";
    }

    private VehiclesBecomeArtifactCreatureEffect(final VehiclesBecomeArtifactCreatureEffect effect) {
        super(effect);
    }

    @Override
    public VehiclesBecomeArtifactCreatureEffect copy() {
        return new VehiclesBecomeArtifactCreatureEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            permanent.addCardType(game, CardType.ARTIFACT);
            permanent.addCardType(game, CardType.CREATURE);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (Permanent permanent : game.getBattlefield().getAllActivePermanents(source.getControllerId())) {
            if (permanent.hasSubtype(SubType.VEHICLE, game)) {
                affectedObjects.add(permanent);
            }
        }
        return !affectedObjects.isEmpty();
    }
}
