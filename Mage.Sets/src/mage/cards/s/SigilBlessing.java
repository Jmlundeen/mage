
package mage.cards.s;

import mage.MageItem;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetControlledCreaturePermanent;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Plopman
 */
public final class SigilBlessing extends CardImpl {

    public SigilBlessing(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{G}{W}");

        // Until end of turn, target creature you control gets +3/+3 and other creatures you control get +1/+1.
        this.getSpellAbility().addTarget(new TargetControlledCreaturePermanent());
        this.getSpellAbility().addEffect(new SigilBlessingBoostControlledEffect());
    }

    private SigilBlessing(final SigilBlessing card) {
        super(card);
    }

    @Override
    public SigilBlessing copy() {
        return new SigilBlessing(this);
    }
}

class SigilBlessingBoostControlledEffect extends ContinuousEffectImpl {

    SigilBlessingBoostControlledEffect() {
        super(Duration.EndOfTurn, Layer.PTChangingEffects_7, SubLayer.ModifyPT_7c, Outcome.BoostCreature);
        staticText = "Until end of turn, target creature you control gets +3/+3 and other creatures you control get +1/+1";
    }

    private SigilBlessingBoostControlledEffect(final SigilBlessingBoostControlledEffect effect) {
        super(effect);
    }

    @Override
    public SigilBlessingBoostControlledEffect copy() {
        return new SigilBlessingBoostControlledEffect(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        for (Permanent perm : game.getBattlefield().getAllActivePermanents(StaticFilters.FILTER_PERMANENT_CREATURE, source.getControllerId(), game)) {
            affectedObjectList.add(new MageObjectReference(perm, game));
        }
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            int boost;
            if (permanent.getId().equals(getTargetPointer().getFirst(game, source))) {
                boost = 3;
            } else {
                boost = 1;
            }
            permanent.addPower(boost);
            permanent.addToughness(boost);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (Iterator<MageObjectReference> it = affectedObjectList.iterator(); it.hasNext();) {
            Permanent permanent = it.next().getPermanent(game);
            if (permanent != null) {
                affectedObjects.add(permanent);
            } else {
                it.remove(); // no longer on the battlefield, remove reference to object
            }
        }
        return !affectedObjects.isEmpty();
    }
}
