
package mage.cards.t;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.NamePredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author jeffwadsworth
 */
public final class TimberpackWolf extends CardImpl {

    public TimberpackWolf(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{1}{G}");
        this.subtype.add(SubType.WOLF);

        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Timberpack Wolf gets +1/+1 for each other creature you control named Timberpack Wolf.
        this.addAbility(new SimpleStaticAbility(new TimberpackWolfEffect()));
    }

    private TimberpackWolf(final TimberpackWolf card) {
        super(card);
    }

    @Override
    public TimberpackWolf copy() {
        return new TimberpackWolf(this);
    }
}

class TimberpackWolfEffect extends ContinuousEffectImpl {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent("other creature you control named Timberpack Wolf");

    static {
        filter.add(new NamePredicate("Timberpack Wolf"));
        filter.add(TargetController.YOU.getControllerPredicate());
    }

    TimberpackWolfEffect() {
        super(Duration.WhileOnBattlefield, Layer.PTChangingEffects_7, SubLayer.ModifyPT_7c, Outcome.BoostCreature);
        staticText = "{this} gets +1/+1 for each other creature you control named Timberpack Wolf";
    }

    private TimberpackWolfEffect(final TimberpackWolfEffect effect) {
        super(effect);
    }

    @Override
    public TimberpackWolfEffect copy() {
        return new TimberpackWolfEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        int count = game.getBattlefield().count(filter, source.getControllerId(), source, game);
        for (MageItem object : affectedObjects) {
            Permanent creature = (Permanent) object;
            creature.addPower(count);
            creature.addToughness(count);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = game.getPermanent(source.getSourceId());
        if (permanent != null) {
            affectedObjects.add(permanent);
            return true;
        } else {
            return false;
        }
    }
}