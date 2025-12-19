package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.Effect;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.util.CardUtil;

import java.util.List;

/**
 * @author TheElk801
 */
public class GainAnchorWordAbilitySourceEffect extends ContinuousEffectImpl {

    private final Ability ability;
    private final ModeChoice modeChoice;

    public GainAnchorWordAbilitySourceEffect(Effect effect, ModeChoice modeChoice) {
        this(new SimpleStaticAbility(effect), modeChoice);
    }

    public GainAnchorWordAbilitySourceEffect(Ability ability, ModeChoice modeChoice) {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.staticText = "&bull " + modeChoice + " &mdash; " + CardUtil.getTextWithFirstCharUpperCase(ability.getRule());
        this.ability = ability;
        this.modeChoice = modeChoice;
        this.ability.setRuleVisible(false);
        this.generateGainAbilityDependencies(ability, null);
    }

    private GainAnchorWordAbilitySourceEffect(final GainAnchorWordAbilitySourceEffect effect) {
        super(effect);
        this.modeChoice = effect.modeChoice;
        this.ability = effect.ability;
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).addAbility(ability, source.getSourceId(), game);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent == null || !modeChoice.checkMode(game, source)) {
            return false;
        }
        affectedObjects.add(permanent);
        return true;
    }

    @Override
    public GainAnchorWordAbilitySourceEffect copy() {
        return new GainAnchorWordAbilitySourceEffect(this);
    }
}
