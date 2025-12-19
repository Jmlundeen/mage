

package mage.cards.r;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.InfoEffect;
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
 * @author BetaSteward_at_googlemail.com
 */
public final class RelentlessRats extends CardImpl {

    public RelentlessRats(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{1}{B}{B}");
        this.subtype.add(SubType.RAT);

        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Relentless Rats gets +1/+1 for each other creature on the battlefield named Relentless Rats.
        this.addAbility(new SimpleStaticAbility(new RelentlessRatsEffect()));

        // A deck can have any number of cards named Relentless Rats.
        this.getSpellAbility().addEffect(new InfoEffect("A deck can have any number of cards named Relentless Rats."));
    }

    private RelentlessRats(final RelentlessRats card) {
        super(card);
    }

    @Override
    public RelentlessRats copy() {
        return new RelentlessRats(this);
    }
}

class RelentlessRatsEffect extends ContinuousEffectImpl {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent();

    static {
        filter.add(new NamePredicate("Relentless Rats"));
    }

    RelentlessRatsEffect() {
        super(Duration.WhileOnBattlefield, Layer.PTChangingEffects_7, SubLayer.ModifyPT_7c, Outcome.BoostCreature);
        staticText = "{this} gets +1/+1 for each other creature on the battlefield named Relentless Rats";
    }

    private RelentlessRatsEffect(final RelentlessRatsEffect effect) {
        super(effect);
    }

    @Override
    public RelentlessRatsEffect copy() {
        return new RelentlessRatsEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        int count = game.getBattlefield().count(filter, source.getControllerId(), source, game) - 1;
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            permanent.addPower(count);
            permanent.addToughness(count);
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
}
