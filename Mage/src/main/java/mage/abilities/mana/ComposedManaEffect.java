package mage.abilities.mana;

import mage.MageObject;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.condition.Condition;
import mage.abilities.costs.Cost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.mana.ManaEffect;
import mage.abilities.mana.providers.ManaPlayerProvider;
import mage.abilities.mana.value.ManaValue;
import mage.constants.ManaType;
import mage.constants.Outcome;
import mage.filter.Filter;
import mage.game.Game;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.*;

/**
 * Mana effect produced by {@link mage.abilities.mana.ComposedManaAbilityBuilder ComposedManaAbilityBuilder}
 * @author jmlundeen
 */
public class ComposedManaEffect extends ManaEffect {
        private final List<ManaValue> manaValues = new ArrayList<>();
        private final List<Condition> spendingConditions = new ArrayList<>();
        private final ManaPlayerProvider manaPlayerProvider;
        private final Filter.ComparisonScope comparisonScope;
        private final Cost anyPlayerPaysCost;
        private final String anyPlayerPaysChooseUseText;
        private final DynamicValue capacityOverride;
        private transient Mana possibleManaInPool;

        private ComposedManaEffect(final ComposedManaEffect effect) {
            super(effect);
            effect.manaValues.stream().map(ManaValue::copy).forEach(this.manaValues::add);
            this.spendingConditions.addAll(effect.spendingConditions);
            this.manaPlayerProvider = effect.manaPlayerProvider == null ? null : effect.manaPlayerProvider.copy();
            this.comparisonScope = effect.comparisonScope;
            this.anyPlayerPaysCost = effect.anyPlayerPaysCost == null ? null : effect.anyPlayerPaysCost.copy();
            this.anyPlayerPaysChooseUseText = effect.anyPlayerPaysChooseUseText;
            this.capacityOverride = effect.capacityOverride == null ? null : effect.capacityOverride.copy();
        }

        public ComposedManaEffect(List<ManaValue> manaValues, List<Condition> spendingConditions,
                                  ManaPlayerProvider manaPlayerProvider, Filter.ComparisonScope comparisonScope,
                                  Cost anyPlayerPaysCost,
                                  String anyPlayerPaysChooseUseText,
                                  DynamicValue capacityOverride) {
            super();
            this.manaValues.addAll(manaValues);
            this.spendingConditions.addAll(spendingConditions);
            this.manaPlayerProvider = manaPlayerProvider;
            this.comparisonScope = comparisonScope;
            this.anyPlayerPaysCost = anyPlayerPaysCost;
            this.anyPlayerPaysChooseUseText = anyPlayerPaysChooseUseText;
            this.capacityOverride = capacityOverride;
        }

        @Override
        protected Player getPlayer(Game game, Ability source) {
            if (manaPlayerProvider != null) {
                Player player = manaPlayerProvider.getManaPlayer(game, source, this);
                if (player != null) {
                    return player;
                }
            }
            return super.getPlayer(game, source);
        }

        public Player getChoicePlayer(Game game, Ability source) {
            if (manaPlayerProvider != null) {
                Player player = manaPlayerProvider.getChoicePlayer(game, source, this);
                if (player != null) {
                    return player;
                }
            }
            return getPlayer(game, source);
        }

        public Player getManaPlayer(Game game, Ability source) {
            return getPlayer(game, source);
        }

        public Mana getPossibleManaInPool() {
            return possibleManaInPool == null ? null : possibleManaInPool.copy();
        }

        public DynamicValue getCapacityOverride() {
            return capacityOverride;
        }

    @Override
        public List<Mana> getNetMana(Game game, Ability source) {
            List<Mana> netMana = new ArrayList<>();
            if (game == null) {
                return netMana;
            }
            for (ManaValue mv : manaValues) {
                List<Mana> evaluated = mv.evaluate(game, source, this, false);
                if (evaluated != null) {
                    for (Mana m : evaluated) {
                        // Attach conditions to each mana option for playable calculations
                        if (!spendingConditions.isEmpty()) {
                            m.addConditions(spendingConditions);
                            m.setComparisonScope(comparisonScope);
                            m.setConditionText(buildConditionText());
                        }
                        netMana.add(m);
                    }
                }
            }
            return netMana;
        }

