package mage.cards.h;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class Humility extends CardImpl {

    public Humility(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{W}{W}");

        // All creatures lose all abilities and have base power and toughness 1/1.
        this.addAbility(new SimpleStaticAbility(
                new HumilityEffect(Duration.WhileOnBattlefield)));

    }

    private Humility(final Humility card) {
        super(card);
    }

    @Override
    public Humility copy() {
        return new Humility(this);
    }

    static class HumilityEffect extends ContinuousEffectImpl {

        public HumilityEffect(Duration duration) {
            super(duration, Outcome.LoseAbility);
            staticText = "All creatures lose all abilities and have base power and toughness 1/1";
        }

        private HumilityEffect(final HumilityEffect effect) {
            super(effect);
        }

        @Override
        public HumilityEffect copy() {
            return new HumilityEffect(this);
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
                            permanent.getPower().setModifiedBaseValue(1);
                            permanent.getToughness().setModifiedBaseValue(1);
                        }
                }
            }
        }

        @Override
        public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
            if (!source.getAffectedObjects().isEmpty()) {
                affectedObjects.addAll(source.getAffectedObjects());
            } else {
                    Player player = game.getPlayer(source.getControllerId());
                    if (player == null) {
                        return false;
                    }
                    for (Permanent permanent : game.getBattlefield().getActivePermanents(
                            StaticFilters.FILTER_PERMANENT_CREATURE, player.getId(), source, game)) {
                        affectedObjects.add(permanent);
                        source.getAffectedObjects().add(permanent);
                    }
            }
            return !affectedObjects.isEmpty();
        }

        @Override
        public boolean hasLayer(Layer layer) {
            return layer == Layer.AbilityAddingRemovingEffects_6
                    || layer == Layer.PTChangingEffects_7;
        }

        @Override
        public boolean hasSubLayer(SubLayer sublayer) {
            return sublayer == SubLayer.SetPT_7b || sublayer == SubLayer.NA;
        }
    }
}
