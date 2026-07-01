package mage.abilities.effects.common.countered;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.FilterTyped;
import mage.game.Game;
import mage.game.stack.Spell;
import mage.game.stack.StackAbility;
import mage.game.stack.StackObject;
import mage.players.Player;
import mage.target.targetpointer.FixedTarget;
import mage.target.targetpointer.RememberedTargets;
import mage.util.ObjectQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * One-shot effect that counters spells (and optionally {@link StackAbility
 * stack abilities}) on the stack. Supports optional filtering, additional
 * effects triggered on successful counter, and remembering various attributes
 * of countered objects for use by subsequent linked effects.
 * @author jmlundeen
 */
public class CounterEffect extends OneShotEffect {

    /** Effects to apply after a stack object is successfully countered. */
    List<Effect> additionalEffects;
    /**
     * If true, the cumulative mana value of all countered objects is stored
     * on the source effects as "{sourceId}_counteredManaValue".
     */
    boolean rememberManaValue = false;
    /**
     * If true, countered stack objects are added to the effect's
     * {@link RememberedTargets} for use by subsequent linked effects.
     */
    boolean rememberSpell = false;
    /**
     * If true, the controller of each countered stack object is added to the
     * effect's {@link RememberedTargets}.
     */
    boolean rememberController = false;
    /**
     * Optional filter that stack objects must match. When set, the effect
     * also queries objects on the stack matching this filter.
     */
    FilterTyped filter;

    /**
     * Creates a CounterEffect with detrimental outcome and no filter.
     */
    public CounterEffect() {
        this(Outcome.Detriment, null);
    }

    /**
     * Creates a CounterEffect that only counters stack objects matching
     * the given filter.
     */
    public CounterEffect(FilterTyped filter) {
        this(Outcome.Detriment, filter);
    }

    /**
     * Creates a fully-configured CounterEffect.
     *
     * @param outcome the outcome for AI evaluation
     * @param filter  optional filter restricting what can be countered
     */
    public CounterEffect(Outcome outcome, FilterTyped filter) {
        super(outcome);
        this.filter = filter;
    }

    protected CounterEffect(final CounterEffect effect) {
        super(effect);
        this.additionalEffects = effect.additionalEffects == null ? null : new ArrayList<>(effect.additionalEffects);
        this.rememberManaValue = effect.rememberManaValue;
        this.rememberSpell = effect.rememberSpell;
        this.rememberController = effect.rememberController;
        this.filter = effect.filter == null ? null : effect.filter.copy();
    }

    @Override
    public void applyToObjects(Ability source, Game game, List<MageItem> affectedObjects) {
        int counteredManaValue = 0;
        List<MageItem> rememberedObjects = new ArrayList<>();
        for (MageItem item : affectedObjects) {
            StackObject stackObject = (StackObject) item;
            boolean countered = game.getStack().counter(item.getId(), source, game);
            if (rememberManaValue) {
                counteredManaValue += stackObject.getManaValue();
            }
            if (rememberSpell) {
                rememberedObjects.add(stackObject);
            }
            if (rememberController) {
                Player player = game.getPlayer(stackObject.getControllerId());
                if (player != null) {
                    rememberedObjects.add(player);
                }
            }
            if (countered && additionalEffects != null) {
                FixedTarget target = new FixedTarget(stackObject.getId(), game.getState().getZoneChangeCounter(stackObject.getId()));
                for (Effect effect : additionalEffects) {
                    effect.setTargetPointer(target);
                    if (effect instanceof OneShotEffect) {
                        effect.apply(game, source);
                    } else {
                        game.addEffect((ContinuousEffect) effect, source);
                    }
                }
            }
        }
        if (rememberManaValue) {
            source.getEffects().setValue(source.getSourceId() + "_counteredManaValue", counteredManaValue);
        }
        if (!rememberedObjects.isEmpty()) {
            RememberedTargets rememberedTargets = new RememberedTargets(rememberedObjects, game);
            source.getEffects().setTargetPointer(rememberedTargets);
        }
    }

    @Override
    public boolean queryAffectedObjects(Ability source, Game game, List<MageItem> affectedObjects) {
        for (UUID targetId : getTargetPointer().getTargets(game, source)) {
            Spell spell = game.getStack().getSpell(targetId);
            if (spell != null) {
                affectedObjects.add(spell);
                continue;
            }
            StackAbility stackAbility = (StackAbility) game.getStack()
                    .getStackObject(targetId);
            if (stackAbility != null) {
                affectedObjects.add(stackAbility);
            }
        }
        if (filter != null) {
            affectedObjects.removeIf(item -> !filter.match(item, source.getControllerId(), source, game));
            affectedObjects.addAll(ObjectQuery.query(game, game.getPlayer(source.getControllerId()), source, filter, Set.of(Zone.STACK)));
        }
        return !affectedObjects.isEmpty();
    }

    @Override
    public boolean apply(Game game, Ability source) {
        List<MageItem> affectedObjects = new ArrayList<>(source.getAffectedObjects());
        if (queryAffectedObjects(source, game, affectedObjects)) {
            applyToObjects(source, game, affectedObjects);
            return true;
        }
        return false;
    }

    @Override
    public CounterEffect copy() {
        return new CounterEffect(this);
    }

    @Override
    public CounterEffect setText(String staticText) {
        return (CounterEffect) super.setText(staticText);
    }

    /**
     * Sets a list of effects to apply after each successful counter.
     * The target pointer of each effect is fixed to the countered object.
     */
    public CounterEffect setAdditionalEffects(List<Effect> additionalEffects) {
        this.additionalEffects = additionalEffects;
        return this;
    }

    /**
     * Adds a single effect to the list of effects applied after each
     * successful counter.
     */
    public CounterEffect addAdditionalEffect(Effect effect) {
        if (this.additionalEffects == null) {
            this.additionalEffects = new ArrayList<>();
        }
        this.additionalEffects.add(effect);
        return this;
    }

    /**
     * If true, the cumulative mana value of all countered objects is stored
     * on the source effects under the key "{sourceId}_counteredManaValue".
     */
    public CounterEffect setRememberManaValue(boolean rememberManaValue) {
        this.rememberManaValue = rememberManaValue;
        return this;
    }

    /**
     * If true, the controller of each countered stack object is added to the
     * effect's {@link RememberedTargets} for use by subsequent linked effects.
     */
    public CounterEffect setRememberController(boolean rememberController) {
        this.rememberController = rememberController;
        return this;
    }

    /**
     * If true, each countered stack object is added to the effect's
     * {@link RememberedTargets} for use by subsequent linked effects.
     */
    public CounterEffect setRememberSpell(boolean rememberSpell) {
        this.rememberSpell = rememberSpell;
        return this;
    }
}
