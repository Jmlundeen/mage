

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
import mage.players.Player;

import java.util.List;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class GainAbilityControllerEffect extends ContinuousEffectImpl {

    protected Ability ability;

    /**
     * Add ability with Duration.WhileOnBattlefield
     *
     * @param ability
     */
    public GainAbilityControllerEffect(Ability ability) {
        this(ability, Duration.WhileOnBattlefield);
    }

    /**
     * @param ability
     * @param duration custom - effect will be discarded as soon there is no sourceId - permanent on the battlefield
     */
    public GainAbilityControllerEffect(Ability ability, Duration duration) {
        super(duration, Layer.PlayerEffects, SubLayer.NA, Outcome.AddAbility);
        this.ability = ability;
        staticText = "you " + (duration == Duration.WhileOnBattlefield ? "have" : "gain") + ' ' + ability.getRule();
        if (!duration.toString().isEmpty()) {
            staticText += ' ' + duration.toString();
        }
    }

    protected GainAbilityControllerEffect(final GainAbilityControllerEffect effect) {
        super(effect);
        this.ability = effect.ability.copy();
    }

    @Override
    public GainAbilityControllerEffect copy() {
        return new GainAbilityControllerEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Player) object).addAbility(ability);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player player = game.getPlayer(source.getControllerId());
        Permanent permanent = game.getPermanent(source.getSourceId());
        if (permanent == null && duration == Duration.Custom) {
            // discard effect if the source permanent is not on the battlefield
            discard();
            return false;
        }
        if (player != null) {
            affectedObjects.add(player);
            return true;
        }
        return false;
    }

}
