
package mage.cards.s;

import mage.MageItem;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.ChangelingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.TargetPlayer;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * @author Styxo
 */
public final class ShieldsOfVelisVel extends CardImpl {

    public ShieldsOfVelisVel(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.KINDRED, CardType.INSTANT}, "{W}");
        this.subtype.add(SubType.SHAPESHIFTER);

        // Changeling
        this.addAbility(new ChangelingAbility());

        //Creatures target player controls get +0/+1 and gain all creature types until end of turn.
        this.getSpellAbility().addEffect(new ShieldsOfVelisVelBoostEffect());
        this.getSpellAbility().addTarget(new TargetPlayer());

    }

    private ShieldsOfVelisVel(final ShieldsOfVelisVel card) {
        super(card);
    }

    @Override
    public ShieldsOfVelisVel copy() {
        return new ShieldsOfVelisVel(this);
    }
}

class ShieldsOfVelisVelBoostEffect extends ContinuousEffectImpl {

    ShieldsOfVelisVelBoostEffect() {
        super(Duration.EndOfTurn, Layer.PTChangingEffects_7, SubLayer.ModifyPT_7c, Outcome.BoostCreature);
        staticText = "Creatures target player controls get +0/+1 and gain all creature types until end of turn";
    }

    private ShieldsOfVelisVelBoostEffect(final ShieldsOfVelisVelBoostEffect effect) {
        super(effect);
    }

    @Override
    public ShieldsOfVelisVelBoostEffect copy() {
        return new ShieldsOfVelisVelBoostEffect(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        if (getAffectedObjectsSet()) {
            List<Permanent> creatures = game.getBattlefield().getAllActivePermanents(StaticFilters.FILTER_PERMANENT_CREATURES, source.getFirstTarget(), game);
            for (Permanent creature : creatures) {
                affectedObjectList.add(new MageObjectReference(creature, game));
            }
        }
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            switch (layer) {
                case PTChangingEffects_7:
                    if (sublayer == SubLayer.ModifyPT_7c) {
                        permanent.addToughness(1);
                    }
                    break;
                case TypeChangingEffects_4:
                    permanent.setIsAllCreatureTypes(game, true);
                    break;
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (Iterator<MageObjectReference> it = affectedObjectList.iterator(); it.hasNext(); ) {
            Permanent permanent = it.next().getPermanent(game);
            if (permanent != null) {
                affectedObjects.add(permanent);
            } else {
                it.remove();
            }
        }
        return !affectedObjects.isEmpty();
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.PTChangingEffects_7 || layer == Layer.TypeChangingEffects_4;
    }

    @Override
    public boolean hasSubLayer(SubLayer sublayer) {
        return sublayer == SubLayer.ModifyPT_7c || sublayer == SubLayer.NA;
    }
}
