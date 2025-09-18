package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.game.Game;

import java.util.List;

/**
 * @author TheElk801
 */
public class IsAllCreatureTypesSourceEffect extends ContinuousEffectImpl {

    public IsAllCreatureTypesSourceEffect() {
        super(Duration.Custom, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Benefit);
        this.staticText = "{this} is every creature type <i>(even if this card isn't on the battlefield)</i>.";
    }

    private IsAllCreatureTypesSourceEffect(final IsAllCreatureTypesSourceEffect effect) {
        super(effect);
    }

    @Override
    public IsAllCreatureTypesSourceEffect copy() {
        return new IsAllCreatureTypesSourceEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((MageObject) object).setIsAllCreatureTypes(game, true);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        MageObject sourceObject = game.getObject(source);
        if (sourceObject == null) {
            return false;
        }
        affectedObjects.add(sourceObject);
        return true;
    }
}
