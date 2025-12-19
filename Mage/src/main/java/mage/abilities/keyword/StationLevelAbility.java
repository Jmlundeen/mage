package mage.abilities.keyword;

import mage.abilities.Ability;
import mage.abilities.StaticAbility;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.SourceHasCounterCondition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.constants.*;
import mage.counters.CounterType;
import mage.util.CardUtil;

import java.util.stream.Collectors;

/**
 * @author TheElk801
 */
public class StationLevelAbility extends StaticAbility {

    private final int level;
    private final Condition counterCondition;
    private boolean hasPT = false;

    public StationLevelAbility(int level) {
        super(Zone.BATTLEFIELD, null);
        this.level = level;
        counterCondition = new SourceHasCounterCondition(CounterType.CHARGE, ComparisonType.OR_GREATER, level);
    }

    private StationLevelAbility(final StationLevelAbility ability) {
        super(ability);
        this.level = ability.level;
        this.counterCondition = ability.counterCondition;
        this.hasPT = ability.hasPT;
    }

    @Override
    public StationLevelAbility copy() {
        return new StationLevelAbility(this);
    }

    public StationLevelAbility withLevelAbility(Ability ability) {
        this.addEffect(new ConditionalContinuousEffect(
                new ContinuousEffectBuilder(Duration.WhileOnBattlefield, Outcome.AddAbility, ContinuousAffected.SOURCE)
                        .withGainedAbilities(ability),
                counterCondition, ability.getRule()
        ));
        return this;
    }

    public StationLevelAbility withPT(int power, int toughness) {
        this.addEffect(new ConditionalContinuousEffect(
                new ContinuousEffectBuilder(Duration.WhileOnBattlefield, Outcome.BecomeCreature, ContinuousAffected.SOURCE)
                        .withSetPower(power)
                        .withSetToughness(toughness)
                        .withAddedCardTypes(false, CardType.CREATURE),
                counterCondition, power + "/" + toughness
        ));
        this.hasPT = true;
        return this;
    }

    @Override
    public String getRule() {
        return "STATION " + level + "+<br>" + this
                .getEffects()
                .stream()
                .map(effect -> effect.getText(this.getModes().getMode()))
                .map(CardUtil::getTextWithFirstCharUpperCase)
                .collect(Collectors.joining("<br>"));
    }

    public boolean hasPT() {
        return hasPT;
    }
}
