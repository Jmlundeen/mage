package mage.abilities.mana;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.costs.mana.ManaCost;
import mage.constants.ManaType;
import mage.game.Game;
import mage.players.ManaPoolItem;
import org.jgrapht.alg.flow.DinicMFImpl;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jspecify.annotations.NonNull;

import java.util.*;

/**
 * Max-flow graph solver for mana payment feasibility.
 * <p>
 * Uses Dinic's max-flow approach to check possible payments.
 *
 * <h2>Graph Structure (spell payment)</h2>
 * <pre>
 * SuperSource ──[cap=option.capacity]──► ManaSourceNode
 * ManaSourceNode ──[cap=capacityBetween(symbol,option)]──► CostNode
 * CostNode ──[cap=symbol.minCost()]──► SuperSink
 * </pre>
 *
 * Flow represents mana from sources being allocated to pay costs.
 */
public final class ManaPaymentFlowSolver {

    private static final int MAX_REUSE_ACTIVATIONS = 5;
    private static final int MAX_PAYMENT_ALLOCATION_RESULTS = 256;

    private ManaPaymentFlowSolver() {
    }

    /**
     * Returns {@code true} if given cost can be paid from given sources.
     * Does not check conditions or AsThoughEffects.
     *
     * @param cost    list of parsed mana cost symbols
     * @param sources available mana source nodes
     */
    public static boolean canPay(List<ManaCostSymbol> cost, List<ManaSourceNode> sources) {
        return canPay(cost, sources, null, null, null, null);
    }

    /**
     * Returns {@code true} if given cost can be paid from given sources.
     * Checks conditions on mana options and AsThoughEffects when game and ability are provided.
     *
     * @param cost     list of parsed mana cost symbols
     * @param sources  available mana source nodes
     * @param game     the game state (for AsThoughEffects and conditions)
     * @param ability  the ability being paid for (for AsThoughEffects)
     */
    public static boolean canPay(List<ManaCostSymbol> cost, List<ManaSourceNode> sources, Game game, Ability ability) {
        UUID playerId = ability != null ? ability.getControllerId() : null;
        ManaCost manaCost = ability != null ? ability.getManaCostsToPay() : null;
        return canPay(cost, sources, game, ability, playerId, manaCost);
    }

    /**
     * Returns {@code true} if the given cost can be paid from the given sources.
     * Checks conditions on mana options and AsThoughEffects when game and ability are provided.
     *
     * @param cost     list of parsed mana cost symbols
     * @param sources  available mana source nodes
     * @param game     the game state
     * @param ability  the ability being paid for (for AsThoughEffects)
     * @param playerId the player paying the cost; may be {@code null}
     * @param manaCost the full mana cost being paid; may be {@code null}
     */
    public static boolean canPay(List<ManaCostSymbol> cost, List<ManaSourceNode> sources, Game game, Ability ability, UUID playerId, ManaCost manaCost) {
        if (cost.isEmpty()) {
            return true;
        }
        if (sources.isEmpty()) {
            return cost.stream().allMatch(s -> s.minCost() == 0);
        }

        List<ManaSourceNode> pureFree = new ArrayList<>();
        List<ManaSourceNode> purePaid = new ArrayList<>();
        List<ManaSourceNode> mixed    = new ArrayList<>();

        for (ManaSourceNode node : sources) {
            if (node.isMixed()) {
                mixed.add(node);
            } else if (node.isAllPaid()) {
                purePaid.add(node);
            } else if (node.hasSingleOption()) {
                pureFree.add(node);
            } else {
                // Multi-option free nodes must still be enumerated to enforce one choice per activation.
                mixed.add(node);
            }
        }

        List<ManaAbilityOption> freePool = new ArrayList<>();
        for (ManaSourceNode node : pureFree) {
            ManaAbilityOption option = node.getSingleOption();
            if (game == null || ability == null || checkConditions(option, ability, game, manaCost)) {
                freePool.add(option);
            }
        }

        SearchContext searchContext = new SearchContext();
        searchContext.purePaid = purePaid;
        searchContext.freePool = freePool;
        return enumerateMixedSelections(cost, mixed, game, ability, playerId, manaCost, 0, searchContext);
    }

    /**
     * Stores memoized state shared across recursive payment searches.
     */
    private static class SearchContext {
        List<ManaSourceNode> purePaid = new ArrayList<>();
        List<ManaAbilityOption> freePool = new ArrayList<>();
        final List<ManaAbilityOption> selectedMixedOptions = new ArrayList<>();
        final Map<String, List<List<ManaAbilityOption>>> paidActivationPoolCache = new HashMap<>();
        final Map<String, List<ManaAbilityOption>> paidOutputCache = new HashMap<>();
        final Map<String, Boolean> paidConditionCache = new HashMap<>();
        final Set<String> paidFailedStates = new HashSet<>();
    }

    /**
     * Evaluates mana-option conditions
     */
    private static boolean checkConditions(ManaAbilityOption option, Ability ability, Game game, ManaCost manaCost) {
        if (!option.hasConditions() || game == null || ability == null) {
            return true;
        }
        return option.applyConditions(ability, game, option.getSourceId(), manaCost);
    }

    /**
     * Recursively tries each ability option on each mixed node (those with both
     * free and paid abilities). At the leaf, delegates to paid-funding enumeration.
     * Populates {@code searchContext.selectedMixedOptions} and
     * {@code searchContext.selectedPaidOptions} on success.
     */
    private static boolean enumerateMixedSelections(List<ManaCostSymbol> cost,
                                                     List<ManaSourceNode> mixed,
                                                     Game game,
                                                     Ability ability,
                                                     UUID playerId,
                                                     ManaCost manaCost,
                                                     int idx,
                                                     SearchContext searchContext) {
        if (idx == mixed.size()) {
            return enumeratePaidFunding(cost, searchContext, game, ability, playerId, manaCost);
        }

        ManaSourceNode node = mixed.get(idx);
        for (ManaAbilityOption option : node.getAbilityOptions()) {
            boolean conditionsMet = game == null || ability == null || checkConditions(option, ability, game, manaCost);

            if (conditionsMet) {
                searchContext.selectedMixedOptions.add(option);
            }

            if (enumerateMixedSelections(cost, mixed, game, ability, playerId, manaCost, idx + 1, searchContext)) {
                return true;
            }

            if (conditionsMet) {
                searchContext.selectedMixedOptions.removeLast();
            } else {
                // If conditions weren't met, we already tried the branch without this option
                return false;
            }
        }
        return false;
    }

