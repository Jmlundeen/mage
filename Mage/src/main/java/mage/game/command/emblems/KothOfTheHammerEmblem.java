package mage.game.command.emblems;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.DamageTargetEffect;
import mage.constants.*;
import mage.filter.common.FilterLandPermanent;
import mage.game.Game;
import mage.game.command.Emblem;
import mage.game.permanent.Permanent;
import mage.target.common.TargetAnyTarget;

import java.util.List;

/**
 * @author spjspj
 */
public final class KothOfTheHammerEmblem extends Emblem {
    // "Mountains you control have '{T}: This land deals 1 damage to any target.'"

    public KothOfTheHammerEmblem() {
        super("Emblem Koth");
        this.getAbilities().add(new SimpleStaticAbility(Zone.COMMAND, new KothOfTheHammerThirdEffect()));
    }

    private KothOfTheHammerEmblem(final KothOfTheHammerEmblem card) {
        super(card);
    }

    @Override
    public KothOfTheHammerEmblem copy() {
        return new KothOfTheHammerEmblem(this);
    }
}

class KothOfTheHammerThirdEffect extends ContinuousEffectImpl {

    static final FilterLandPermanent mountains = new FilterLandPermanent("Mountain you control");

    static {
        mountains.add(SubType.MOUNTAIN.getPredicate());
        mountains.add(TargetController.YOU.getControllerPredicate());
    }

    public KothOfTheHammerThirdEffect() {
        super(Duration.EndOfGame, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "Mountains you control have '{T}: This land deals 1 damage to any target.'";
    }

    protected KothOfTheHammerThirdEffect(final KothOfTheHammerThirdEffect effect) {
        super(effect);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            Ability ability = new SimpleActivatedAbility(new DamageTargetEffect(1), new TapSourceCost());
            ability.addTarget(new TargetAnyTarget());
            permanent.addAbility(ability, source.getSourceId(), game);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        affectedObjects.addAll(game.getBattlefield().getActivePermanents(mountains, source.getControllerId(), source, game));
        return !affectedObjects.isEmpty();
    }

    @Override
    public KothOfTheHammerThirdEffect copy() {
        return new KothOfTheHammerThirdEffect(this);
    }
}
