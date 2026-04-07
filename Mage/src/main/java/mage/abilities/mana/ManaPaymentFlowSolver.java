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
 * Uses Dinic's max-flow approach to check possible payments
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

    private ManaPaymentFlowSolver() {
    }

    /**
     * The result of a successful mana-payment feasibility search.
     * Captures the exact {@link ManaAbilityOption} choices that must be activated
     * to pay the target mana cost.
     *
     * @param selectedMixedOptions one chosen option from each mixed source node (may be empty)
     * @param selectedPaidOptions all activated paid options from pure-paid nodes and funded
     *                            paid options from mixed nodes (may include options already in
     *                            {@code selectedMixedOptions} — deduplicate by {@code abilityId})
     */
    public record ManaPaymentPlan(
            List<ManaAbilityOption> selectedMixedOptions,
            List<ManaAbilityOption> selectedPaidOptions
    ) {
        private static final ManaPaymentPlan EMPTY = new ManaPaymentPlan(List.of(), List.of());

        public static ManaPaymentPlan empty() {
            return EMPTY;
        }

        /**
         * Returns all ability IDs that must be activated, in activation order:
         * paid abilities first (to fund their output), then mixed selections.
         */
        public List<UUID> activationOrder() {
            List<UUID> result = new ArrayList<>();
            for (ManaAbilityOption opt : selectedPaidOptions) {
                result.add(opt.getAbilityId());
            }
            for (ManaAbilityOption opt : selectedMixedOptions) {
                if (!opt.hasCost()) {
                    result.add(opt.getAbilityId());
                }
            }
            return result;
        }
    }

    /**
     * Returns a payment plan if the given cost can be paid from the given sources,
     * or {@code null} otherwise.
     * Checks conditions on mana options and AsThoughEffects when game and ability are provided.
     *
     * @param cost    parsed mana cost symbols to pay
     * @param sources available mana source nodes
     * @param game    the game state (for AsThoughEffects and conditions); may be {@code null}
     * @param ability the ability being paid for (for AsThoughEffects); may be {@code null}
     */
    public static ManaPaymentPlan findPaymentPlan(List<ManaCostSymbol> cost,
                                                  List<ManaSourceNode> sources,
                                                  Game game,
                                                  Ability ability) {
        UUID playerId = ability != null ? ability.getControllerId() : null;
        ManaCost manaCost = ability != null ? ability.getManaCostsToPay() : null;
        return findPaymentPlan(cost, sources, game, ability, playerId, manaCost);
    }

    /**
     * Returns a payment plan if the given cost can be paid from the given sources,
     * or {@code null} otherwise.
     * Checks conditions on mana options and AsThoughEffects when game and ability are provided.
     *
     * @param cost    parsed mana cost symbols to pay
     * @param sources available mana source nodes
     * @param game    the game state (for AsThoughEffects and conditions); may be {@code null}
     * @param ability the ability being paid for (for AsThoughEffects); may be {@code null}
     * @param playerId the player paying the cost; may be {@code null}
     * @param manaCost the full mana cost being paid; may be {@code null}
     */
    public static ManaPaymentPlan findPaymentPlan(List<ManaCostSymbol> cost,
                                                  List<ManaSourceNode> sources,
                                                  Game game,
                                                  Ability ability,
                                                  UUID playerId,
                                                  ManaCost manaCost) {
        if (cost.isEmpty()) {
            return ManaPaymentPlan.empty();
        }
        if (sources.isEmpty()) {
            return cost.stream().allMatch(s -> s.minCost() == 0)
                    ? ManaPaymentPlan.empty()
                    : null;
        }

        List<ManaSourceNode> pureFree = new ArrayList<>();
        List<ManaSourceNode> purePaid = new ArrayList<>();
        List<ManaSourceNode> mixed = new ArrayList<>();

        for (ManaSourceNode node : sources) {
            if (node.isMixed()) {
                mixed.add(node);
            } else if (node.isAllPaid()) {
                purePaid.add(node);
            } else if (node.hasSingleOption()) {
                // Single free option — add directly to the pool (fast path).
                pureFree.add(node);
            } else {
                // Multiple free options (e.g. UU-or-WW): exactly one may be chosen
                // per activation, so enumerate them alongside mixed nodes.
                mixed.add(node);
            }
        }

        List<ManaAbilityOption> freePool = new ArrayList<>();
        for (ManaSourceNode node : pureFree) {
            ManaAbilityOption option = node.getSingleOption();
            if (game == null || ability == null || checkConditions(option, ability, game, playerId, manaCost)) {
                freePool.add(option);
            }
        }

        Accumulator accumulator = new Accumulator();
        accumulator.purePaid = purePaid;
        accumulator.freePool = freePool;

        if (enumerateMixedSelections(cost, mixed, game, ability, playerId, manaCost, 0, accumulator)) {
            return new ManaPaymentPlan(
                    List.copyOf(accumulator.selectedMixedOptions),
                    List.copyOf(accumulator.selectedPaidOptions)
            );
        }
        return null;
    }

    /**
     * Returns {@code true} if the given cost can be paid from the given sources.
     * Does not check conditions or AsThoughEffects.
     *
     * @param cost    list of parsed mana cost symbols
     * @param sources available mana source nodes
     */
    public static boolean canPay(List<ManaCostSymbol> cost, List<ManaSourceNode> sources) {
        return canPay(cost, sources, (Game) null, (Ability) null, (UUID) null, (ManaCost) null);
    }

    /**
     * Returns {@code true} if the given cost can be paid from the given sources.
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
     * @param game     the game state (for AsThoughEffects and conditions)
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
                // Multi-option free: enumerate to enforce "choose one per activation".
                mixed.add(node);
            }
        }

        List<ManaAbilityOption> freePool = new ArrayList<>();
        for (ManaSourceNode node : pureFree) {
            ManaAbilityOption option = node.getSingleOption();
            if (game == null || ability == null || checkConditions(option, ability, game, playerId, manaCost)) {
                freePool.add(option);
            }
        }

        Accumulator acc = new Accumulator();
        acc.purePaid = purePaid;
        acc.freePool = freePool;
        return enumerateMixedSelections(cost, mixed, game, ability, playerId, manaCost, 0, acc);
    }

    private static class Accumulator {
        List<ManaSourceNode> purePaid = new ArrayList<>();
        List<ManaAbilityOption> freePool = new ArrayList<>();
        final List<ManaAbilityOption> selectedMixedOptions = new ArrayList<>();
        final List<ManaAbilityOption> selectedPaidOptions = new ArrayList<>();
    }

    private static boolean checkConditions(ManaAbilityOption option, Ability ability, Game game, UUID playerId, ManaCost manaCost) {
        if (!option.hasConditions() || game == null || ability == null) {
            return true;
        }
        // Use the provided playerId if available, otherwise fall back to ability controller
        UUID effectivePlayerId = playerId != null ? playerId : ability.getControllerId();
        return option.applyConditions(ability, game, effectivePlayerId, manaCost);
    }

    /**
     * Recursively tries each ability option on each mixed node (those with both
     * free and paid abilities). At the leaf, delegates to paid-funding enumeration.
     * Populates {@code accumulator.selectedMixedOptions} and
     * {@code accumulator.selectedPaidOptions} on success.
     */
    private static boolean enumerateMixedSelections(List<ManaCostSymbol> cost,
                                                     List<ManaSourceNode> mixed,
                                                     Game game,
                                                     Ability ability,
                                                     UUID playerId,
                                                     ManaCost manaCost,
                                                     int idx,
                                                     Accumulator accumulator) {
        if (idx == mixed.size()) {
            return enumeratePaidFunding(cost, accumulator, game, ability, playerId, manaCost);
        }

        ManaSourceNode node = mixed.get(idx);
        for (ManaAbilityOption option : node.getAbilityOptions()) {
            if (game != null && ability != null && !checkConditions(option, ability, game, playerId, manaCost)) {
                continue;
            }
            accumulator.selectedMixedOptions.add(option);
            if (enumerateMixedSelections(cost, mixed, game, ability, playerId, manaCost, idx + 1, accumulator)) {
                return true;
            }
            accumulator.selectedMixedOptions.removeLast();
        }
        return false;
    }

    /**
     * Enumerates subsets of paid mana abilities to activate, verifies each subset
     * can be funded, then runs the flow solver on the remaining available mana.
     * Populates {@code accumulator.selectedPaidOptions} on success.
     */
    private static boolean enumeratePaidFunding(List<ManaCostSymbol> cost, Accumulator accumulator, Game game, Ability ability, UUID playerId, ManaCost manaCost) {
        List<ManaAbilityOption> freeMixed = new ArrayList<>();
        List<ManaAbilityOption> paidMixed = new ArrayList<>();
        for (ManaAbilityOption opt : accumulator.selectedMixedOptions) {
            if (opt.hasCost()) {
                paidMixed.add(opt);
            } else {
                freeMixed.add(opt);
            }
        }

        List<ManaAbilityOption> allFree = new ArrayList<>(accumulator.freePool);
        allFree.addAll(freeMixed);

        // Separate pool-dependent abilities (e.g., Doubling Cube) from normal abilities
        List<ManaAbilityOption> normalFree = new ArrayList<>();
        List<ManaAbilityOption> poolDependent = new ArrayList<>();
        for (ManaAbilityOption opt : allFree) {
            if (opt.isPoolDependent()) {
                poolDependent.add(opt);
            } else {
                normalFree.add(opt);
            }
        }

        List<ManaAbilityOption> allPaid = new ArrayList<>();
        for (ManaSourceNode node : accumulator.purePaid) {
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
                .mapToInt(ManaCostSymbol::minCost).sum()));

        // Pass both normal and pool-dependent lists to the enumeration
        return enumeratePaidActivations(cost, normalFree, normalPaid, poolDependent, poolDependentPaid, 0, Collections.emptySet(), new HashMap<>(), normalFree, accumulator, game, ability, playerId, manaCost);
    }

    private static boolean enumeratePaidActivations(List<ManaCostSymbol> cost,
                                                    List<ManaAbilityOption> allFree,
                                                    List<ManaAbilityOption> allPaid,
                                                    List<ManaAbilityOption> poolDependentFree,
                                                    List<ManaAbilityOption> poolDependentPaid,
                                                    int index,
                                                    Set<UUID> spentFreeIds,
                                                    Map<UUID, Integer> activationCounts,
                                                    List<ManaAbilityOption> currentFreePool,
                                                    Accumulator accumulator, Game game, Ability ability, UUID playerId, ManaCost manaCost) {
        if (index == allPaid.size()) {
            // Filter the free pool to only include triggered mana whose triggering ability is active
            List<ManaAbilityOption> effectiveFreePool = filterTriggeredMana(
                currentFreePool, 
                activationCounts, 
                accumulator.selectedMixedOptions
            );
            
            if (solveWithHybrids(cost, effectiveFreePool, game, ability)) {
                accumulator.selectedPaidOptions.clear();
                for (ManaAbilityOption paid : allPaid) {
                    int count = activationCounts.getOrDefault(paid.getOptionId(), 0);
                    for (int i = 0; i < count; i++) {
                        accumulator.selectedPaidOptions.add(paid);
                    }
                }
                return true;
            }

            // If not solvable without pool-dependent, try enumerating pool-dependent abilities
            if (!poolDependentFree.isEmpty() || !poolDependentPaid.isEmpty()) {
                List<ManaAbilityOption> allPoolDependent = new ArrayList<>();
                allPoolDependent.addAll(poolDependentFree);
                allPoolDependent.addAll(poolDependentPaid);

                if (enumeratePoolDependentActivations(
                        cost, effectiveFreePool, allPoolDependent,
                        accumulator, game, ability, playerId, manaCost)) {
                    return true;
                }
            }

            return false;
        }

        ManaAbilityOption paid = allPaid.get(index);
        int maxActivations = paid.hasTapCost() ? 1 : MAX_REUSE_ACTIVATIONS;


        for (int count = 0; count <= maxActivations; count++) {
            activationCounts.put(paid.getOptionId(), count);

            List<ManaCostSymbol> activationCost = paid.getActivationCost();
            if (count == 0) {
                if (enumeratePaidActivations(cost, allFree, allPaid, poolDependentFree, poolDependentPaid, index + 1, spentFreeIds, activationCounts, currentFreePool, accumulator, game, ability, playerId, manaCost)) {
                    return true;
                }
            } else {
                List<List<ManaAbilityOption>> poolWithPaidOutput = getPoolWithPaidActivations(activationCost, spentFreeIds, currentFreePool, paid, game, count, playerId, manaCost);
                for ( List<ManaAbilityOption> pool : poolWithPaidOutput) {
                    Set<UUID> newSpentFreeIds = new HashSet<>(spentFreeIds);
                    for (ManaAbilityOption opt : currentFreePool) {
                        if (!pool.contains(opt)) {
                            newSpentFreeIds.add(opt.getOptionId());
                        }
                    }
                    if (enumeratePaidActivations(cost, allFree, allPaid, poolDependentFree, poolDependentPaid, index + 1, newSpentFreeIds, activationCounts, pool, accumulator, game, ability, playerId, manaCost)) {
                        return true;
                    }
                }
            }
        }

        activationCounts.remove(paid.getOptionId());
        return false;
    }

    /**
     * Enumerates pool-dependent ability activations by trying different combinations.
     * Pool-dependent abilities (like Doubling Cube) depend on the current pool state and can cascade.
     *
     * @param cost the mana cost to pay
     * @param basePool the available mana pool before activating pool-dependent abilities
     * @param poolDependentOptions the pool-dependent abilities to consider
     * @param accumulator the result accumulator
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
            Accumulator accumulator,
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
                0, new ArrayList<>(), accumulator,
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
     * @param accumulator the result accumulator
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
            Accumulator accumulator,
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
                        index + 1, activatedOptions, accumulator,
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
                        activatedOptions, accumulator,
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
     * @param accumulator result accumulator
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
            Accumulator accumulator,
            Game game,
            Ability ability,
            UUID playerId,
            ManaCost manaCost) {

        // Recursively activate 'count' times, enumerating payment allocations
        return activatePoolDependentWithEnumeration(
                poolDepOpt, currentPool, count, 0,
                cost, poolDependentOptions, nextIndex,
                activatedOptions, accumulator,
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
            Accumulator accumulator,
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
                    nextIndex, newActivatedOptions, accumulator,
                    game, ability, playerId, manaCost);
        }

        // Check if we can afford to activate
        if (!canAffordPoolDependentActivation(poolDepOpt, currentPool, game, ability, playerId, manaCost)) {
            return false;
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
                        activatedOptions, accumulator,
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
                        activatedOptions, accumulator,
                        game, ability, playerId, manaCost)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Enumerates all possible ways to pay a cost from a pool, returning the
     * different remaining pool states after payment.
     */
    private static List<List<ManaAbilityOption>> enumeratePaymentAllocations(
            List<ManaCostSymbol> cost,
            List<ManaAbilityOption> pool,
            Game game,
            Ability ability) {

        List<List<ManaAbilityOption>> results = new ArrayList<>();
        enumeratePaymentAllocationsRecursive(cost, pool, 0, new ArrayList<>(pool), results, game, ability);
        return results;
    }

    /**
     * Recursively builds all payment allocations for a cost.
     */
    private static void enumeratePaymentAllocationsRecursive(
            List<ManaCostSymbol> cost,
            List<ManaAbilityOption> originalPool,
            int costIndex,
            List<ManaAbilityOption> currentRemaining,
            List<List<ManaAbilityOption>> results,
            Game game,
            Ability ability) {

        // Base case: paid all cost symbols
        if (costIndex == cost.size()) {
            results.add(new ArrayList<>(currentRemaining));
            return;
        }

        ManaCostSymbol symbol = cost.get(costIndex);
        int amountNeeded = symbol.minCost();

        // Try different ways to pay this symbol from currentRemaining
        enumerateSymbolPayments(symbol, amountNeeded, currentRemaining, 0, 0,
                new ArrayList<>(),
                cost, costIndex + 1, originalPool, results, game, ability);
    }

    /**
     * Enumerates different ways to pay a single symbol from the remaining pool.
     */
    private static void enumerateSymbolPayments(
            ManaCostSymbol symbol,
            int amountNeeded,
            List<ManaAbilityOption> remainingPool,
            int poolIndex,
            int amountPaid,
            List<ManaAbilityOption> newRemaining,
            List<ManaCostSymbol> cost,
            int nextCostIndex,
            List<ManaAbilityOption> originalPool,
            List<List<ManaAbilityOption>> results,
            Game game,
            Ability ability) {

        // Base case: fully paid this symbol
        if (amountPaid >= amountNeeded) {
            // Add remaining pool items that we haven't processed yet
            for (int i = poolIndex; i < remainingPool.size(); i++) {
                newRemaining.add(remainingPool.get(i));
            }
            // Continue to next cost symbol
            enumeratePaymentAllocationsRecursive(cost, originalPool, nextCostIndex,
                    newRemaining, results, game, ability);
            return;
        }

        // Base case: exhausted pool without fully paying
        if (poolIndex >= remainingPool.size()) {
            return; // Can't pay - don't add to results
        }

        ManaAbilityOption opt = remainingPool.get(poolIndex);
        boolean canPay = opt.canProduce(symbol);

        if (!canPay) {
            // This option can't pay for this symbol - keep it and try next
            List<ManaAbilityOption> updatedRemaining = new ArrayList<>(newRemaining);
            updatedRemaining.add(opt);
            enumerateSymbolPayments(symbol, amountNeeded, remainingPool, poolIndex + 1, amountPaid,
                    updatedRemaining, cost, nextCostIndex, originalPool, results, game, ability);
        } else {
            // This option CAN pay - try using different amounts
            int maxUse = Math.min(opt.getCapacity(), amountNeeded - amountPaid);

            // Try using 0 to maxUse from this option
            for (int use = 0; use <= maxUse; use++) {
                List<ManaAbilityOption> updatedRemaining = new ArrayList<>(newRemaining);

                if (use == 0) {
                    // Don't use this option - keep it fully
                    updatedRemaining.add(opt);
                } else if (use < opt.getCapacity()) {
                    // Partial use - keep the remainder
                    updatedRemaining.add(opt.withCapacity(opt.getCapacity() - use));
                }
                // If use == opt.getCapacity(), we consumed it entirely (don't add to remaining)

                enumerateSymbolPayments(symbol, amountNeeded, remainingPool, poolIndex + 1, amountPaid + use,
                        updatedRemaining, cost, nextCostIndex, originalPool, results, game, ability);
            }
        }
    }

    /**
     * Activates a pool-dependent ability with a specific payment allocation.
     * The remainingPoolAfterPayment is provided (already computed by enumeration).
     */
    private static List<ManaAbilityOption> activatePoolDependentSinglePayment(
            ManaAbilityOption poolDepOpt,
            List<ManaAbilityOption> currentPool,
            List<ManaAbilityOption> remainingPoolAfterPayment,
            Game game,
            Ability ability,
            UUID playerId) {

        List<ManaAbilityOption> remainingPool = remainingPoolAfterPayment != null
                ? remainingPoolAfterPayment
                : new ArrayList<>(currentPool);

        // Calculate pool mana AFTER paying activation cost
        Mana poolMana = calculatePoolMana(remainingPool);

        // Get the ability and calculate its output given the REDUCED pool state
        Ability manaAbility = getAbility(poolDepOpt, game);
        if (manaAbility == null || !(manaAbility instanceof ManaAbility)) {
            return null;
        }

        ManaAbility poolDepAbility = (ManaAbility) manaAbility;
        List<Mana> producedMana = poolDepAbility.getNetMana(game, poolMana);

        if (producedMana == null || producedMana.isEmpty()) {
            return null;
        }

        // Add the produced mana to the remaining pool
        List<ManaAbilityOption> result = new ArrayList<>(remainingPool);
        for (Mana mana : producedMana) {
            List<ManaAbilityOption> producedOptions = convertManaToOptions(mana, poolDepOpt.getAbilityId());
            result.addAll(producedOptions);
        }

        return result;
    }

    /**
     * Activates a pool-dependent ability and returns the resulting pool state.
     *
     * @deprecated Use activatePoolDependentSinglePayment with explicit payment allocation instead
     */
    private static List<ManaAbilityOption> activatePoolDependent(
            ManaAbilityOption poolDepOpt,
            List<ManaAbilityOption> currentPool,
            Game game,
            Ability ability,
            UUID playerId) {

        // Subtract the activation cost from the pool FIRST
        List<ManaAbilityOption> remainingPool = subtractActivationCost(
                poolDepOpt.getActivationCost(), currentPool, game, ability);

        if (remainingPool == null) {
            return null; // Can't afford activation cost
        }

        // Calculate pool mana AFTER paying activation cost
        Mana poolMana = calculatePoolMana(remainingPool);

        // Get the ability and calculate its output given the REDUCED pool state
        Ability manaAbility = getAbility(poolDepOpt, game);
        if (!(manaAbility instanceof ManaAbility poolDepAbility)) {
            return null;
        }

        List<Mana> producedMana = poolDepAbility.getNetMana(game, poolMana);

        if (producedMana == null || producedMana.isEmpty()) {
            return null;
        }

        // Add the produced mana to the remaining pool
        List<ManaAbilityOption> result = new ArrayList<>(remainingPool);
        for (Mana mana : producedMana) {
            List<ManaAbilityOption> producedOptions = convertManaToOptions(mana, poolDepOpt.getAbilityId());
            result.addAll(producedOptions);
        }

        return result;
    }

    /**
     * Calculates the amount of each mana type in the pool after activating the given options.
     * Used to evaluate pool-dependent abilities like Doubling Cube.
     */
    private static Mana calculatePoolMana(List<ManaAbilityOption> activatedOptions) {
        Mana pool = new Mana();
        for (ManaAbilityOption opt : activatedOptions) {
            if (!opt.isPoolDependent()) {
                // Only add mana from normal (non-pool-dependent) abilities
                Mana produced = opt.toMana();
                pool.add(produced);
            }
        }
        return pool;
    }

    /**
     * Checks if activating a pool-dependent ability with the given pool state would help pay the cost.
     * Returns true if the activation cost can be paid from available sources.
     */
    private static boolean canAffordPoolDependentActivation(
            ManaAbilityOption poolDepOpt,
            List<ManaAbilityOption> currentPool,
            Game game,
            Ability ability,
            UUID playerId,
            ManaCost manaCost) {
        if (!poolDepOpt.hasCost()) {
            return true; // Free to activate
        }

        // Check if we can pay the activation cost from the current pool
        List<ManaCostSymbol> activationCost = poolDepOpt.getActivationCost();
        return checkFlow(activationCost, currentPool, game, ability);
    }

    /**
     * Subtracts the activation cost from the pool and returns the remaining pool.
     * Returns null if the cost cannot be paid.
     */
    private static List<ManaAbilityOption> subtractActivationCost(
            List<ManaCostSymbol> activationCost,
            List<ManaAbilityOption> currentPool,
            Game game,
            Ability ability) {

        if (activationCost == null || activationCost.isEmpty()) {
            return new ArrayList<>(currentPool);
        }

        // Check if we can pay the cost
        if (!checkFlow(activationCost, currentPool, game, ability)) {
            return null;
        }

        // Simple greedy subtraction (could be improved with actual flow result)
        List<ManaAbilityOption> remaining = new ArrayList<>(currentPool);

        // Track remaining cost amounts for each symbol
        Map<ManaCostSymbol, Integer> symbolAmounts = new LinkedHashMap<>();
        for (ManaCostSymbol symbol : activationCost) {
            symbolAmounts.put(symbol, symbol.minCost());
        }

        // Pay each symbol from the pool
        for (ManaCostSymbol symbol : activationCost) {
            int amountRemaining = symbolAmounts.get(symbol);
            if (amountRemaining <= 0) {
                continue; // Already fully paid
            }

            for (int i = 0; i < remaining.size() && amountRemaining > 0; i++) {
                ManaAbilityOption opt = remaining.get(i);
                if (opt.canProduce(symbol)) {
                    int toSubtract = Math.min(opt.getCapacity(), amountRemaining);
                    if (toSubtract > 0) {
                        amountRemaining -= toSubtract;
                        symbolAmounts.put(symbol, amountRemaining);

                        if (opt.getCapacity() > toSubtract) {
                            remaining.set(i, opt.withCapacity(opt.getCapacity() - toSubtract));
                        } else {
                            remaining.remove(i);
                            i--;
                        }
                    }
                }
            }
        }

        return remaining;
    }

    /**
     * Converts a Mana object to a list of ManaAbilityOptions.
     */
    private static List<ManaAbilityOption> convertManaToOptions(Mana mana, UUID sourceAbilityId) {
        List<ManaAbilityOption> options = new ArrayList<>();

        if (mana.getWhite() > 0) {
            options.add(ManaAbilityOption.createSyntheticOption(
                    sourceAbilityId, ManaType.WHITE, mana.getWhite()));
        }
        if (mana.getBlue() > 0) {
            options.add(ManaAbilityOption.createSyntheticOption(
                    sourceAbilityId, ManaType.BLUE, mana.getBlue()));
        }
        if (mana.getBlack() > 0) {
            options.add(ManaAbilityOption.createSyntheticOption(
                    sourceAbilityId, ManaType.BLACK, mana.getBlack()));
        }
        if (mana.getRed() > 0) {
            options.add(ManaAbilityOption.createSyntheticOption(
                    sourceAbilityId, ManaType.RED, mana.getRed()));
        }
        if (mana.getGreen() > 0) {
            options.add(ManaAbilityOption.createSyntheticOption(
                    sourceAbilityId, ManaType.GREEN, mana.getGreen()));
        }
        if (mana.getColorless() > 0) {
            options.add(ManaAbilityOption.createSyntheticOption(
                    sourceAbilityId, ManaType.COLORLESS, mana.getColorless()));
        }
        if (mana.getGeneric() > 0) {
            options.add(ManaAbilityOption.createSyntheticOption(
                    sourceAbilityId, ManaType.GENERIC, mana.getGeneric()));
        }

        return options;
    }

    /**
     * Filters a mana pool to only include triggered mana options whose triggering ability is active.
     * 
     * @param freePool the full free mana pool
     * @param activationCounts the count of paid ability activations
     * @param selectedMixedOptions the mixed options that have been selected
     * @return filtered pool with only available triggered mana
     */
    private static List<ManaAbilityOption> filterTriggeredMana(
            List<ManaAbilityOption> freePool,
            Map<UUID, Integer> activationCounts,
            List<ManaAbilityOption> selectedMixedOptions) {
        
        // Collect IDs of all abilities being activated
        Set<UUID> activeAbilityIds = new HashSet<>();
        
        // Add abilities from the free pool (non-triggered mana)
        for (ManaAbilityOption opt : freePool) {
            if (!opt.isTriggeredMana()) {
                activeAbilityIds.add(opt.getAbilityId());
            }
        }
        
        // Add abilities from mixed selections
        for (ManaAbilityOption opt : selectedMixedOptions) {
            activeAbilityIds.add(opt.getAbilityId());
        }
        
        // Add abilities from paid activations (count > 0)
        for (Map.Entry<UUID, Integer> entry : activationCounts.entrySet()) {
            if (entry.getValue() > 0) {
                // Find the ability ID for this option
                for (ManaAbilityOption opt : freePool) {
                    if (opt.getOptionId().equals(entry.getKey())) {
                        activeAbilityIds.add(opt.getAbilityId());
                        break;
                    }
                }
            }
        }
        
        // Build effective pool: include normal mana and triggered mana whose trigger is active
        List<ManaAbilityOption> effectiveFreePool = new ArrayList<>();
        for (ManaAbilityOption opt : freePool) {
            if (!opt.isTriggeredMana()) {
                // Normal mana - always include
                effectiveFreePool.add(opt);
            } else if (activeAbilityIds.contains(opt.getTriggeringAbilityId())) {
                // Triggered mana whose triggering ability is being used - include
                effectiveFreePool.add(opt);
            }
            // else: triggered mana whose trigger isn't used - exclude
        }
        
        return effectiveFreePool;
    }

    /**
     * Builds a directed weighted flow graph and runs Dinic's max-flow algorithm.
     * Returns true if {@code maxFlow >= totalCost}.
     *
     * <pre>
     * SuperSource ──[cap=option.capacity]──► ManaSourceNode
     * ManaSourceNode ──[cap=capacityBetween]──► CostNode
     * CostNode ──[cap=symbol.minCost()]──► SuperSink
     * </pre>
     */
    static boolean checkFlow(List<ManaCostSymbol> costSymbols,
                              List<ManaAbilityOption> availableOptions) {
        return checkFlow(costSymbols, availableOptions, null, null);
    }

    static boolean checkFlow(List<ManaCostSymbol> costSymbols,
                              List<ManaAbilityOption> availableOptions,
                              Game game,
                              Ability ability) {
        if (costSymbols.isEmpty()) {
            return true;
        }

        int totalCost = costSymbols.stream()
                .filter(s -> s.getType() != ManaCostSymbol.SymbolType.PHYREXIAN)
                .mapToInt(ManaCostSymbol::minCost).sum();
        if (totalCost == 0) {
            return true;
        }
        if (availableOptions.isEmpty()) {
            return false;
        }

        SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph =
                new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        final String SOURCE = "__SRC__";
        final String SINK   = "__SNK__";
        graph.addVertex(SOURCE);
        graph.addVertex(SINK);

        // Create cost nodes and connect them to sink
        Map<String, Integer> costNodeDemand = new HashMap<>();
        
        for (int i = 0; i < costSymbols.size(); i++) {
            ManaCostSymbol symbol = costSymbols.get(i);
            String costNodeId;

            if (symbol.getType() == ManaCostSymbol.SymbolType.GENERIC) {
                costNodeId = "COST_GENERIC_" + symbol.getGenericCost();
            } else {
                costNodeId = "COST_" + i + "_" + symbol.getType() + symbol.getColorOptions();
            }

            costNodeDemand.merge(costNodeId, symbol.minCost(), Integer::sum);

            if (!graph.containsVertex(costNodeId)) {
                graph.addVertex(costNodeId);
            }
        }

        // Connect cost nodes to sink (demand)
        for (Map.Entry<String, Integer> entry : costNodeDemand.entrySet()) {
            String costNode = entry.getKey();
            int cap = entry.getValue();
            addOrAccumulateEdge(graph, costNode, SINK, cap);
        }

        Map<Integer, String> symbolToCostNode = getSymbolToCostNode(costSymbols);

        // Create source nodes and connect them from source, then to cost nodes
        for (int j = 0; j < availableOptions.size(); j++) {
            ManaAbilityOption option = availableOptions.get(j);
            String sourceNode = "SRC_" + j;

            if (!graph.containsVertex(sourceNode)) {
                graph.addVertex(sourceNode);
            }
            
            // SuperSource → ManaSourceNode (supply)
            addOrAccumulateEdge(graph, SOURCE, sourceNode, option.getCapacity());

            // ManaSourceNode → CostNode (can this source pay for this cost?)
            for (int i = 0; i < costSymbols.size(); i++) {
                ManaCostSymbol symbol = costSymbols.get(i);
                String costNode = symbolToCostNode.get(i);
                int edgeCap = capacityBetween(symbol, option, game, ability);
                if (edgeCap > 0) {
                    addOrAccumulateEdge(graph, sourceNode, costNode, edgeCap);
                }
            }
        }

        // Run Dinic's max-flow
        DinicMFImpl<String, DefaultWeightedEdge> dinic = new DinicMFImpl<>(graph);
        double maxFlow = dinic.getMaximumFlowValue(SOURCE, SINK);
        return (int) Math.round(maxFlow) >= totalCost;
    }

    private static @NonNull Map<Integer, String> getSymbolToCostNode(List<ManaCostSymbol> costSymbols) {
        Map<Integer, String> symbolToCostNode = new HashMap<>();
        for (int i = 0; i < costSymbols.size(); i++) {
            ManaCostSymbol symbol = costSymbols.get(i);
            String costNodeId;
            if (symbol.getType() == ManaCostSymbol.SymbolType.GENERIC) {
                costNodeId = "COST_GENERIC_" + symbol.getGenericCost();
            } else {
                costNodeId = "COST_" + i + "_" + symbol.getType() + symbol.getColorOptions();
            }
            symbolToCostNode.put(i, costNodeId);
        }
        return symbolToCostNode;
    }

    /**
     * Adds an edge from {@code u} to {@code v} with the given capacity, or
     * accumulates capacity on the existing edge if one already exists.
     */
    private static void addOrAccumulateEdge(SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph,
                                             String u, String v, int cap) {
        DefaultWeightedEdge existing = graph.getEdge(u, v);
        if (existing != null) {
            graph.setEdgeWeight(existing, graph.getEdgeWeight(existing) + cap);
        } else {
            DefaultWeightedEdge edge = graph.addEdge(u, v);
            graph.setEdgeWeight(edge, cap);
        }
    }

    /**
     * Returns the maximum flow capacity from a cost symbol to a source option.
     * When game and ability are provided, also checks AsThoughEffects.
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

    private static int capacityBetween(ManaCostSymbol symbol, ManaAbilityOption option) {
        if (option.isProducesAny()) {
            return switch (symbol.getType()) {
                case MONO_COLOR, HYBRID_COLOR, HYBRID_GENERIC, COLORLESS, GENERIC -> option.getCapacity();
                case PHYREXIAN -> 0;  // Phyrexian is paid by life, not mana
            };
        }
        return capacityBetween(symbol, option, option.getProducibleTypes());
    }

    private static int capacityBetween(ManaCostSymbol symbol, ManaAbilityOption option, Set<ManaType> producibleTypes) {
        return switch (symbol.getType()) {
            case MONO_COLOR, HYBRID_GENERIC -> {
                ManaType color = symbol.getColorOptions().iterator().next();
                yield producibleTypes.contains(color) ? option.getCapacity() : 0;
            }
            case PHYREXIAN -> {
                // Phyrexian mana is paid by life, not mana sources.
                // Since we're excluding it from totalCost, return 0 capacity needed.
                yield 0;
            }
            case HYBRID_COLOR -> {
                for (ManaType color : symbol.getColorOptions()) {
                    if (producibleTypes.contains(color)) yield option.getCapacity();
                }
                yield 0;
            }
            case COLORLESS -> producibleTypes.contains(ManaType.COLORLESS) ? option.getCapacity() : 0;
            case GENERIC -> option.getCapacity();
        };
    }

    /**
     * Gets the set of mana types that can be spent from this option, considering AsThoughEffects.
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
                ManaPoolItem manaItem = new ManaPoolItem(0, 0, 0, 0, 0, 0, null, option.getAbilityId(), false);
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

    private static List<List<ManaAbilityOption>> getPoolWithPaidActivations(List<ManaCostSymbol> activationCost,
                                                                      Set<UUID> spentFreeIds, List<ManaAbilityOption> freeOptions, ManaAbilityOption paid, Game game, int paidActivations, UUID playerId, ManaCost manaCost) {
        List<List<ManaAbilityOption>> resultPools = new ArrayList<>();
        if (freeOptions.isEmpty() || activationCost.isEmpty()) {
            return resultPools;
        }
        int totalCost = activationCost.stream()
                .filter(s -> s.getType() != ManaCostSymbol.SymbolType.PHYREXIAN)
                .mapToInt(ManaCostSymbol::minCost).sum() * paidActivations;
        Set<UUID> tempSpentFreeIds = new HashSet<>(spentFreeIds);
        while (tempSpentFreeIds.size() < freeOptions.size()) {
            List<ManaAbilityOption> options = new ArrayList<>(freeOptions);
            int sum = 0;
            symbolLoop:
            for (ManaCostSymbol symbol : activationCost) {
                for (ManaAbilityOption option : freeOptions) {
                    if (tempSpentFreeIds.contains(option.getOptionId())) {
                        continue;
                    }
                    if (!checkConditions(option, getAbility(paid, game), game, playerId, manaCost)) {
                        continue;
                    }
                    if (option.canProduce(symbol)) {
                        int difference = totalCost - sum;
                        if (option.getCapacity() > difference) {
                            // Don't add more capacity than needed to fund the paid ability.
                            options.add(option.withCapacity(difference));
                        }
                        sum += option.getCapacity();
                        options.remove(option);
                        tempSpentFreeIds.add(option.getOptionId());
                    }
                    if (sum >= totalCost) {
                        for (int i = 0; i < paidActivations; i++) {
                            options.add(paid);
                        }
                        break symbolLoop;
                    }
                }
            }
            int paidCount = options.stream().filter(o -> o.getAbilityId().equals(paid.getAbilityId())).mapToInt(o -> 1).sum();
            if (paidCount < paidActivations) {
                break;
            }
            if (sum >= totalCost) {
                resultPools.add(options);
            }
        }
        return resultPools;
    }

    private static Ability getAbility(ManaAbilityOption option, Game game) {
        if (game == null || option == null) {
            return null;
        }
        return game.getAbility(option.getAbilityId(), option.getSourceId()).orElse(null);
    }

    /**
     * Handles hybrid symbols by enumerating their resolutions before running the
     * core flow check.
     */
    private static boolean solveWithHybrids(List<ManaCostSymbol> cost,
                                             List<ManaAbilityOption> options,
                                             Game game,
                                             Ability ability) {
        List<Integer> hybridIndices = new ArrayList<>();
        for (int i = 0; i < cost.size(); i++) {
            if (cost.get(i).isHybrid()) {
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
     * Resolves a hybrid symbol to a concrete symbol.
     *
     * @param hybrid the hybrid symbol
     * @param choice 0 = first option (color), 1 = second option (other color or generic)
     */
    private static ManaCostSymbol resolveHybrid(ManaCostSymbol hybrid, int choice) {
        List<ManaType> colors = new ArrayList<>(hybrid.getColorOptions());
        switch (hybrid.getType()) {
            case HYBRID_COLOR:
                // choice 0 → first color, choice 1 → second color
                return ManaCostSymbol.monoColor(colors.get(choice % colors.size()));
            case HYBRID_GENERIC:
                // choice 0 → the mono color, choice 1 → generic alternative
                if (choice == 0) {
                    return ManaCostSymbol.monoColor(colors.getFirst());
                } else {
                    return ManaCostSymbol.generic(hybrid.getGenericCost());
                }
            default:
                return hybrid; // not actually hybrid
        }
    }
}