    /**
     * Splits selected options into free, paid, and pool-dependent groups, then searches paid activations.
     */
    private static boolean enumeratePaidFunding(List<ManaCostSymbol> cost,
                                                SearchContext searchContext,
                                                Game game,
                                                Ability ability,
                                                UUID playerId,
                                                ManaCost manaCost) {
        List<ManaAbilityOption> freeMixed = new ArrayList<>();
        List<ManaAbilityOption> paidMixed = new ArrayList<>();
        for (ManaAbilityOption opt : searchContext.selectedMixedOptions) {
            if (opt.hasCost()) {
                paidMixed.add(opt);
            } else {
                freeMixed.add(opt);
            }
        }

        List<ManaAbilityOption> allFree = new ArrayList<>(searchContext.freePool);
        allFree.addAll(freeMixed);

        // Separate pool-dependent abilities (e.g., Doubling Cube) from normal abilities
        List<ManaAbilityOption> normalFree = new ArrayList<>();
        List<ManaAbilityOption> poolDependentFree = new ArrayList<>();
        for (ManaAbilityOption opt : allFree) {
            if (opt.isPoolDependent()) {
                poolDependentFree.add(opt);
            } else {
                normalFree.add(opt);
            }
        }

        List<ManaAbilityOption> allPaid = new ArrayList<>();
        for (ManaSourceNode node : searchContext.purePaid) {
            allPaid.addAll(node.getAbilityOptions());
        }
        allPaid.addAll(paidMixed);

        // Also separate pool-dependent from paid abilities
        List<ManaAbilityOption> normalPaid = new ArrayList<>();
        List<ManaAbilityOption> poolDependentPaid = new ArrayList<>();
        for (ManaAbilityOption opt : allPaid) {
            if (opt.isPoolDependent()) {
                poolDependentPaid.add(opt);
            } else {
                normalPaid.add(opt);
            }
        }

        normalPaid.sort(Comparator.comparingInt(o -> o.getActivationCost().stream()
                .mapToInt(ManaCostSymbol::minCost)
                .sum()));

        // Precompute a key that identifies exactly which paid options are being processed.
        // This prevents cross-contamination in paidFailedStates when different mixed selections
        // produce different allPaid lists that happen to share the same index/freePool shape.
        String allPaidKey = buildAllPaidKey(normalPaid);

        // Pass both normal and pool-dependent lists to the enumeration
        return enumeratePaidActivations(cost, normalFree, normalPaid, poolDependentFree, poolDependentPaid, 0, Collections.emptySet(), new HashMap<>(), normalFree, allPaidKey, searchContext.paidActivationPoolCache, searchContext.paidOutputCache, searchContext.paidConditionCache, searchContext, game, ability, playerId, manaCost);
    }

    /**
     * Enumerates how many times each paid ability is activated while tracking remaining free mana.
     */
    private static boolean enumeratePaidActivations(List<ManaCostSymbol> cost,
                                                    List<ManaAbilityOption> allFree,
                                                    List<ManaAbilityOption> allPaid,
                                                    List<ManaAbilityOption> poolDependentFree,
                                                    List<ManaAbilityOption> poolDependentPaid,
                                                    int index,
                                                    Set<UUID> spentFreeIds,
                                                    Map<UUID, Integer> activationCounts,
                                                    List<ManaAbilityOption> currentFreePool,
                                                    String allPaidKey,
                                                    Map<String, List<List<ManaAbilityOption>>> paidActivationPoolCache,
                                                    Map<String, List<ManaAbilityOption>> paidOutputCache,
                                                    Map<String, Boolean> paidConditionCache,
                                                    SearchContext searchContext, Game game, Ability ability, UUID playerId, ManaCost manaCost) {

        // Failed-state memoization must include exact branch state, not only pooled mana shape.
        String stateKey = buildPaidActivationStateKey(allPaidKey, index, currentFreePool, spentFreeIds, activationCounts, allPaid);
        if (searchContext.paidFailedStates.contains(stateKey)) {
            return false;
        }

        if (index == allPaid.size()) {
            // Filter the free pool to only include triggered mana whose triggering ability is active
            List<ManaAbilityOption> effectiveFreePool = filterTriggeredMana(
                currentFreePool,
                activationCounts,
                searchContext.selectedMixedOptions,
                allPaid
            );

            if (solveWithHybrids(cost, effectiveFreePool, game, ability)) {
                return true;
            }

            // If not solvable without pool-dependent, try enumerating pool-dependent abilities
            if (!poolDependentFree.isEmpty() || !poolDependentPaid.isEmpty()) {
                List<ManaAbilityOption> allPoolDependent = new ArrayList<>();
                allPoolDependent.addAll(poolDependentFree);
                allPoolDependent.addAll(poolDependentPaid);

                return enumeratePoolDependentActivations(
                        cost, effectiveFreePool, allPoolDependent,
                        searchContext, game, ability, playerId, manaCost);
            }

            return false;
        }

        ManaAbilityOption paid = allPaid.get(index);
        int maxActivations = paid.hasTapCost() ? 1 : paid.getMaxActivationsPerTurn();
        if (maxActivations == Integer.MAX_VALUE) {
            maxActivations = MAX_REUSE_ACTIVATIONS;
        }

        for (int count = 0; count <= maxActivations; count++) {
            activationCounts.put(paid.getOptionId(), count);

            List<ManaCostSymbol> activationCost = paid.getActivationCost();
            if (count == 0) {
                if (enumeratePaidActivations(cost, allFree, allPaid, poolDependentFree, poolDependentPaid, index + 1, spentFreeIds, activationCounts, currentFreePool, allPaidKey, paidActivationPoolCache, paidOutputCache, paidConditionCache, searchContext, game, ability, playerId, manaCost)) {
                    return true;
                }
            } else {
                List<List<ManaAbilityOption>> poolWithPaidOutput = getPoolWithPaidActivations(activationCost, spentFreeIds, currentFreePool, paid, game, count, manaCost, paidActivationPoolCache, paidOutputCache, paidConditionCache, activationCounts, searchContext.selectedMixedOptions, allPaid);
                for ( List<ManaAbilityOption> pool : poolWithPaidOutput) {
                    Set<UUID> newSpentFreeIds = buildNextSpentFreeIds(spentFreeIds, currentFreePool, pool);
                    if (enumeratePaidActivations(cost, allFree, allPaid, poolDependentFree, poolDependentPaid, index + 1, newSpentFreeIds, activationCounts, pool, allPaidKey, paidActivationPoolCache, paidOutputCache, paidConditionCache, searchContext, game, ability, playerId, manaCost)) {
                        return true;
                    }
                }
            }
        }

        activationCounts.remove(paid.getOptionId());
        searchContext.paidFailedStates.add(stateKey);
        return false;
    }

    /**
     * Enumerates pool-dependent ability activations by trying different combinations.
     * Pool-dependent abilities (like Doubling Cube) depend on the current pool state and can cascade.
     *
     * @param cost the mana cost to pay
     * @param basePool the available mana pool before activating pool-dependent abilities
     * @param poolDependentOptions the pool-dependent abilities to consider
     * @param searchContext the result searchContext
     * @param game the game state
     * @param ability the ability being paid for
     * @param playerId the player paying the cost
     * @param manaCost the full mana cost being paid
     * @return true if a valid payment plan was found
     */
    private static boolean enumeratePoolDependentActivations(
            List<ManaCostSymbol> cost,
            List<ManaAbilityOption> basePool,
            List<ManaAbilityOption> poolDependentOptions,
            SearchContext searchContext,
            Game game,
            Ability ability,
            UUID playerId,
            ManaCost manaCost) {

        // Try without activating any pool-dependent abilities first
        if (solveWithHybrids(cost, basePool, game, ability)) {
            return true;
        }

        // Enumerate different combinations of pool-dependent activations
        return enumeratePoolDependentRecursive(
                cost, basePool, poolDependentOptions,
                0, new ArrayList<>(), searchContext,
                game, ability, playerId, manaCost);
    }