        @Override
        public List<Mana> getNetMana(Game game, Mana possibleManaInPool, Ability source) {
            this.possibleManaInPool = possibleManaInPool == null ? null : possibleManaInPool.copy();
            try {
                return getNetMana(game, source);
            } finally {
                this.possibleManaInPool = null;
            }
        }

        @Override
        public Set<ManaType> getProducableManaTypes(Game game, Ability source) {
            Set<ManaType> types = HashSet.newHashSet(5);
            for (ManaValue mv : manaValues) {
                types.addAll(mv.getProducibleTypes(game, source, this));
            }
            return types.isEmpty() ? super.getProducableManaTypes(game, source) : types;
        }

        @Override
        public Mana produceMana(Game game, Ability source) {
            if (isPreventedByAnyPlayerPaying(game, source)) {
                return new Mana();
            }
            Mana result = new Mana();
            for (ManaValue mv : manaValues) {
                List<Mana> evaluated = mv.evaluate(game, source, this, true);
                if (evaluated != null) {
                    for (Mana m : evaluated) {
                        result.add(m);
                    }
                }
            }
            return result;
        }

        @Override
        public ManaEffect copy() {
            return new ComposedManaEffect(this);
        }

        @Override
        public boolean apply(Game game, Ability source) {
            if (!spendingConditions.isEmpty()) {
                return applyConditional(game, source);
            }
            return super.apply(game, source);
        }

        private boolean applyConditional(Game game, Ability source) {
            Player player = getPlayer(game, source);
            if (player == null) {
                return false;
            }
            
            if (game.inCheckPlayableState()) {
                if (source.isTriggeredAbility()) {
                    player.addAvailableTriggeredMana(getNetMana(game, source));
                }
                return true;
            }

            Mana manaToAdd = produceMana(game, source);
            if (manaToAdd.count() > 0) {
                // Attach conditions directly to Mana instead of using ConditionalMana
                manaToAdd.addConditions(spendingConditions);
                manaToAdd.setComparisonScope(comparisonScope);
                manaToAdd.setConditionText(buildConditionText());
                checkToFirePossibleEvents(manaToAdd, game, source);
                addManaToPool(player, manaToAdd, game, source);
            }
            return true;
        }

        private boolean isPreventedByAnyPlayerPaying(Game game, Ability source) {
            if (game == null || anyPlayerPaysCost == null) {
                return false;
            }

            Player controller = getPlayer(game, source);
            if (controller == null) {
                return false;
            }

            MageObject sourceObject = game.getObject(source);
            String message = sourceObject == null
                    ? anyPlayerPaysChooseUseText
                    : CardUtil.replaceSourceName(anyPlayerPaysChooseUseText, sourceObject.getName());
            for (UUID playerId : game.getState().getPlayersInRange(controller.getId(), game)) {
                Player player = game.getPlayer(playerId);
                if (player != null
                        && player.canRespond()
                        && anyPlayerPaysCost.canPay(source, source, player.getId(), game)
                        && player.chooseUse(Outcome.Detriment, message, source, game)) {
                    anyPlayerPaysCost.clearPaid();
                    if (anyPlayerPaysCost.pay(source, game, source, player.getId(), false, null)) {
                        if (!game.isSimulation()) {
                            game.informPlayers(player.getLogName() + " pays the cost to prevent the effect");
                        }
                        return true;
                    }
                }
            }
            return false;
        }

        private String buildConditionText() {
            StringBuilder sb = new StringBuilder();
            for (Condition condition : spendingConditions) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append(condition.getManaText());
            }
            return sb.toString();
        }

        @Override
        protected void addManaToPool(Player player, Mana manaToAdd, Game game, Ability source) {
            if (manaToAdd.hasConditions()) {
                player.getManaPool().addMana(manaToAdd, game, source);
            } else {
                super.addManaToPool(player, manaToAdd, game, source);
            }
        }
    }