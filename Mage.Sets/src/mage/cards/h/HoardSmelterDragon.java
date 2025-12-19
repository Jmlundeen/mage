

package mage.cards.h;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.DestroyTargetEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author Loki
 */
public final class HoardSmelterDragon extends CardImpl {
    private static final FilterPermanent filter = new FilterPermanent("artifact");

    static {
        filter.add(CardType.ARTIFACT.getPredicate());
    }

    public HoardSmelterDragon (UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{4}{R}{R}");
        this.subtype.add(SubType.DRAGON);

        this.power = new MageInt(5);
        this.toughness = new MageInt(5);
        this.addAbility(FlyingAbility.getInstance());
        Ability ability = new SimpleActivatedAbility(new DestroyTargetEffect(), new ManaCostsImpl<>("{3}{R}"));
        ability.addTarget(new TargetPermanent(filter));
        ability.addEffect(new HoardSmelterEffect());
        this.addAbility(ability);
    }

    private HoardSmelterDragon(final HoardSmelterDragon card) {
        super(card);
    }

    @Override
    public HoardSmelterDragon copy() {
        return new HoardSmelterDragon(this);
    }
}

class HoardSmelterEffect extends ContinuousEffectImpl {
    private int costValue = 0;

    HoardSmelterEffect() {
        super(Duration.EndOfTurn, Layer.PTChangingEffects_7, SubLayer.ModifyPT_7c, Outcome.BoostCreature);
        staticText = "{this} gets +X/+0 until end of turn, where X is that artifact's mana value";
    }

    private HoardSmelterEffect(final HoardSmelterEffect effect) {
        super(effect);
        this.costValue = effect.costValue;
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).addPower(costValue);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent target = game.getPermanent(source.getSourceId());
        if (target != null) {
            affectedObjects.add(target);
        }
        return !affectedObjects.isEmpty();
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        Card targeted = game.getCard(source.getFirstTarget());
        if (targeted != null) {
            costValue = targeted.getManaValue();
        }
    }

    @Override
    public HoardSmelterEffect copy() {
        return new HoardSmelterEffect(this);
    }

}
