
package mage.cards.l;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.LeylineAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterNonlandPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class LeylineOfSingularity extends CardImpl {

    public LeylineOfSingularity(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{U}{U}");

        // If Leyline of Singularity is in your opening hand, you may begin the game with it on the battlefield.
        this.addAbility(LeylineAbility.getInstance());

        // All nonland permanents are legendary.
        this.addAbility(new SimpleStaticAbility(new SetSupertypeAllEffect()));
    }

    private LeylineOfSingularity(final LeylineOfSingularity card) {
        super(card);
    }

    @Override
    public LeylineOfSingularity copy() {
        return new LeylineOfSingularity(this);
    }
}

class SetSupertypeAllEffect extends ContinuousEffectImpl {

    private static final FilterNonlandPermanent filter = new FilterNonlandPermanent();

    public SetSupertypeAllEffect() {
        super(Duration.WhileOnBattlefield, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Detriment);
        this.staticText = "All nonland permanents are legendary";
        this.dependendToTypes.add(DependencyType.BecomeNonbasicLand);
    }

    private SetSupertypeAllEffect(final SetSupertypeAllEffect effect) {
        super(effect);
    }

    @Override
    public SetSupertypeAllEffect copy() {
        return new SetSupertypeAllEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).addSuperType(game, SuperType.LEGENDARY);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        affectedObjects.addAll(game.getBattlefield().getActivePermanents(filter, source.getControllerId(), source, game));
        return !affectedObjects.isEmpty();
    }
}
