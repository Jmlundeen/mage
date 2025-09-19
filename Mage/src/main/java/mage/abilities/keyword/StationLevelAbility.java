package mage.abilities.keyword;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.StaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.*;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.util.CardUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author TheElk801
 */
public class StationLevelAbility extends StaticAbility {

    private final int level;

    public StationLevelAbility(int level) {
        super(Zone.BATTLEFIELD, null);
        this.level = level;
    }

    private StationLevelAbility(final StationLevelAbility ability) {
        super(ability);
        this.level = ability.level;
    }

    @Override
    public StationLevelAbility copy() {
        return new StationLevelAbility(this);
    }

    public StationLevelAbility withLevelAbility(Ability ability) {
        this.addEffect(new StationLevelAbilityEffect(ability, level));
        return this;
    }

    public StationLevelAbility withPT(int power, int toughness) {
        this.addEffect(new StationLevelCreatureEffect(power, toughness, level));
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
        return this.getEffects().stream().anyMatch(StationLevelCreatureEffect.class::isInstance);
    }
}

class StationLevelAbilityEffect extends ContinuousEffectImpl {

    private final Ability ability;
    private final int level;

    StationLevelAbilityEffect(Ability ability, int level) {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.ability = ability;
        this.level = level;
        this.ability.setRuleVisible(false);
        this.staticText = ability.getRule();
    }

    private StationLevelAbilityEffect(final StationLevelAbilityEffect effect) {
        super(effect);
        this.ability = effect.ability;
        this.level = effect.level;
    }

    @Override
    public StationLevelAbilityEffect copy() {
        return new StationLevelAbilityEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent == null || permanent.getCounters(game).getCount(CounterType.CHARGE) < level) {
            return false;
        }
        permanent.addAbility(ability, source.getSourceId(), game);
        return true;
    }
}

class StationLevelCreatureEffect extends ContinuousEffectImpl {

    private final int power;
    private final int toughness;
    private final int level;

    StationLevelCreatureEffect(int power, int toughness, int level) {
        super(Duration.WhileOnBattlefield, Outcome.BecomeCreature);
        this.power = power;
        this.toughness = toughness;
        this.level = level;
        staticText = power + "/" + toughness;
    }

    private StationLevelCreatureEffect(final StationLevelCreatureEffect effect) {
        super(effect);
        this.power = effect.power;
        this.toughness = effect.toughness;
        this.level = effect.level;
    }

    @Override
    public StationLevelCreatureEffect copy() {
        return new StationLevelCreatureEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            switch (layer) {
                case TypeChangingEffects_4:
                    if (sublayer == SubLayer.NA) {
                        if (!permanent.isCreature(game)) {
                            permanent.addCardType(game, CardType.CREATURE);
                        }
                    }
                    break;
                case PTChangingEffects_7:
                    if (sublayer == SubLayer.SetPT_7b) {
                        permanent.getPower().setModifiedBaseValue(power);
                        permanent.getToughness().setModifiedBaseValue(toughness);
                    }
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent != null) {
            affectedObjects.add(permanent);
        }
        return !affectedObjects.isEmpty();
    }

    @Override
    public boolean hasLayer(Layer layer) {
        switch (layer) {
            case TypeChangingEffects_4:
            case PTChangingEffects_7:
                return true;
            default:
                return false;
        }
    }
}
