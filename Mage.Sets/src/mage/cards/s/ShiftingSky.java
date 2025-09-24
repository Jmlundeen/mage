
package mage.cards.s;

import mage.MageItem;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.ChooseColorEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author Luna Skyrise
 */
public final class ShiftingSky extends CardImpl {

    public ShiftingSky(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ENCHANTMENT},"{2}{U}");

        // As Shifting Sky enters the battlefield, choose a color.
        this.addAbility(new AsEntersBattlefieldAbility(new ChooseColorEffect(Outcome.Detriment)));

        // All nonland permanents are the chosen color.        
        this.addAbility(new SimpleStaticAbility(new ShiftingSkyEffect()));
    }

    private ShiftingSky(final ShiftingSky card) {
        super(card);
    }

    @Override
    public ShiftingSky copy() {
        return new ShiftingSky(this);
    }
}

class ShiftingSkyEffect extends ContinuousEffectImpl {
    
    private static final FilterPermanent filter = new FilterPermanent("All nonland permanents");
    
    static {
        filter.add(Predicates.not(CardType.LAND.getPredicate()));
    }
    
    public ShiftingSkyEffect() {
        super(Duration.WhileOnBattlefield, Layer.ColorChangingEffects_5, SubLayer.NA, Outcome.Benefit);
        staticText = "All nonland permanents are the chosen color";
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).getColor(game).setColor((ObjectColor) game.getState().getValue(source.getSourceId() + "_color"));
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player controller = game.getPlayer(source.getControllerId());
        ObjectColor color = (ObjectColor) game.getState().getValue(source.getSourceId() + "_color");
        if (color == null || controller == null) {
            return false;
        }
        affectedObjects.addAll(game.getBattlefield().getActivePermanents(filter, source.getControllerId(), source, game));
        return !affectedObjects.isEmpty();
    }

    @Override
    public ShiftingSkyEffect copy() {
        return new ShiftingSkyEffect(this);
    }

    private ShiftingSkyEffect(ShiftingSkyEffect effect) {
        super(effect);
    }

}
