
package mage.cards.t;

import mage.MageItem;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author Plopman
 */
public final class ThranLens extends CardImpl {

    public ThranLens(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ARTIFACT},"{2}");
        // All permanents are colorless.
        this.addAbility(new SimpleStaticAbility(new ThranLensEffect()));
    }

    private ThranLens(final ThranLens card) {
        super(card);
    }

    @Override
    public ThranLens copy() {
        return new ThranLens(this);
    }
    
   
}
 class ThranLensEffect extends ContinuousEffectImpl {

    ThranLensEffect()
    {
        super(Duration.WhileOnBattlefield, Layer.ColorChangingEffects_5, SubLayer.NA, Outcome.Benefit);
        staticText = "All permanents are colorless";
    }

     @Override
     public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
         for (MageItem object : affectedObjects) {
             ((Permanent) object).getColor(game).setColor(ObjectColor.COLORLESS);
         }
     }

     @Override
     public boolean queryAffectedObjects(Ability source, Game game, List<MageItem> affectedObjects) {
         affectedObjects.addAll(game.getBattlefield().getActivePermanents(source.getSourceId(), game));
         return !affectedObjects.isEmpty();
     }

    @Override
    public ThranLensEffect copy() {
        return new ThranLensEffect(this);
    }

    private ThranLensEffect(ThranLensEffect effect) {
        super(effect);
    }


}
