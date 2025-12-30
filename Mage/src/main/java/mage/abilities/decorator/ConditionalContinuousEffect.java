package mage.abilities.decorator;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.condition.Condition;
import mage.abilities.condition.FixedCondition;
import mage.abilities.condition.LockedInCondition;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.Duration;
import mage.constants.EffectType;
import mage.constants.Layer;
import mage.constants.SubLayer;
import mage.game.Game;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds condition to {@link ContinuousEffect}. Acts as decorator.
 *
 * @author nantuko
 */
public class ConditionalContinuousEffect extends ContinuousEffectImpl {

    protected ContinuousEffect effect;
    protected ContinuousEffect otherwiseEffect;
    protected Condition baseCondition;
    protected Condition condition;
    protected boolean initDone = false;

    public ConditionalContinuousEffect(ContinuousEffect effect, Condition condition, String text) {
        this(effect, null, condition, text);
    }

    /**
     * Only use this if both effects have the same layers (TODO: add tests to search it?)
     *
     * @param effect
     * @param otherwiseEffect
     * @param condition
     * @param text
     */
    public ConditionalContinuousEffect(ContinuousEffect effect, ContinuousEffect otherwiseEffect, Condition condition, String text) {
        super(effect.getDuration(), effect.getLayer(), effect.getSublayer(), effect.getOutcome());
        this.effect = effect;
        this.otherwiseEffect = otherwiseEffect;
        this.baseCondition = condition;
        this.staticText = text;

        // checks for compatibility
        EffectType needType = EffectType.CONTINUOUS;
        if (effect.getEffectType() != needType) {
            throw new IllegalArgumentException("ConditionalContinuousEffect supports only " + needType + " but found " + effect.getEffectType().toString());
        }
        if (otherwiseEffect != null && otherwiseEffect.getEffectType() != needType) {
            throw new IllegalArgumentException("ConditionalContinuousEffect supports only " + needType.toString() + " but found " + effect.getEffectType().toString());
        }
        if (otherwiseEffect != null && effect.getEffectType() != otherwiseEffect.getEffectType()) {
            throw new IllegalArgumentException("ConditionalContinuousEffect must be same but found " + effect.getEffectType().toString() + " and " + otherwiseEffect.getEffectType().toString());
        }
    }

    protected ConditionalContinuousEffect(final ConditionalContinuousEffect effect) {
        super(effect);
        this.effect = effect.effect.copy();
        if (effect.otherwiseEffect != null) {
            this.otherwiseEffect = effect.otherwiseEffect.copy();
        }
        this.condition = effect.condition;
        this.baseCondition = effect.baseCondition;
        this.initDone = effect.initDone;
    }

    @Override
    public boolean isDiscarded() {
        return this.discarded || effect.isDiscarded() || (otherwiseEffect != null && otherwiseEffect.isDiscarded());
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        if (baseCondition instanceof LockedInCondition) {
            condition = new FixedCondition(((LockedInCondition) baseCondition).getBaseCondition().apply(game, source));
        } else {
            condition = baseCondition;
        }
        effect.setTargetPointer(this.getTargetPointer().copy());
        effect.init(source, game);
        if (otherwiseEffect != null) {
            otherwiseEffect.setTargetPointer(this.getTargetPointer().copy());
            otherwiseEffect.init(source, game);
        }
        initDone = true;
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        boolean conditionState = condition.apply(game, source);
        if (conditionState) {
            effect.applyToObjects(layer, sublayer, source, game, affectedObjects);
        } else if (otherwiseEffect != null) {
            otherwiseEffect.applyToObjects(layer, sublayer, source, game, affectedObjects);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        if (!initDone) { // if simpleStaticAbility, init won't be called
            init(source, game);
        }
        boolean conditionState = condition.apply(game, source);
        if (conditionState) {
            return effect.queryAffectedObjects(layer, source, game, affectedObjects);
        } else if (otherwiseEffect != null) {
            return otherwiseEffect.queryAffectedObjects(layer, source, game, affectedObjects);
        }
        if (!conditionState && effect.getDuration() == Duration.OneUse) {
            used = true;
        }
        switch (effect.getDuration()) {
            case OneUse:
                used = true;
                break;
            case Custom:
            case WhileControlled:
                this.discard();
        }
        return false;
    }

    @Override
    public boolean apply(Layer layer, SubLayer sublayer, Ability source, Game game) {
        if (!initDone) { // if simpleStaticAbility, init won't be called
            init(source, game);
        }
        boolean conditionState = condition.apply(game, source);
        if (conditionState) {
            return effect.apply(layer, sublayer, source, game);
        } else if (otherwiseEffect != null) {
            return otherwiseEffect.apply(layer, sublayer, source, game);
        }
        if (!conditionState && effect.getDuration() == Duration.OneUse) {
            used = true;
        }
        switch (effect.getDuration()) {
            case OneUse:
                used = true;
                break;
            case Custom:
            case WhileControlled:
                this.discard();
        }
        return false;
    }

    @Override
    public boolean apply(Game game, Ability source) {
        if (condition == null && baseCondition != null) {
            condition = baseCondition;
        }
        boolean conditionState = condition != null && condition.apply(game, source);
        if (conditionState) {
            effect.setTargetPointer(this.getTargetPointer().copy());
            return effect.apply(game, source);
        } else if (otherwiseEffect != null) {
            otherwiseEffect.setTargetPointer(this.getTargetPointer().copy());
            return otherwiseEffect.apply(game, source);
        }
        switch (effect.getDuration()) {
            case OneUse:
                used = true;
                break;
            case Custom:
            case WhileControlled:
                this.discard();
        }
        return false;
    }

    @Override
    public String getText(Mode mode) {
        if ((staticText == null || staticText.isEmpty()) && this.effect != null) { // usefull for conditional night/day card abilities
            return effect.getText(mode);
        }
        return staticText;
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return effect.hasLayer(layer);
    }

    @Override
    public boolean hasSubLayer(SubLayer sublayer) {
        return effect.hasSubLayer(sublayer);
    }

    @Override
    public int calculateResult(Game game, Ability source, List<MageItem> affectedObjects) {
        return effect.calculateResult(game, source, affectedObjects);
    }

    @Override
    public ConditionalContinuousEffect copy() {
        return new ConditionalContinuousEffect(this);
    }


    /**
     * Return all effects list, for tests only
     *
     * @return
     */
    public List<ContinuousEffect> getAllEffects() {
        List<ContinuousEffect> res = new ArrayList<>();
        if (this.effect != null) res.add(this.effect);
        if (this.otherwiseEffect != null) res.add(this.otherwiseEffect);
        return res;
    }

    @Override
    public Condition getCondition() {
        return condition == null ? baseCondition : condition;
    }
}