    /**
     * Recursively enumerates pool-dependent ability activations.
     * Tries each option at each index, checking if the activation is affordable and beneficial.
     *
     * @param cost the mana cost to pay
     * @param currentPool the current pool state
     * @param poolDependentOptions all available pool-dependent abilities
     * @param index current index in poolDependentOptions
     * @param activatedOptions the options activated so far
     * @param searchContext the result searchContext
     * @param game the game state
     * @param ability the ability being paid for
     * @param playerId the player paying the cost
     * @param manaCost the full mana cost being paid
     * @return true if a valid payment plan was found
     */
    private static boolean enumeratePoolDependentRecursive(
            List<ManaCostSymbol> cost,
            List<ManaAbilityOption> currentPool,
            List<ManaAbilityOption> poolDependentOptions,
            int index,
            List<ManaAbilityOption> activatedOptions,
            SearchContext searchContext,
            Game game,
            Ability ability,
            UUID playerId,
            ManaCost manaCost) {

        // Base case: tried all pool-dependent options
        if (index == poolDependentOptions.size()) {
            return solveWithHybrids(cost, currentPool, game, ability);
        }

        ManaAbilityOption poolDepOpt = poolDependentOptions.get(index);
        int maxActivations = poolDepOpt.hasTapCost() ? 1 : MAX_REUSE_ACTIVATIONS;

        // Try activating this option 0 to maxActivations times
        for (int count = 0; count <= maxActivations; count++) {
            if (count == 0) {
                // Don't activate this option - recurse to next
                if (enumeratePoolDependentRecursive(
                        cost, currentPool, poolDependentOptions,
                        index + 1, activatedOptions, searchContext,
                        game, ability, playerId, manaCost)) {
                    return true;
                }
            } else {
                // Try activating this option 'count' times
                List<ManaAbilityOption> newPool = currentPool;
                boolean allSucceeded = true;

                // Try all possible ways to activate 'count' times
                if (tryPoolDependentActivations(
                        poolDepOpt, currentPool, count,
                        cost, poolDependentOptions, index + 1,
                        activatedOptions, searchContext,
                        game, ability, playerId, manaCost)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Tries activating a pool-dependent ability 'count' times, enumerating all possible
     * ways to pay the activation cost. This is critical because different payment allocations
     * leave different remaining pools, which produce different outputs when doubled.
     *
     * @param poolDepOpt the pool-dependent ability to activate
     * @param currentPool the current pool state
     * @param count how many times to activate
     * @param cost the mana cost we're trying to pay
     * @param poolDependentOptions all pool-dependent options
     * @param nextIndex the next index in poolDependentOptions to try
     * @param activatedOptions the options activated so far
     * @param searchContext the result searchContext
     * @param game the game state
     * @param ability the ability being paid for
     * @param playerId the player paying the cost
     * @param manaCost the full mana cost being paid
     * @return true if found a valid solution
     */
    private static boolean tryPoolDependentActivations(
            ManaAbilityOption poolDepOpt,
            List<ManaAbilityOption> currentPool,
            int count,
            List<ManaCostSymbol> cost,
            List<ManaAbilityOption> poolDependentOptions,
            int nextIndex,
            List<ManaAbilityOption> activatedOptions,
            SearchContext searchContext,
            Game game,
            Ability ability,
            UUID playerId,
            ManaCost manaCost) {

        // Recursively activate 'count' times, enumerating payment allocations
        return activatePoolDependentWithEnumeration(
                poolDepOpt, currentPool, count, 0,
                cost, poolDependentOptions, nextIndex,
                activatedOptions, searchContext,
                game, ability, playerId, manaCost);
    }

    /**
     * Recursively activates a pool-dependent ability, enumerating all ways to pay
     * the activation cost at each step.
     */
    private static boolean activatePoolDependentWithEnumeration(
            ManaAbilityOption poolDepOpt,
            List<ManaAbilityOption> currentPool,
            int totalCount,
            int currentCount,
            List<ManaCostSymbol> cost,
            List<ManaAbilityOption> poolDependentOptions,
            int nextIndex,
            List<ManaAbilityOption> activatedOptions,
            SearchContext searchContext,
            Game game,
            Ability ability,
            UUID playerId,
            ManaCost manaCost) {

        // Base case: completed all activations
        if (currentCount == totalCount) {
            List<ManaAbilityOption> newActivatedOptions = new ArrayList<>(activatedOptions);
            for (int i = 0; i < totalCount; i++) {
                newActivatedOptions.add(poolDepOpt);
            }

            return enumeratePoolDependentRecursive(
                    cost, currentPool, poolDependentOptions,
                    nextIndex, newActivatedOptions, searchContext,
                    game, ability, playerId, manaCost);
        }

        // Enumerate all ways to pay the activation cost
        List<ManaCostSymbol> activationCost = poolDepOpt.getActivationCost();
        if (activationCost == null || activationCost.isEmpty()) {
            // Free activation - just calculate output and recurse
            List<ManaAbilityOption> expandedPool = activatePoolDependentSinglePayment(
                    poolDepOpt, currentPool, null, game, ability, playerId);
            if (expandedPool != null) {
                return activatePoolDependentWithEnumeration(
                        poolDepOpt, expandedPool, totalCount, currentCount + 1,
                        cost, poolDependentOptions, nextIndex,
                        activatedOptions, searchContext,
                        game, ability, playerId, manaCost);
            }
            return false;
        }

        // Enumerate different ways to pay the activation cost
        List<List<ManaAbilityOption>> paymentOptions = enumeratePaymentAllocations(
                activationCost, currentPool, game, ability);

        for (List<ManaAbilityOption> remainingPool : paymentOptions) {
            // Activate with this specific payment allocation
            List<ManaAbilityOption> expandedPool = activatePoolDependentSinglePayment(
                    poolDepOpt, currentPool, remainingPool, game, ability, playerId);

            if (expandedPool != null) {
                if (activatePoolDependentWithEnumeration(
                        poolDepOpt, expandedPool, totalCount, currentCount + 1,
                        cost, poolDependentOptions, nextIndex,
                        activatedOptions, searchContext,
                        game, ability, playerId, manaCost)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Builds all reachable remaining free-mana pools after paying for paid-ability activations.
     */
    private static List<List<ManaAbilityOption>> getPoolWithPaidActivations(List<ManaCostSymbol> activationCost,
                                                                            Set<UUID> spentFreeIds,
                                                                            List<ManaAbilityOption> freeOptions,
                                                                            ManaAbilityOption paid,
                                                                            Game game,
                                                                            int paidActivations,
                                                                            ManaCost manaCost,
                                                                            Map<String, List<List<ManaAbilityOption>>> paidActivationPoolCache,
                                                                            Map<String, List<ManaAbilityOption>> paidOutputCache,
                                                                            Map<String, Boolean> paidConditionCache,
                                                                            Map<UUID, Integer> activationCounts,
                                                                            List<ManaAbilityOption> selectedMixedOptions,
                                                                            List<ManaAbilityOption> allPaid) {
        String cacheKey = buildPaidActivationCacheKey(activationCost, spentFreeIds, freeOptions, paid, paidActivations);
        List<List<ManaAbilityOption>> cached = paidActivationPoolCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<List<ManaAbilityOption>> resultPools = new ArrayList<>();
        if (freeOptions.isEmpty() || activationCost.isEmpty()) {
            List<List<ManaAbilityOption>> result = List.of();
            paidActivationPoolCache.put(cacheKey, result);
            return result;
        }

        int totalCost = 0;
        for (ManaCostSymbol symbol : activationCost) {
            if (symbol.getType() != ManaCostSymbol.SymbolType.PHYREXIAN) {
                totalCost += symbol.minCost();
            }
        }
        totalCost *= paidActivations;

        int availableCapacity = 0;
        for (ManaAbilityOption opt : freeOptions) {
            if (!spentFreeIds.contains(opt.getOptionId())) {
                availableCapacity += opt.getCapacity();
            }
        }
        if (availableCapacity < totalCost) {
            List<List<ManaAbilityOption>> result = List.of();
            paidActivationPoolCache.put(cacheKey, result);
            return result;
        }

        Ability paidAbility = getAbility(paid, game);

        List<ManaAbilityOption> validFreeOptions = new ArrayList<>(freeOptions.size());
        List<ManaAbilityOption> carryForwardOptions = new ArrayList<>();
        UUID paidAbilityId = paidAbility != null ? paidAbility.getId() : null;
        Set<UUID> activeAbilityIds = collectActiveAbilityIds(
                activationCounts,
                freeOptions,
                selectedMixedOptions,
                allPaid,
                paid.getOptionId()
        );
        for (ManaAbilityOption option : freeOptions) {
            if (spentFreeIds.contains(option.getOptionId())) {
                continue;
            }

            if (option.isTriggeredMana() && !activeAbilityIds.contains(option.getTriggeringAbilityId())) {
                carryForwardOptions.add(option);
                continue;
            }

            String conditionKey = option.getOptionId() + "@" + (paidAbilityId != null ? paidAbilityId : "null");
            boolean conditionsMet = paidConditionCache.computeIfAbsent(
                    conditionKey,
                    k -> checkConditions(option, paidAbility, game, manaCost)
            );
            if (conditionsMet) {
                validFreeOptions.add(option);
            } else {
                carryForwardOptions.add(option);
            }
        }

        if (validFreeOptions.isEmpty()) {
            List<List<ManaAbilityOption>> result = List.of();
            paidActivationPoolCache.put(cacheKey, result);
            return result;
        }

        Set<String> seenPoolShapes = new HashSet<>();
        boolean genericOnlyActivationCost = activationCost.stream()
                .allMatch(symbol -> symbol.getType() == ManaCostSymbol.SymbolType.GENERIC);

        if (genericOnlyActivationCost) {
            Map<String, List<Integer>> byShape = new LinkedHashMap<>();
            for (int i = 0; i < validFreeOptions.size(); i++) {
                String shape = buildProducibleMapShapeKey(validFreeOptions.get(i));
                byShape.computeIfAbsent(shape, k -> new ArrayList<>()).add(i);
            }

            List<String> shapes = new ArrayList<>(byShape.keySet());
            for (String preserveShape : shapes) {
                List<String> prioritizedConsumers = new ArrayList<>();
                prioritizedConsumers.add(null);
                for (String shape : shapes) {
                    if (!shape.equals(preserveShape)) {
                        prioritizedConsumers.add(shape);
                    }
                }

                for (String prioritizedConsumeShape : prioritizedConsumers) {
                    int[] takeForOption = new int[validFreeOptions.size()];
                    int consumed = 0;

                    if (prioritizedConsumeShape != null) {
                        for (int idx : byShape.get(prioritizedConsumeShape)) {
                            if (consumed >= totalCost) {
                                break;
                            }
                            int take = Math.min(validFreeOptions.get(idx).getCapacity(), totalCost - consumed);
                            if (take > 0) {
                                takeForOption[idx] += take;
                                consumed += take;
                            }
                        }
                    }

                    for (Map.Entry<String, List<Integer>> entry : byShape.entrySet()) {
                        if (entry.getKey().equals(preserveShape) || entry.getKey().equals(prioritizedConsumeShape)) {
                            continue;
                        }
                        for (int idx : entry.getValue()) {
                            if (consumed >= totalCost) {
                                break;
                            }
                            int available = validFreeOptions.get(idx).getCapacity() - takeForOption[idx];
                            int take = Math.min(available, totalCost - consumed);
                            if (take > 0) {
                                takeForOption[idx] += take;
                                consumed += take;
                            }
                        }
                    }

                    for (int idx : byShape.get(preserveShape)) {
                        if (consumed >= totalCost) {
                            break;
                        }
                        int available = validFreeOptions.get(idx).getCapacity() - takeForOption[idx];
                        int take = Math.min(available, totalCost - consumed);
                        if (take > 0) {
                            takeForOption[idx] += take;
                            consumed += take;
                        }
                    }

                    if (consumed >= totalCost) {
                        List<ManaAbilityOption> finalOptions = new ArrayList<>(carryForwardOptions);
                        for (int i = 0; i < validFreeOptions.size(); i++) {
                            int remaining = validFreeOptions.get(i).getCapacity() - takeForOption[i];
                            if (remaining > 0) {
                                finalOptions.add(validFreeOptions.get(i).withCapacity(remaining));
                            }
                        }
                        if (!paid.isPoolDependent()) {
                            finalOptions.addAll(getCachedPaidOutput(paid, paidActivations, paidOutputCache));
                        }
                        String poolShape = buildCombinedCapacityProducibleMapKey(finalOptions);
                        if (seenPoolShapes.add(poolShape)) {
                            resultPools.add(finalOptions);
                        }
                    }
                }
            }
        } else {
            int maxIterations = Math.min(validFreeOptions.size(), 15);
            for (int skipIdx = 0; skipIdx < maxIterations; skipIdx++) {
                int[] takeForOption = new int[validFreeOptions.size()];
                int sum = 0;
                boolean success = true;

                for (ManaCostSymbol symbol : activationCost) {
                    int symbolNeed = symbol.minCost() * paidActivations;
                    int symbolGot = 0;

                    for (int offset = 0; offset < validFreeOptions.size() && symbolGot < symbolNeed; offset++) {
                        int freeIdx = (skipIdx + offset) % validFreeOptions.size();
                        ManaAbilityOption option = validFreeOptions.get(freeIdx);
                        int available = option.getCapacity() - takeForOption[freeIdx];
                        if (available > 0 && option.canProduce(symbol)) {
                            int take = Math.min(available, symbolNeed - symbolGot);
                            if (take > 0) {
                                takeForOption[freeIdx] += take;
                                symbolGot += take;
                                sum += take;
                            }
                        }
                    }
                    if (symbolGot < symbolNeed) {
                        success = false;
                        break;
                    }
                }

                if (success && sum >= totalCost) {
                    List<ManaAbilityOption> finalOptions = new ArrayList<>(carryForwardOptions);
                    for (int i = 0; i < validFreeOptions.size(); i++) {
                        int remaining = validFreeOptions.get(i).getCapacity() - takeForOption[i];
                        if (remaining > 0) {
                            finalOptions.add(validFreeOptions.get(i).withCapacity(remaining));
                        }
                    }
                    if (!paid.isPoolDependent()) {
                        finalOptions.addAll(getCachedPaidOutput(paid, paidActivations, paidOutputCache));
                    }
                    String poolShape = buildCombinedCapacityProducibleMapKey(finalOptions);
                    if (seenPoolShapes.add(poolShape)) {
                        resultPools.add(finalOptions);
                    }
                }
            }
        }

        List<List<ManaAbilityOption>> result = deepUnmodifiablePoolList(resultPools);
        paidActivationPoolCache.put(cacheKey, result);
        return result;
    }

    /**
     * Filters triggered mana out of pool unless its triggering ability is active in current plan.
     */
    private static List<ManaAbilityOption> filterTriggeredMana(List<ManaAbilityOption> freePool,
                                                               Map<UUID, Integer> activationCounts,
                                                               List<ManaAbilityOption> selectedMixedOptions,
                                                               List<ManaAbilityOption> paidOptions) {
        Set<UUID> activeAbilityIds = collectActiveAbilityIds(
                activationCounts,
                freePool,
                selectedMixedOptions,
                paidOptions,
                null
        );

        List<ManaAbilityOption> effectiveFreePool = new ArrayList<>();
        for (ManaAbilityOption opt : freePool) {
            if (!opt.isTriggeredMana()) {
                effectiveFreePool.add(opt);
            } else if (activeAbilityIds.contains(opt.getTriggeringAbilityId())) {
                effectiveFreePool.add(opt);
            }
        }

        return effectiveFreePool;
    }

    /**
     * Collects ability ids considered active for current branch.
     * Triggered mana uses this set to decide whether its parent ability fired.
     */
    private static Set<UUID> collectActiveAbilityIds(Map<UUID, Integer> activationCounts,
                                                     List<ManaAbilityOption> freePool,
                                                     List<ManaAbilityOption> selectedMixedOptions,
                                                     List<ManaAbilityOption> paidOptions,
                                                     UUID excludedPaidOptionId) {
        Set<UUID> activeAbilityIds = new HashSet<>();

        for (ManaAbilityOption opt : selectedMixedOptions) {
            activeAbilityIds.add(opt.getAbilityId());
        }

        for (ManaAbilityOption opt : freePool) {
            if (!opt.isTriggeredMana()) {
                activeAbilityIds.add(opt.getAbilityId());
            }
        }

        for (Map.Entry<UUID, Integer> entry : activationCounts.entrySet()) {
            if (entry.getValue() <= 0 || entry.getKey().equals(excludedPaidOptionId)) {
                continue;
            }
            for (ManaAbilityOption opt : paidOptions) {
                if (opt.getOptionId().equals(entry.getKey())) {
                    activeAbilityIds.add(opt.getAbilityId());
                    break;
                }
            }
        }

        return activeAbilityIds;
    }

    /**
     * Computes which free-option ids become fully spent after moving from one pool snapshot to another.
     */
    private static Set<UUID> buildNextSpentFreeIds(Set<UUID> spentFreeIds,
                                                   List<ManaAbilityOption> currentFreePool,
                                                   List<ManaAbilityOption> remainingPool) {
        Set<UUID> nextSpentFreeIds = new HashSet<>(spentFreeIds);
        Map<UUID, Integer> remainingByOptionId = new HashMap<>();

        for (ManaAbilityOption option : remainingPool) {
            remainingByOptionId.merge(option.getOptionId(), option.getCapacity(), Integer::sum);
        }

        for (ManaAbilityOption option : currentFreePool) {
            if (remainingByOptionId.getOrDefault(option.getOptionId(), 0) <= 0) {
                nextSpentFreeIds.add(option.getOptionId());
            }
        }

        return nextSpentFreeIds;
    }

    /**
     * Enumerates distinct remaining pools after paying activation cost from current pool.
     */
    private static List<List<ManaAbilityOption>> enumeratePaymentAllocations(List<ManaCostSymbol> activationCost,
                                                                             List<ManaAbilityOption> currentPool,
                                                                             Game game,
                                                                             Ability ability) {
        if (!checkFlow(activationCost, currentPool, game, ability)) {
            return Collections.emptyList();
        }

        List<ManaCostSymbol> expandedUnits = expandCostToUnitSymbols(activationCost);
        if (expandedUnits.isEmpty()) {
            return List.of(List.copyOf(currentPool));
        }

        expandedUnits.sort((a, b) -> {
            int leftRank = symbolRestrictivenessRank(a);
            int rightRank = symbolRestrictivenessRank(b);
            if (leftRank != rightRank) {
                return Integer.compare(leftRank, rightRank);
            }
            return Integer.compare(a.getColorOptions().size(), b.getColorOptions().size());
        });

        int[] remainingByOption = new int[currentPool.size()];
        for (int i = 0; i < currentPool.size(); i++) {
            remainingByOption[i] = currentPool.get(i).getCapacity();
        }

        List<List<ManaAbilityOption>> results = new ArrayList<>();
        Set<String> seenShapes = new HashSet<>();
        enumeratePaymentAllocationsRecursive(
                expandedUnits,
                0,
                0,
                currentPool,
                remainingByOption,
                results,
                seenShapes);
        return deepUnmodifiablePoolList(results);
    }

    /**
     * Expands multi-unit mana symbols into one-unit symbols so payment-allocation search can branch per unit.
     */
    private static List<ManaCostSymbol> expandCostToUnitSymbols(List<ManaCostSymbol> activationCost) {
        List<ManaCostSymbol> result = new ArrayList<>();
        for (ManaCostSymbol symbol : activationCost) {
            if (symbol.getType() == ManaCostSymbol.SymbolType.PHYREXIAN) {
                continue;
            }
            int units = symbol.minCost();
            if (units <= 0) {
                continue;
            }
            if (symbol.getType() == ManaCostSymbol.SymbolType.GENERIC) {
                for (int i = 0; i < units; i++) {
                    result.add(ManaCostSymbol.generic(1));
                }
            } else {
                for (int i = 0; i < units; i++) {
                    result.add(symbol);
                }
            }
        }
        return result;
    }

    /**
     * Ranks symbols from most restrictive to least restrictive so recursion prunes sooner.
     */
    private static int symbolRestrictivenessRank(ManaCostSymbol symbol) {
        return switch (symbol.getType()) {
            case MONO_COLOR, COLORLESS -> 0;
            case HYBRID_COLOR, PHYREXIAN -> 1;
            case HYBRID_GENERIC -> 2;
            case GENERIC -> 3;
        };
    }

    /**
     * Recursively assigns one-unit cost symbols to pool options.
     */
    private static void enumeratePaymentAllocationsRecursive(List<ManaCostSymbol> expandedUnits,
                                                             int symbolIdx,
                                                             int minOptionIdx,
                                                             List<ManaAbilityOption> currentPool,
                                                             int[] remainingByOption,
                                                             List<List<ManaAbilityOption>> results,
                                                             Set<String> seenShapes) {
        if (results.size() >= MAX_PAYMENT_ALLOCATION_RESULTS) {
            return;
        }

        if (symbolIdx == expandedUnits.size()) {
            List<ManaAbilityOption> remainingPool = buildRemainingPool(currentPool, remainingByOption);
            String poolShape = buildCombinedCapacityProducibleMapKey(remainingPool);
            if (seenShapes.add(poolShape)) {
                results.add(remainingPool);
            }
            return;
        }

        ManaCostSymbol symbol = expandedUnits.get(symbolIdx);
        boolean nextSame = symbolIdx + 1 < expandedUnits.size()
                && isSameCostUnit(symbol, expandedUnits.get(symbolIdx + 1));

        for (int optionIdx = minOptionIdx; optionIdx < currentPool.size(); optionIdx++) {
            if (remainingByOption[optionIdx] <= 0) {
                continue;
            }

            ManaAbilityOption option = currentPool.get(optionIdx);
            if (!canPayUnitWithOption(symbol, option)) {
                continue;
            }

            remainingByOption[optionIdx]--;
            enumeratePaymentAllocationsRecursive(
                    expandedUnits,
                    symbolIdx + 1,
                    nextSame ? optionIdx : 0,
                    currentPool,
                    remainingByOption,
                    results,
                    seenShapes
            );
            remainingByOption[optionIdx]++;

            if (results.size() >= MAX_PAYMENT_ALLOCATION_RESULTS) {
                return;
            }
        }
    }

    /**
     * Checks whether one option can satisfy one expanded cost unit.
     */
    private static boolean canPayUnitWithOption(ManaCostSymbol symbol, ManaAbilityOption option) {
        if (symbol.getType() == ManaCostSymbol.SymbolType.GENERIC) {
            return option.getCapacity() > 0;
        }
        return option.canProduce(symbol);
    }

    /**
     * Detects whether two expanded cost units are interchangeable for symmetry pruning.
     */
    private static boolean isSameCostUnit(ManaCostSymbol left, ManaCostSymbol right) {
        return left.getType() == right.getType()
                && left.getGenericCost() == right.getGenericCost()
                && left.getColorOptions().equals(right.getColorOptions());
    }

    /**
     * Rebuilds pool snapshot from original options and updated per-option remaining capacity.
     */
    private static List<ManaAbilityOption> buildRemainingPool(List<ManaAbilityOption> currentPool, int[] remainingByOption) {
        List<ManaAbilityOption> remainingPool = new ArrayList<>();
        for (int i = 0; i < currentPool.size(); i++) {
            int remaining = remainingByOption[i];
            if (remaining <= 0) {
                continue;
            }

            ManaAbilityOption option = currentPool.get(i);
            if (remaining == option.getCapacity()) {
                remainingPool.add(option);
            } else {
                remainingPool.add(option.withCapacity(remaining));
            }
        }
        return remainingPool;
    }

    /**
     * Converts a Mana object to a list of ManaAbilityOptions.
     */
    private static boolean solveWithHybrids(List<ManaCostSymbol> cost,
                                            List<ManaAbilityOption> options,
                                            Game game,
                                            Ability ability) {
        List<Integer> hybridIndices = new ArrayList<>();
        for (int i = 0; i < cost.size(); i++) {
            if (cost.get(i).getType() == ManaCostSymbol.SymbolType.HYBRID_GENERIC) {
                hybridIndices.add(i);
            }
        }

        if (hybridIndices.isEmpty()) {
            return checkFlow(cost, options, game, ability);
        }

        int n = hybridIndices.size();
        int combinations = 1 << n;
        for (int combo = 0; combo < combinations; combo++) {
            List<ManaCostSymbol> resolved = new ArrayList<>(cost);
            for (int hi = 0; hi < n; hi++) {
                int idx = hybridIndices.get(hi);
                int choice = (combo >> hi) & 1;
                resolved.set(idx, resolveHybrid(cost.get(idx), choice));
            }
            if (checkFlow(resolved, options, game, ability)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolves one hybrid symbol into one concrete alternative.
     *
     * @param hybrid hybrid symbol
     * @param choice 0 = first option, 1 = second option
     */
    private static ManaCostSymbol resolveHybrid(ManaCostSymbol hybrid, int choice) {
        List<ManaType> colors = new ArrayList<>(hybrid.getColorOptions());
        switch (hybrid.getType()) {
            case HYBRID_COLOR:
                return ManaCostSymbol.monoColor(colors.get(choice % colors.size()));
            case HYBRID_GENERIC:
                if (choice == 0) {
                    return ManaCostSymbol.monoColor(colors.getFirst());
                } else {
                    return ManaCostSymbol.generic(hybrid.getGenericCost());
                }
            default:
                return hybrid;
        }
    }

    static boolean checkFlow(List<ManaCostSymbol> costSymbols,
                             List<ManaAbilityOption> availableOptions,
                             Game game,
                             Ability ability) {
        if (costSymbols.isEmpty()) {
            return true;
        }

        int totalCost = costSymbols.stream()
                .mapToInt(ManaCostSymbol::minCost)
                .sum();
        if (totalCost == 0) {
            return true;
        }
        if (availableOptions.isEmpty()) {
            return false;
        }

        SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
                new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        final Integer source = -1;
        final Integer sink = -2;
        graph.addVertex(source);
        graph.addVertex(sink);

        for (int i = 0; i < costSymbols.size(); i++) {
            ManaCostSymbol symbol = costSymbols.get(i);
            int cap = symbol.minCost();
            if (cap > 0) {
                graph.addVertex(i);
                addOrAccumulateEdge(graph, i, sink, cap);
            }
        }

        int sourceOffset = costSymbols.size();
        for (int j = 0; j < availableOptions.size(); j++) {
            ManaAbilityOption option = availableOptions.get(j);
            Integer sourceNode = sourceOffset + j;

            graph.addVertex(sourceNode);
            addOrAccumulateEdge(graph, source, sourceNode, option.getCapacity());

            for (int i = 0; i < costSymbols.size(); i++) {
                ManaCostSymbol symbol = costSymbols.get(i);
                int edgeCap = capacityBetween(symbol, option, game, ability);
                if (edgeCap > 0) {
                    addOrAccumulateEdge(graph, sourceNode, i, edgeCap);
                }
            }
        }

        DinicMFImpl<Integer, DefaultWeightedEdge> dinic = new DinicMFImpl<>(graph);
        double maxFlow = dinic.getMaximumFlowValue(source, sink);
        return (int) Math.round(maxFlow) >= totalCost;
    }

    /**
     * Adds edge from {@code u} to {@code v}, or accumulates capacity if edge already exists.
     */
    private static void addOrAccumulateEdge(SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph,
                                            Integer u,
                                            Integer v,
                                            int cap) {
        DefaultWeightedEdge existing = graph.getEdge(u, v);
        if (existing != null) {
            graph.setEdgeWeight(existing, graph.getEdgeWeight(existing) + cap);
        } else {
            DefaultWeightedEdge edge = graph.addEdge(u, v);
            graph.setEdgeWeight(edge, cap);
        }
    }

    /**
     * Returns maximum flow capacity from one cost symbol into one source option.
     * When game and ability are present, also applies AsThoughEffects.
     */
    private static int capacityBetween(ManaCostSymbol symbol, ManaAbilityOption option, Game game, Ability ability) {
        if (option.isProducesAny()) {
            return symbol.minCost();
        }
        if (game == null || ability == null) {
            return capacityBetween(symbol, option);
        }

        Set<ManaType> spendableTypes = getSpendableTypes(symbol.getColorOptions(), option, game, ability);
        return capacityBetween(symbol, option, spendableTypes);
    }

    /**
     * Fast-path capacity check when AsThoughEffects are irrelevant.
     */
    private static int capacityBetween(ManaCostSymbol symbol, ManaAbilityOption option) {
        if (option.isProducesAny()) {
            return option.getCapacity();
        }
        return capacityBetween(symbol, option, option.getProducibleTypes());
    }

    /**
     * Computes capacity using already-resolved set of spendable mana types.
     */
    private static int capacityBetween(ManaCostSymbol symbol, ManaAbilityOption option, Set<ManaType> producibleTypes) {
        return switch (symbol.getType()) {
            case MONO_COLOR, HYBRID_GENERIC -> {
                ManaType color = symbol.getColorOptions().iterator().next();
                yield producibleTypes.contains(color) ? option.getCapacity() : 0;
            }
            case HYBRID_COLOR, PHYREXIAN -> {
                for (ManaType color : symbol.getColorOptions()) {
                    if (producibleTypes.contains(color)) {
                        yield option.getCapacity();
                    }
                }
                yield 0;
            }
            case COLORLESS -> producibleTypes.contains(ManaType.COLORLESS) ? option.getCapacity() : 0;
            case GENERIC -> option.getCapacity();
        };
    }

    /**
     * Gets mana types this option may spend as, including AsThoughEffects remapping.
     */
    private static Set<ManaType> getSpendableTypes(Set<ManaType> colorOptions, ManaAbilityOption option, Game game, Ability ability) {
        EnumSet<ManaType> spendableTypes = EnumSet.copyOf(option.getProducibleTypes());

        if (ability == null) {
            return spendableTypes;
        }

        UUID controllerId = ability.getControllerId();
        UUID objectId = ability.getSourceId() != null ? ability.getSourceId() : option.getAbilityId();

        for (ManaType colorOption : colorOptions) {
            for (ManaType manaType : option.getProducibleTypes()) {
                ManaPoolItem manaItem = new ManaPoolItem(0, 0, 0, 0, 0, 0, game.getObject(option.getSourceId()), option.getAbilityId(), false);
                manaItem.add(manaType, 1);
                ManaType asThoughType = game.getContinuousEffects().asThoughMana(
                        colorOption, manaItem, objectId, ability, controllerId, game);
                if (asThoughType == manaType) {
                    spendableTypes.add(colorOption);
                }
            }
        }

        return spendableTypes;
    }

    /**
     * Activates pool-dependent ability once and returns expanded pool snapshot.
     * Remaining pool may be supplied when activation already paid for.
     */
    private static List<ManaAbilityOption> activatePoolDependentSinglePayment(ManaAbilityOption poolDepOpt,
                                                                              List<ManaAbilityOption> currentPool,
                                                                              List<ManaAbilityOption> remainingPool,
                                                                              Game game,
                                                                              Ability ability,
                                                                              UUID playerId) {
        List<ManaAbilityOption> basePool = remainingPool != null ? remainingPool : currentPool;

        Mana poolMana = new Mana();
        for (ManaAbilityOption opt : basePool) {
            if (opt.getCapacity() > 0) {
                Map<ManaType, Integer> producibleMap = opt.getProducibleMap();
                if (!producibleMap.isEmpty()) {
                    ManaType firstType = producibleMap.keySet().iterator().next();
                    Integer amount = producibleMap.get(firstType);
                    if (amount != null && amount > 0) {
                        poolMana.add(new Mana(firstType, amount * opt.getCapacity()));
                    } else {
                        poolMana.add(new Mana(firstType, opt.getCapacity()));
                    }
                }
            }
        }

        Ability poolDepAbility = getAbility(poolDepOpt, game);
        if (!(poolDepAbility instanceof ActivatedManaAbilityImpl manaAbility)) {
            return null;
        }

        List<Mana> outputManas = manaAbility.getNetMana(game, poolMana);
        if (outputManas.isEmpty()) {
            return basePool;
        }

        List<ManaAbilityOption> newPool = new ArrayList<>(basePool);
        for (Mana mana : outputManas) {
            List<ManaAbilityOption> manaOptions = convertManaToOptions(mana, poolDepOpt.getAbilityId());
            newPool.addAll(manaOptions);
        }

        return newPool;
    }

    /**
     * Converts concrete mana object into synthetic free mana options.
     */
    private static List<ManaAbilityOption> convertManaToOptions(Mana mana, UUID sourceAbilityId) {
        List<ManaAbilityOption> options = new ArrayList<>();

        if (mana.getWhite() > 0) {
            options.add(ManaAbilityOption.createSyntheticOption(sourceAbilityId, ManaType.WHITE, mana.getWhite()));
        }
        if (mana.getBlue() > 0) {
            options.add(ManaAbilityOption.createSyntheticOption(sourceAbilityId, ManaType.BLUE, mana.getBlue()));
        }
        if (mana.getBlack() > 0) {
            options.add(ManaAbilityOption.createSyntheticOption(sourceAbilityId, ManaType.BLACK, mana.getBlack()));
        }
        if (mana.getRed() > 0) {
            options.add(ManaAbilityOption.createSyntheticOption(sourceAbilityId, ManaType.RED, mana.getRed()));
        }
        if (mana.getGreen() > 0) {
            options.add(ManaAbilityOption.createSyntheticOption(sourceAbilityId, ManaType.GREEN, mana.getGreen()));
        }
        if (mana.getColorless() > 0) {
            options.add(ManaAbilityOption.createSyntheticOption(sourceAbilityId, ManaType.COLORLESS, mana.getColorless()));
        }
        if (mana.getGeneric() > 0) {
            options.add(ManaAbilityOption.createSyntheticOption(sourceAbilityId, ManaType.GENERIC, mana.getGeneric()));
        }

        return options;
    }

    /**
     * Converts paid ability output into synthetic free options representing mana produced across activations.
     */
    private static List<ManaAbilityOption> convertPaidOutputToFree(ManaAbilityOption paid, int activations) {
        if (activations <= 0) {
            return Collections.emptyList();
        }

        List<ManaAbilityOption> freeOptions = new ArrayList<>();
        if (paid.isProducesAny()) {
            int totalCapacity = paid.getCapacity() * activations;
            ManaAbilityOption anyOption = ManaAbilityOption.builder()
                    .abilityId(paid.getAbilityId())
                    .capacity(totalCapacity)
                    .producesAny(true)
                    .build();
            freeOptions.add(anyOption);
            return freeOptions;
        }

        Map<ManaType, Integer> totalByType = getManaTypeIntegerMap(paid, activations);
        boolean allStatic = totalByType.values().stream().allMatch(v -> v > 0);
        int totalCapacity = paid.getCapacity() * activations;

        if (allStatic) {
            for (Map.Entry<ManaType, Integer> entry : totalByType.entrySet()) {
                if (entry.getValue() > 0) {
                    freeOptions.add(ManaAbilityOption.createSyntheticOption(
                            paid.getAbilityId(),
                            entry.getKey(),
                            entry.getValue()
                    ));
                }
            }
        } else {
            ManaAbilityOption flexOption = ManaAbilityOption.builder()
                    .abilityId(paid.getAbilityId())
                    .capacity(totalCapacity)
                    .producibleMap(totalByType)
                    .build();
            freeOptions.add(flexOption);
        }

        return freeOptions;
    }

    /**
     * Builds aggregate producible-map payload for synthetic paid-output options.
     * Zero values mean flexible distribution rather than fixed amount.
     */
    private static @NonNull Map<ManaType, Integer> getManaTypeIntegerMap(ManaAbilityOption paid, int activations) {
        Map<ManaType, Integer> totalByType = new EnumMap<>(ManaType.class);
        for (Map.Entry<ManaType, Integer> entry : paid.getProducibleMap().entrySet()) {
            ManaType type = entry.getKey();
            int amountPerActivation = entry.getValue();
            if (amountPerActivation > 0) {
                totalByType.put(type, amountPerActivation * activations);
            } else {
                totalByType.put(type, 0);
            }
        }
        return totalByType;
    }

    /**
     * Memoizes paid-output expansion because same paid option/count pair appears in many branches.
     */
    private static List<ManaAbilityOption> getCachedPaidOutput(ManaAbilityOption paid,
                                                               int paidActivations,
                                                               Map<String, List<ManaAbilityOption>> paidOutputCache) {
        String key = paid.getOptionId() + "#" + paidActivations;
        return paidOutputCache.computeIfAbsent(
                key,
                k -> List.copyOf(convertPaidOutputToFree(paid, paidActivations))
        );
    }

    /**
     * Resolves concrete ability instance for option when game state is available.
     */
    private static Ability getAbility(ManaAbilityOption option, Game game) {
        if (game == null || option == null) {
            return null;
        }
        return game.getAbility(option.getAbilityId(), option.getSourceId()).orElse(null);
    }

    /**
     * Namespaces failed-state memoization by exact paid-option set.
     */
    private static String buildAllPaidKey(List<ManaAbilityOption> allPaid) {
        if (allPaid.isEmpty()) {
            return "empty";
        }
        StringJoiner joiner = new StringJoiner(",");
        for (ManaAbilityOption opt : allPaid) {
            joiner.add(opt.getOptionId().toString());
        }
        return joiner.toString();
    }

    /**
     * Builds cache key for one paid-activation payment problem.
     * Key includes activation cost, already-spent options, current free pool, paid option, and activation count.
     */
    private static String buildPaidActivationCacheKey(List<ManaCostSymbol> activationCost,
                                                      Set<UUID> spentFreeIds,
                                                      List<ManaAbilityOption> freeOptions,
                                                      ManaAbilityOption paid,
                                                      int paidActivations) {
        return paid.getOptionId()
                + "@" + paidActivations
                + "|ac=" + buildActivationCostKey(activationCost)
                + "|spent=" + buildUuidSetKey(spentFreeIds)
                + "|pool=" + buildExactPoolStateKey(freeOptions);
    }

    /**
     * Builds failed-state memoization key for recursive paid-activation search.
     */
    private static String buildPaidActivationStateKey(String allPaidKey,
                                                      int index,
                                                      List<ManaAbilityOption> currentFreePool,
                                                      Set<UUID> spentFreeIds,
                                                      Map<UUID, Integer> activationCounts,
                                                      List<ManaAbilityOption> allPaid) {
        return allPaidKey
                + "|idx=" + index
                + "|pool=" + buildExactPoolStateKey(currentFreePool)
                + "|spent=" + buildUuidSetKey(spentFreeIds)
                + "|counts=" + buildActivationCountsKey(activationCounts, allPaid);
    }

    /**
     * Freezes nested pool lists before caching so recursive callers cannot mutate shared results.
     */
    private static List<List<ManaAbilityOption>> deepUnmodifiablePoolList(List<List<ManaAbilityOption>> pools) {
        if (pools.isEmpty()) {
            return List.of();
        }
        List<List<ManaAbilityOption>> copy = new ArrayList<>(pools.size());
        for (List<ManaAbilityOption> pool : pools) {
            copy.add(List.copyOf(pool));
        }
        return List.copyOf(copy);
    }

    /**
     * Serializes activation-cost shape for caches.
     */
    private static String buildActivationCostKey(List<ManaCostSymbol> activationCost) {
        StringJoiner joiner = new StringJoiner("|");
        for (ManaCostSymbol symbol : activationCost) {
            joiner.add(symbol.getType().name()
                    + "#" + symbol.getGenericCost()
                    + "#" + symbol.getColorOptions()
                    + "#" + symbol.minCost());
        }
        return joiner.toString();
    }

    /**
     * Serializes UUID set in stable order so cache keys remain deterministic.
     */
    private static String buildUuidSetKey(Set<UUID> ids) {
        if (ids.isEmpty()) {
            return "";
        }
        List<String> orderedIds = new ArrayList<>(ids.size());
        for (UUID id : ids) {
            orderedIds.add(id.toString());
        }
        Collections.sort(orderedIds);
        return String.join(",", orderedIds);
    }

    /**
     * Serializes activation counts in order of {@code allPaid} so equivalent maps produce same key.
     */
    private static String buildActivationCountsKey(Map<UUID, Integer> activationCounts,
                                                   List<ManaAbilityOption> allPaid) {
        if (allPaid.isEmpty()) {
            return "";
        }

        StringJoiner joiner = new StringJoiner(",");
        for (ManaAbilityOption option : allPaid) {
            joiner.add(option.getOptionId() + "=" + activationCounts.getOrDefault(option.getOptionId(), 0));
        }
        return joiner.toString();
    }

    /**
     * Serializes exact pool state, keeping option ids and capacities distinct.
     */
    private static String buildExactPoolStateKey(List<ManaAbilityOption> options) {
        if (options.isEmpty()) {
            return "";
        }

        List<String> optionStates = new ArrayList<>(options.size());
        for (ManaAbilityOption option : options) {
            optionStates.add(option.getOptionId()
                    + "#" + option.getCapacity()
                    + "#" + buildProducibleMapShapeKey(option));
        }
        Collections.sort(optionStates);
        return String.join("|", optionStates);
    }

    /**
     * Canonical pool key used to deduplicate equivalent paid-activation outputs.
     * Pools are equal when they have same combined capacity per producible-map shape.
     */
    private static String buildCombinedCapacityProducibleMapKey(List<ManaAbilityOption> options) {
        Map<String, Integer> combinedByShape = new TreeMap<>();
        for (ManaAbilityOption option : options) {
            String shapeKey = buildProducibleMapShapeKey(option);
            combinedByShape.merge(shapeKey, option.getCapacity(), Integer::sum);
        }

        StringJoiner joiner = new StringJoiner("|");
        for (Map.Entry<String, Integer> entry : combinedByShape.entrySet()) {
            joiner.add(entry.getKey() + "#" + entry.getValue());
        }
        return joiner.toString();
    }

    /**
     * Serializes producible-map shape only, ignoring option identity and total count.
     */
    private static String buildProducibleMapShapeKey(ManaAbilityOption option) {
        if (option.isProducesAny()) {
            return "ANY";
        }

        StringJoiner joiner = new StringJoiner(",");
        for (ManaType manaType : ManaType.values()) {
            Integer amount = option.getProducibleMap().get(manaType);
            if (amount != null) {
                joiner.add(manaType.name() + "=" + amount);
            }
        }
        return joiner.length() > 0 ? joiner.toString() : "EMPTY";
    }
}
