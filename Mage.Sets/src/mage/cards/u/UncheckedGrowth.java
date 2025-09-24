
package mage.cards.u;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.continuous.BoostTargetEffect;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCreaturePermanent;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class UncheckedGrowth extends CardImpl {

    public UncheckedGrowth(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.INSTANT},"{2}{G}");
        this.subtype.add(SubType.ARCANE);

        
        // Target creature gets +4/+4 until end of turn. 
        this.getSpellAbility().addEffect(new BoostTargetEffect(4, 4, Duration.EndOfTurn));
        // If it's a Spirit, it gains trample until end of turn.
        this.getSpellAbility().addEffect(new UncheckedGrowthTrampleEffect());
        this.getSpellAbility().addTarget(new TargetCreaturePermanent());
    }

    private UncheckedGrowth(final UncheckedGrowth card) {
        super(card);
    }

    @Override
    public UncheckedGrowth copy() {
        return new UncheckedGrowth(this);
    }
    

}
class UncheckedGrowthTrampleEffect extends ContinuousEffectImpl {

    UncheckedGrowthTrampleEffect() {
        super(Duration.EndOfTurn, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "If it's a Spirit, it gains trample until end of turn";
    }

    private UncheckedGrowthTrampleEffect(final UncheckedGrowthTrampleEffect effect) {
        super(effect);
    }

    @Override
    public UncheckedGrowthTrampleEffect copy() {
        return new UncheckedGrowthTrampleEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).addAbility(TrampleAbility.getInstance(), source.getSourceId(), game);
        }
    }

    @Override
    public boolean queryAffectedObjects(Ability source, Game game, List<MageItem> affectedObjects) {
        for (UUID permanentId : getTargetPointer().getTargets(game, source)) {
            Permanent permanent = game.getPermanent(permanentId);
            if (permanent != null && permanent.hasSubtype(SubType.SPIRIT, game)) {
                affectedObjects.add(permanent);
            }
        }
        return !affectedObjects.isEmpty();
    }
}
