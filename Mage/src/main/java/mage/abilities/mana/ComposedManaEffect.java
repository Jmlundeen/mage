package mage.abilities.mana;

import mage.MageObject;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.condition.Condition;
import mage.abilities.costs.Cost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.mana.ManaEffect;
import mage.abilities.mana.providers.ManaConditionProvider;
import mage.abilities.mana.providers.ManaPlayerProvider;
import mage.abilities.mana.value.ManaValue;
import mage.choices.Choice;
import mage.choices.ChoiceImpl;
import mage.constants.Duration;
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
        private final List<ManaConditionProvider> spendingConditionProviders = new ArrayList<>();
        private final ManaPlayerProvider manaPlayerProvider;
        private final Filter.ComparisonScope comparisonScope;
        private final boolean chooseManaValue;
        private final Cost anyPlayerPaysCost;
        private final String anyPlayerPaysChooseUseText;
        private final DynamicValue capacityOverride;
        private final Duration manaPoolDuration;
        private transient Mana possibleManaInPool;

        private ComposedManaEffect(final ComposedManaEffect effect) {
            super(effect);
            effect.manaValues.stream().map(ManaValue::copy).forEach(this.manaValues::add);
            this.spendingConditions.addAll(effect.spendingConditions);
            effect.spendingConditionProviders.stream().map(ManaConditionProvider::copy).forEach(this.spendingConditionProviders::add);
            this.manaPlayerProvider = effect.manaPlayerProvider == null ? null : effect.manaPlayerProvider.copy();
            this.comparisonScope = effect.comparisonScope;
            this.chooseManaValue = effect.chooseManaValue;
            this.anyPlayerPaysCost = effect.anyPlayerPaysCost == null ? null : effect.anyPlayerPaysCost.copy();
            this.anyPlayerPaysChooseUseText = effect.anyPlayerPaysChooseUseText;
            this.capacityOverride = effect.capacityOverride == null ? null : effect.capacityOverride.copy();
            this.manaPoolDuration = effect.manaPoolDuration;
        }

        public ComposedManaEffect(List<ManaValue> manaValues, List<Condition> spendingConditions,
                                  List<ManaConditionProvider> spendingConditionProviders,
                                  ManaPlayerProvider manaPlayerProvider, Filter.ComparisonScope comparisonScope,
                                  boolean chooseManaValue,
                                  Cost anyPlayerPaysCost,
                                  String anyPlayerPaysChooseUseText,
                                  DynamicValue capacityOverride,
                                  Duration manaPoolDuration) {
            super();
            this.manaValues.addAll(manaValues);
            this.spendingConditions.addAll(spendingConditions);
            this.spendingConditionProviders.addAll(spendingConditionProviders);
            this.manaPlayerProvider = manaPlayerProvider;
            this.comparisonScope = comparisonScope;
            this.chooseManaValue = chooseManaValue;
            this.anyPlayerPaysCost = anyPlayerPaysCost;
            this.anyPlayerPaysChooseUseText = anyPlayerPaysChooseUseText;
            this.capacityOverride = capacityOverride;
            this.manaPoolDuration = manaPoolDuration;
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

        public Duration getManaPoolDuration() {
            return manaPoolDuration;
        }

    @Override
        public List<Mana> getNetMana(Game game, Ability source) {
            List<Condition> resolvedSpendingConditions = getSpendingConditions(game, source);
            List<Mana> netMana = chooseManaValue
                    ? getChoiceNetMana(game, source)
                    : getCombinedNetMana(game, source);
            return applySpendingConditions(netMana, resolvedSpendingConditions);
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
            if (chooseManaValue) {
                return chooseProducedMana(game, source);
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

        private List<Mana> getChoiceNetMana(Game game, Ability source) {
            List<Mana> options = new ArrayList<>();
            for (ManaValue manaValue : manaValues) {
                List<Mana> evaluated = manaValue.evaluate(game, source, this, false);
                if (evaluated != null) {
                    for (Mana mana : evaluated) {
                        if (mana != null && mana.count() > 0) {
                            options.add(mana);
                        }
                    }
                }
            }
            return distinct(options);
        }

        private List<Mana> getCombinedNetMana(Game game, Ability source) {
            List<Mana> combined = new ArrayList<>();
            combined.add(new Mana());

            for (ManaValue manaValue : manaValues) {
                List<Mana> evaluated = manaValue.evaluate(game, source, this, false);
                if (evaluated == null || evaluated.isEmpty()) {
                    continue;
                }

                List<Mana> next = new ArrayList<>();
                for (Mana baseMana : combined) {
                    for (Mana addedMana : evaluated) {
                        if (addedMana == null) {
                            continue;
                        }
                        Mana mana = baseMana.copy();
                        mana.add(addedMana);
                        next.add(mana);
                    }
                }
                combined = distinct(next);
            }

            combined.removeIf(mana -> mana == null || mana.count() <= 0);
            return combined;
        }

        private Mana chooseProducedMana(Game game, Ability source) {
            Map<String, ManaValue> manaValueChoices = new LinkedHashMap<>();
            for (ManaValue manaValue : manaValues) {
                List<Mana> evaluated = manaValue.evaluate(game, source, this, false);
                String choiceText = describeManaValue(evaluated);
                if (choiceText != null) {
                    manaValueChoices.putIfAbsent(choiceText, manaValue);
                }
            }

            if (manaValueChoices.isEmpty()) {
                return new Mana();
            }
            if (manaValueChoices.size() == 1) {
                return produceMana(manaValueChoices.values().iterator().next(), game, source);
            }

            Player player = getChoicePlayer(game, source);
            if (player == null) {
                return new Mana();
            }

            Choice choice = new ChoiceImpl(true);
            choice.setMessage("Choose mana to add");
            choice.getChoices().addAll(manaValueChoices.keySet());
            if (!player.choose(Outcome.PutManaInPool, choice, game)) {
                return new Mana();
            }

            ManaValue chosenManaValue = manaValueChoices.get(choice.getChoice());
            if (chosenManaValue != null) {
                return produceMana(chosenManaValue, game, source);
            }
            return new Mana();
        }

        private Mana produceMana(ManaValue manaValue, Game game, Ability source) {
            Mana result = new Mana();
            List<Mana> evaluated = manaValue.evaluate(game, source, this, true);
            if (evaluated != null) {
                for (Mana mana : evaluated) {
                    if (mana != null) {
                        result.add(mana);
                    }
                }
            }
            return result;
        }

        private String describeManaValue(List<Mana> evaluated) {
            if (evaluated == null || evaluated.isEmpty()) {
                return null;
            }

            List<String> descriptions = evaluated.stream()
                    .filter(Objects::nonNull)
                    .filter(mana -> mana.count() > 0)
                    .map(Mana::toString)
                    .distinct()
                    .toList();
            if (descriptions.isEmpty()) {
                return null;
            }
            return descriptions.size() == 1 ? descriptions.getFirst() : CardUtil.concatWithOr(descriptions);
        }

        private List<Mana> applySpendingConditions(List<Mana> manaOptions, List<Condition> resolvedSpendingConditions) {
            if (manaOptions.isEmpty()) {
                return manaOptions;
            }

            List<Mana> result = new ArrayList<>(manaOptions.size());
            String conditionText = resolvedSpendingConditions.isEmpty() ? null : buildConditionText(resolvedSpendingConditions);
            for (Mana mana : manaOptions) {
                if (mana == null || mana.count() <= 0) {
                    continue;
                }
                Mana manaWithConditions = mana.copy();
                if (!resolvedSpendingConditions.isEmpty()) {
                    manaWithConditions.addConditions(resolvedSpendingConditions);
                    manaWithConditions.setComparisonScope(comparisonScope);
                    manaWithConditions.setConditionText(conditionText);
                }
                result.add(manaWithConditions);
            }
            return result;
        }

        private List<Mana> distinct(List<Mana> manaOptions) {
            return new ArrayList<>(new LinkedHashSet<>(manaOptions));
        }

        @Override
        public ManaEffect copy() {
            return new ComposedManaEffect(this);
        }

        @Override
        public boolean apply(Game game, Ability source) {
            if (hasSpendingConditions()) {
                return applyConditional(game, source);
            }
            return super.apply(game, source);
        }

        private boolean applyConditional(Game game, Ability source) {
            Player player = getPlayer(game, source);
            if (player == null) {
                return false;
            }
            List<Condition> resolvedSpendingConditions = getSpendingConditions(game, source);
            
            if (game.inCheckPlayableState()) {
                if (source.isTriggeredAbility()) {
                    player.addAvailableTriggeredMana(getNetMana(game, source));
                }
                return true;
            }

            Mana manaToAdd = produceMana(game, source);
            if (manaToAdd.count() > 0) {
                // Attach conditions directly to Mana instead of using ConditionalMana
                if (!resolvedSpendingConditions.isEmpty()) {
                    manaToAdd.addConditions(resolvedSpendingConditions);
                    manaToAdd.setComparisonScope(comparisonScope);
                    manaToAdd.setConditionText(buildConditionText(resolvedSpendingConditions));
                }
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

        private boolean hasSpendingConditions() {
            return !spendingConditions.isEmpty() || !spendingConditionProviders.isEmpty();
        }

        private List<Condition> getSpendingConditions(Game game, Ability source) {
            if (spendingConditionProviders.isEmpty()) {
                return spendingConditions;
            }

            List<Condition> result = new ArrayList<>(spendingConditions);
            for (ManaConditionProvider provider : spendingConditionProviders) {
                if (provider == null) {
                    continue;
                }
                List<Condition> resolvedConditions = provider.getConditions(game, source, this);
                if (resolvedConditions != null && !resolvedConditions.isEmpty()) {
                    result.addAll(resolvedConditions);
                }
            }
            return result;
        }

        private String buildConditionText(List<Condition> conditions) {
            StringBuilder sb = new StringBuilder();
            for (Condition condition : conditions) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append(condition.getManaText());
            }
            return sb.toString();
        }

        @Override
        protected void addManaToPool(Player player, Mana manaToAdd, Game game, Ability source) {
            if (manaPoolDuration != null) {
                player.getManaPool().addMana(manaToAdd, game, source, manaPoolDuration);
            } else if (manaToAdd.hasConditions()) {
                player.getManaPool().addMana(manaToAdd, game, source);
            } else {
                super.addManaToPool(player, manaToAdd, game, source);
            }
        }
    }