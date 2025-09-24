
package mage.cards.s;

import mage.MageItem;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.SplitSecondAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPlayer;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class SuddenSpoiling extends CardImpl {

    public SuddenSpoiling(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{B}{B}");

        // Split second
        this.addAbility(new SplitSecondAbility());
        // Until end of turn, creatures target player controls lose all abilities and have base power and toughness 0/2.
        this.getSpellAbility().addEffect(new SuddenSpoilingEffect(Duration.EndOfTurn));
        this.getSpellAbility().addTarget(new TargetPlayer());

    }

    private SuddenSpoiling(final SuddenSpoiling card) {
        super(card);
    }

    @Override
    public SuddenSpoiling copy() {
        return new SuddenSpoiling(this);
    }
}

class SuddenSpoilingEffect extends ContinuousEffectImpl {

    SuddenSpoilingEffect(Duration duration) {
        super(duration, Outcome.LoseAbility);
        staticText = "Until end of turn, creatures target player controls lose all abilities and have base power and toughness 0/2";
    }

    private SuddenSpoilingEffect(final SuddenSpoilingEffect effect) {
        super(effect);
    }

    @Override
    public SuddenSpoilingEffect copy() {
        return new SuddenSpoilingEffect(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        Player player = game.getPlayer(this.getTargetPointer().getFirst(game, source));
        if (player == null) {
            return;
        }
        for (Permanent perm : game.getBattlefield().getAllActivePermanents(StaticFilters.FILTER_PERMANENT_CREATURE, player.getId(), game)) {
            affectedObjectList.add(new MageObjectReference(perm, game));
        }
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            switch (layer) {
                case AbilityAddingRemovingEffects_6:
                    permanent.removeAllAbilities(source.getSourceId(), game);
                    break;
                case PTChangingEffects_7:
                    if (sublayer == SubLayer.SetPT_7b) {
                        permanent.getPower().setModifiedBaseValue(0);
                        permanent.getToughness().setModifiedBaseValue(2);
                    }
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        if (!source.getAffectedObjects().isEmpty()) {
            affectedObjects.addAll(source.getAffectedObjects());
        } else {
            Player player = game.getPlayer(this.getTargetPointer().getFirst(game, source));
            if (player == null) {
                return false;
            }
            for (Permanent perm : game.getBattlefield().getAllActivePermanents(StaticFilters.FILTER_PERMANENT_CREATURE, player.getId(), game)) {
                affectedObjects.add(perm);
                source.getAffectedObjects().add(perm);
            }
        }
        return !affectedObjects.isEmpty();
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.AbilityAddingRemovingEffects_6 || layer == Layer.PTChangingEffects_7;
    }

    @Override
    public boolean hasSubLayer(SubLayer sublayer) {
        return  sublayer == SubLayer.NA || sublayer == SubLayer.SetPT_7b;
    }
}
