package mage.cards.g;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.SacrificeSourceUnlessPaysEffect;
import mage.abilities.mana.WhiteManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *
 * @author jmharmon
 */
public final class Glaciers extends CardImpl {

    public Glaciers(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{W}{U}");

        // At the beginning of your upkeep, sacrifice Glaciers unless you pay {W}{U}.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(
                new SacrificeSourceUnlessPaysEffect(new ManaCostsImpl<>("{W}{U}"))));

        // All Mountains are Plains.
        this.addAbility(new SimpleStaticAbility(new GlaciersEffect()));
    }

    private Glaciers(final Glaciers card) {
        super(card);
    }

    @Override
    public Glaciers copy() {
        return new Glaciers(this);
    }

    static class GlaciersEffect extends ContinuousEffectImpl {

        GlaciersEffect() {
            super(Duration.WhileOnBattlefield, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Detriment);
            this.staticText = "All Mountains are Plains";
            this.dependendToTypes.add(DependencyType.BecomeForest);
            this.dependendToTypes.add(DependencyType.BecomeIsland);
            this.dependendToTypes.add(DependencyType.BecomeMountain);
            this.dependendToTypes.add(DependencyType.BecomePlains);
            this.dependendToTypes.add(DependencyType.BecomeSwamp);
            this.dependencyTypes.add(DependencyType.BecomePlains);
        }

        private GlaciersEffect(final GlaciersEffect effect) {
            super(effect);
        }

        @Override
        public GlaciersEffect copy() {
            return new GlaciersEffect(this);
        }

        @Override
        public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
            for (MageItem object : affectedObjects) {
                Permanent land = (Permanent) object;
                // the land mana ability is intrinsic, so apply at this layer not layer 6
                land.removeAllSubTypes(game, SubTypeSet.NonBasicLandType);
                land.addSubType(game, SubType.PLAINS);
                land.removeAllAbilities(source.getSourceId(), game);
                land.addAbility(new WhiteManaAbility(), source.getSourceId(), game);
            }
        }

        @Override
        public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
            for (Permanent land : game.getBattlefield().getActivePermanents(StaticFilters.FILTER_LAND, source.getControllerId(), source, game)) {
                if (land.hasSubtype(SubType.MOUNTAIN, game)) {
                    affectedObjects.add(land);
                }
            }
            return !affectedObjects.isEmpty();
        }

        @Override
        public Set<UUID> isDependentTo(List<ContinuousEffect> allEffectsInLayer) {
            return allEffectsInLayer
                    .stream()
                    .filter(effect -> effect.getDependencyTypes().contains(DependencyType.BecomeMountain))
                    .map(Effect::getId)
                    .collect(Collectors.toSet());
        }
    }
}
