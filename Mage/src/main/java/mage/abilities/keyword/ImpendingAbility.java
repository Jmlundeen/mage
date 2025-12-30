package mage.abilities.keyword;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.CompoundCondition;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.SourceHasCounterCondition;
import mage.abilities.costs.AlternativeSourceCostsImpl;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.decorator.ConditionalOneShotEffect;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.AddContinuousEffectToGame;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.effects.common.counter.RemoveCounterSourceEffect;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.constants.*;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.util.CardUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * "Impending N–[cost]" is a keyword that represents multiple abilities.
 * The official rules are as follows:
 * (a) You may choose to pay [cost] rather than pay this spell's mana cost.
 * (b) If you chose to pay this spell's impending cost, it enters the battlefield with N time counters on it.
 * (c) As long as this permanent has a time counter on it, if it was cast for its impending cost, it's not a creature.
 * (d) At the beginning of your end step, if this permanent was cast for its impending cost, remove a time counter from it. Then if it has no time counters on it, it loses impending.
 *
 * @author TheElk801
 */
public class ImpendingAbility extends AlternativeSourceCostsImpl {

    private static final String IMPENDING_KEYWORD = "Impending";
    private static final String IMPENDING_REMINDER = "If you cast this spell for its impending cost, " +
            "it enters with %s time counters and isn't a creature until the last is removed. " +
            "At the beginning of your end step, remove a time counter from it.";
    private static final Condition counterCondition = new SourceHasCounterCondition(CounterType.TIME, ComparisonType.EQUAL_TO, 0);

    public ImpendingAbility(int amount, String manaString) {
        super(IMPENDING_KEYWORD + ' ' + amount, String.format(IMPENDING_REMINDER, CardUtil.numberToText(amount)), new ManaCostsImpl<>(manaString), IMPENDING_KEYWORD);
        this.setRuleAtTheTop(true);
        // enters with time counters
        this.addSubAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.TIME.createInstance(amount)),
                ImpendingCondition.instance)
        ).setRuleVisible(false));
        // is not a creature while it has time counters
        this.addSubAbility(new SimpleStaticAbility(new ConditionalContinuousEffect(
                new ContinuousEffectBuilder(Duration.WhileOnBattlefield, Outcome.Detriment, ContinuousAffected.SOURCE)
                        .withRemovedCardTypes(CardType.CREATURE),
                new CompoundCondition(
                        ImpendingCondition.instance,
                        new SourceHasCounterCondition(CounterType.TIME, ComparisonType.MORE_THAN, 0)
                ),
                "As long as this permanent has a time counter on it, if it was cast for its impending cost, it's not a creature."
        )).setRuleVisible(false));
        // remove time counter at end step and lose impending if none remain
        Ability ability = new BeginningOfEndStepTriggeredAbility(
                TargetController.YOU, new RemoveCounterSourceEffect(CounterType.TIME.createInstance()),
                false, ImpendingCondition.instance
        );
        ability.addEffect(new ConditionalOneShotEffect(
                new AddContinuousEffectToGame(new ImpendingAbilityRemoveEffect()),
                counterCondition, "Then if it has no time counters on it, it loses impending"
        ));
        this.addSubAbility(ability.setRuleVisible(false));
    }

    private ImpendingAbility(final ImpendingAbility ability) {
        super(ability);
    }

    @Override
    public ImpendingAbility copy() {
        return new ImpendingAbility(this);
    }

    public static String getActivationKey() {
        return getActivationKey(IMPENDING_KEYWORD);
    }
}

enum ImpendingCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        return CardUtil.checkSourceCostsTagExists(game, source, ImpendingAbility.getActivationKey());
    }
}

class ImpendingAbilityRemoveEffect extends ContinuousEffectImpl {

    ImpendingAbilityRemoveEffect() {
        super(Duration.Custom, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.LoseAbility);
    }

    private ImpendingAbilityRemoveEffect(final ImpendingAbilityRemoveEffect effect) {
        super(effect);
    }

    @Override
    public ImpendingAbilityRemoveEffect copy() {
        return new ImpendingAbilityRemoveEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            permanent.removeAbilities(
                    permanent
                            .getAbilities(game)
                            .stream()
                            .filter(ImpendingAbility.class::isInstance)
                            .collect(Collectors.toList()),
                    source.getSourceId(), game
            );
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent == null) {
            discard();
            return false;
        }
        affectedObjects.add(permanent);
        return true;
    }
}
