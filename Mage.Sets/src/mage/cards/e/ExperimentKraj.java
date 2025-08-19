
package mage.cards.e;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCreaturePermanent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author LevelX2
 */
public final class ExperimentKraj extends CardImpl {

    public ExperimentKraj(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}{G}{U}{U}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.OOZE);
        this.subtype.add(SubType.MUTANT);

        this.power = new MageInt(4);
        this.toughness = new MageInt(6);

        // Experiment Kraj has all activated abilities of each other creature with a +1/+1 counter on it.
        this.addAbility(new SimpleStaticAbility(new ExperimentKrajEffect()));

        // {tap}: Put a +1/+1 counter on target creature.
        Ability ability = new SimpleActivatedAbility(new AddCountersTargetEffect(CounterType.P1P1.createInstance()), new TapSourceCost());
        ability.addTarget(new TargetCreaturePermanent());
        this.addAbility(ability);
    }

    private ExperimentKraj(final ExperimentKraj card) {
        super(card);
    }

    @Override
    public ExperimentKraj copy() {
        return new ExperimentKraj(this);
    }
}

class ExperimentKrajEffect extends ContinuousEffectImpl {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent("each other creature with a +1/+1 counter on it");

    static {
        filter.add(CounterType.P1P1.getPredicate());
        filter.add(AnotherPredicate.instance);
    }

    public ExperimentKrajEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "{this} has all activated abilities of each other creature with a +1/+1 counter on it";
    }

    private ExperimentKrajEffect(final ExperimentKrajEffect effect) {
        super(effect);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        List<Ability> abilities = new ArrayList<>();
        for (Permanent creature : game.getBattlefield().getActivePermanents(filter, source.getControllerId(), source, game)) {
            for (Ability ability : creature.getAbilities()) {
                if (ability.isActivatedAbility()) {
                    abilities.add(ability);
                }
            }
        }
        for (MageItem object : affectedObjects) {
            for (Ability ability : abilities) {
                ((Permanent) object).addAbility(ability, source.getSourceId(), game, true);
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = game.getPermanent(source.getSourceId());
        if (permanent != null) {
            affectedObjects.add(permanent);
            return true;
        }
        return false;
    }

    @Override
    public ExperimentKrajEffect copy() {
        return new ExperimentKrajEffect(this);
    }

}
