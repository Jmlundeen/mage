
package mage.cards.g;

import mage.MageInt;
import mage.MageItem;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author anonymous
 */
public final class GhostflameSliver extends CardImpl {

    public GhostflameSliver(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{B}{R}");
        this.subtype.add(SubType.SLIVER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // All Slivers are colorless.
        this.addAbility(new SimpleStaticAbility(new GhostflameSliverEffect()));
    }

    private GhostflameSliver(final GhostflameSliver card) {
        super(card);
    }

    @Override
    public GhostflameSliver copy() {
        return new GhostflameSliver(this);
    }
}

class GhostflameSliverEffect extends ContinuousEffectImpl {
    
    private static final FilterPermanent filter = new FilterPermanent(SubType.SLIVER, "All Slivers");

    public GhostflameSliverEffect() {
        super(Duration.WhileOnBattlefield, Layer.ColorChangingEffects_5, SubLayer.NA, Outcome.Benefit);
        staticText = "All Slivers are colorless";
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).getColor(game).setColor(ObjectColor.COLORLESS);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        affectedObjects.addAll(game.getBattlefield().getActivePermanents(filter, source.getControllerId(), game));
        return !affectedObjects.isEmpty();
    }

    @Override
    public GhostflameSliverEffect copy() {
        return new GhostflameSliverEffect(this);
    }

    private GhostflameSliverEffect(final GhostflameSliverEffect effect) {
        super(effect);
    }
}
