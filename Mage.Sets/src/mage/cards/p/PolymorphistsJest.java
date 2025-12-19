
package mage.cards.p;

import mage.MageItem;
import mage.MageObjectReference;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
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
 * @author LevelX2
 */
public final class PolymorphistsJest extends CardImpl {

    public PolymorphistsJest(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{U}{U}");

        // Until end of turn, each creature target player controls loses all abilities and becomes a blue Frog with base power and toughness 1/1.
        this.getSpellAbility().addEffect(new PolymorphistsJestEffect());
        this.getSpellAbility().addTarget(new TargetPlayer());
    }

    private PolymorphistsJest(final PolymorphistsJest card) {
        super(card);
    }

    @Override
    public PolymorphistsJest copy() {
        return new PolymorphistsJest(this);
    }
}

class PolymorphistsJestEffect extends ContinuousEffectImpl {

    PolymorphistsJestEffect() {
        super(Duration.EndOfTurn, Outcome.BecomeCreature);
        staticText = "Until end of turn, each creature target player controls loses all abilities and becomes a blue Frog with base power and toughness 1/1";
    }

    private PolymorphistsJestEffect(final PolymorphistsJestEffect effect) {
        super(effect);
    }

    @Override
    public PolymorphistsJestEffect copy() {
        return new PolymorphistsJestEffect(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        if (getAffectedObjectsSet()) {
            game.getBattlefield()
                    .getActivePermanents(
                            StaticFilters.FILTER_CONTROLLED_CREATURE,
                            getTargetPointer().getFirst(game, source), source, game
                    ).stream()
                    .map(permanent -> new MageObjectReference(permanent, game))
                    .forEach(affectedObjectList::add);
        }
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            switch (layer) {
                case TypeChangingEffects_4:
                    permanent.removeAllCreatureTypes(game);
                    permanent.addSubType(game, SubType.FROG);
                    break;
                case ColorChangingEffects_5:
                    permanent.getColor(game).setColor(ObjectColor.BLUE);
                    break;
                case AbilityAddingRemovingEffects_6:
                    permanent.removeAllAbilities(source.getSourceId(), game);
                    break;
                case PTChangingEffects_7:
                    if (sublayer == SubLayer.SetPT_7b) {
                        permanent.getPower().setModifiedBaseValue(1);
                        permanent.getToughness().setModifiedBaseValue(1);
                    }
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
        return !affectedObjects.isEmpty();
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.PTChangingEffects_7
                || layer == Layer.ColorChangingEffects_5
                || layer == Layer.AbilityAddingRemovingEffects_6
                || layer == Layer.TypeChangingEffects_4;
    }

    @Override
    public boolean hasSubLayer(SubLayer sublayer) {
        return sublayer == SubLayer.SetPT_7b || sublayer == SubLayer.NA;
    }
}
