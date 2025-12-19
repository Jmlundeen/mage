package mage.cards.e;

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
public final class EgoErasure extends CardImpl {

    public EgoErasure(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.KINDRED, CardType.INSTANT}, "{2}{U}");
        this.subtype.add(SubType.SHAPESHIFTER);

        // Changeling
        this.addAbility(new ChangelingAbility());

        // Creatures target player controls get -2/+0 and lose all creature types until end of turn.
        this.getSpellAbility().addEffect(new EgoErasureEffect());
        this.getSpellAbility().addTarget(new TargetPlayer());
    }

    private EgoErasure(final EgoErasure card) {
        super(card);
    }

    @Override
    public EgoErasure copy() {
        return new EgoErasure(this);
    }
}

class EgoErasureEffect extends ContinuousEffectImpl {

    EgoErasureEffect() {
        super(Duration.EndOfTurn, Outcome.Neutral);
        staticText = "creatures target player controls get -2/-0 and lose all creature types until end of turn";
    }

    private EgoErasureEffect(final EgoErasureEffect effect) {
        super(effect);
    }

    @Override
    public EgoErasureEffect copy() {
        return new EgoErasureEffect(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        game.getBattlefield()
                .getActivePermanents(
                        StaticFilters.FILTER_CONTROLLED_CREATURE,
                        source.getFirstTarget(), source, game
                ).stream()
                .map(permanent -> new MageObjectReference(permanent, game))
                .forEach(affectedObjectList::add);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            switch (layer) {
                case TypeChangingEffects_4:
                    permanent.removeAllCreatureTypes(game);
                    break;
                case PTChangingEffects_7:
                    if (sublayer == SubLayer.ModifyPT_7c) {
                        permanent.addPower(-2);
                    }
                    break;
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (Iterator<MageObjectReference> it = affectedObjectList.iterator(); it.hasNext(); ) {
            Permanent permanent = it.next().getPermanent(game);
            if (permanent == null) {
                it.remove();
                continue;
            }
            affectedObjects.add(permanent);
        }
        if (affectedObjectList.isEmpty()) {
            discard();
            return false;
        }
        return !affectedObjects.isEmpty();
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.TypeChangingEffects_4 || layer == Layer.PTChangingEffects_7;
    }

    @Override
    public boolean hasSubLayer(SubLayer sublayer) {
        return sublayer == SubLayer.NA || sublayer == SubLayer.ModifyPT_7c;
    }
}
