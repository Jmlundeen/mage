package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;

/**
 * @author Noahsark
 */
public class LoseAbilitySourceEffect extends ContinuousEffectImpl {

    protected Ability ability;

    public LoseAbilitySourceEffect(Ability ability) {
        this(ability, Duration.WhileOnBattlefield);
    }

    public LoseAbilitySourceEffect(Ability ability, Duration duration) {
        super(duration, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.LoseAbility);
        this.ability = ability;
        staticText = ("{this} loses " + ability.getRule() + ' ' + duration.toString()).trim();
    }

    protected LoseAbilitySourceEffect(final LoseAbilitySourceEffect effect) {
        super(effect);
        this.ability = effect.ability.copy();
    }

    @Override
    public LoseAbilitySourceEffect copy() {
        return new LoseAbilitySourceEffect(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        if (duration.isOnlyValidIfNoZoneChange()) {
            // If source permanent is no longer onto battlefield discard the effect
            if (source.getSourcePermanentIfItStillExists(game) == null) {
                discard();
            }
        }
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).removeAbility(ability, source.getSourceId(), game);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = game.getPermanent(source.getSourceId());
        if (permanent != null) {
            affectedObjects.add(permanent);
            return true;
        } else {
            return false;
        }
    }
}
