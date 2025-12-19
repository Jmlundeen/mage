

package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.constants.Duration;
import mage.abilities.Ability;
import mage.abilities.keyword.ProtectionAbility;
import mage.constants.Layer;
import mage.filter.FilterCard;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;

/**
 * @author ayratn
 */
public class GainProtectionFromTypeTargetEffect extends GainAbilityTargetEffect {

    private final String typeName;

    public GainProtectionFromTypeTargetEffect(Duration duration, FilterCard protectionFrom) {
        super(new ProtectionAbility(new FilterCard()), duration);
        ((ProtectionAbility) ability).setFilter(protectionFrom);
        typeName = protectionFrom.getMessage();
        staticText = "Target creature gains protection from " + typeName + ' ' + duration.toString();
    }

    protected GainProtectionFromTypeTargetEffect(final GainProtectionFromTypeTargetEffect effect) {
        super(effect);
        this.typeName = effect.typeName;
    }

    @Override
    public GainProtectionFromTypeTargetEffect copy() {
        return new GainProtectionFromTypeTargetEffect(this);
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent creature = game.getPermanent(source.getFirstTarget());
        if (creature == null) {
            return false;
        }
        affectedObjects.add(source);
        return true;
    }
}
