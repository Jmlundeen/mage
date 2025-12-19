
package mage.cards.k;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.filter.predicate.mageobject.MulticoloredPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author jeffwadsworth
 */
public final class KnightOfNewAlara extends CardImpl {

    public KnightOfNewAlara(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{2}{G}{W}");
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.KNIGHT);



        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Each other multicolored creature you control gets +1/+1 for each of its colors.
        this.addAbility(new SimpleStaticAbility(new KnightOfNewAlaraEffect()));

    }

    private KnightOfNewAlara(final KnightOfNewAlara card) {
        super(card);
    }

    @Override
    public KnightOfNewAlara copy() {
        return new KnightOfNewAlara(this);
    }
}

class KnightOfNewAlaraEffect extends ContinuousEffectImpl {

    private static final FilterControlledCreaturePermanent filter = new FilterControlledCreaturePermanent();
    static {
        filter.add(AnotherPredicate.instance);
        filter.add(MulticoloredPredicate.instance);
    }

    public KnightOfNewAlaraEffect() {
        super(Duration.WhileOnBattlefield, Layer.PTChangingEffects_7, SubLayer.ModifyPT_7c, Outcome.BoostCreature);
        staticText = "Each other multicolored creature you control gets +1/+1 for each of its colors";
    }

    private KnightOfNewAlaraEffect(final KnightOfNewAlaraEffect effect) {
        super(effect);
    }

    @Override
    public KnightOfNewAlaraEffect copy() {
        return new KnightOfNewAlaraEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent creature = (Permanent) object;
            int colors = creature.getColor(game).getColorCount();
            creature.addPower(colors);
            creature.addToughness(colors);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        affectedObjects.addAll(game.getBattlefield().getAllActivePermanents(filter, source.getControllerId(), game));
        return !affectedObjects.isEmpty();
    }
}
