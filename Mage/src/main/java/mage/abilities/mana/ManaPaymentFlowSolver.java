package mage.abilities.mana;

import mage.abilities.Ability;
import mage.abilities.condition.Condition;
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
            if (game == null || ability == null || checkConditions(option, ability, game)) {
                freePool.add(option);
            }
        }

        Accumulator accumulator = new Accumulator();
        accumulator.purePaid = purePaid;
        accumulator.freePool = freePool;

        if (enumerateMixedSelections(cost, mixed, game, ability, 0, accumulator)) {
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
        return canPay(cost, sources, null, null);
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
            if (game == null || ability == null || checkConditions(option, ability, game)) {
                freePool.add(option);
            }
        }

        Accumulator acc = new Accumulator();
        acc.purePaid = purePaid;
        acc.freePool = freePool;
        return enumerateMixedSelections(cost, mixed, game, ability, 0, acc);
    }

    private static class Accumulator {
        List<ManaSourceNode> purePaid = new ArrayList<>();
        List<ManaAbilityOption> freePool = new ArrayList<>();
        final List<ManaAbilityOption> selectedMixedOptions = new ArrayList<>();
        final List<ManaAbilityOption> selectedPaidOptions = new ArrayList<>();
    }

    private static boolean checkConditions(ManaAbilityOption option, Ability ability, Game game) {
        if (!option.hasConditions() || game == null || ability == null) {
            return true;
        }
        for (Condition condition : option.getConditions()) {
            if (!condition.apply(game, ability)) {
                return false;
            }
        }
        return true;
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
                                                     int idx,
                                                     Accumulator accumulator) {
        if (idx == mixed.size()) {
            return enumeratePaidFunding(cost, accumulator, game, ability);
        }

        ManaSourceNode node = mixed.get(idx);
        for (ManaAbilityOption option : node.getAbilityOptions()) {
            if (game != null && ability != null && !checkConditions(option, ability, game)) {
                continue;
            }
            accumulator.selectedMixedOptions.add(option);
            if (enumerateMixedSelections(cost, mixed, game, ability, idx + 1, accumulator)) {
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
    private static boolean enumeratePaidFunding(List<ManaCostSymbol> cost, Accumulator accumulator, Game game, Ability ability) {
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

        List<ManaAbilityOption> allPaid = new ArrayList<>();
        for (ManaSourceNode node : accumulator.purePaid) {
            allPaid.addAll(node.getAbilityOptions());
        }
        allPaid.addAll(paidMixed);

        allPaid.sort(Comparator.comparingInt(o -> o.getActivationCost().stream()
                .mapToInt(ManaCostSymbol::minCost).sum()));

        return enumeratePaidActivations(cost, allFree, allPaid, 0, Collections.emptySet(), new HashMap<>(), allFree, accumulator, game, ability);
    }

    private static boolean enumeratePaidActivations(List<ManaCostSymbol> cost,
                                                    List<ManaAbilityOption> allFree,
                                                    List<ManaAbilityOption> allPaid,
                                                    int index,
                                                    Set<UUID> spentFreeIds,
                                                    Map<UUID, Integer> activationCounts,
                                                    List<ManaAbilityOption> currentFreePool,
                                                    Accumulator accumulator, Game game, Ability ability) {
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
            return false;
        }

        ManaAbilityOption paid = allPaid.get(index);
        int maxActivations = paid.hasTapCost() ? 1 : MAX_REUSE_ACTIVATIONS;


        for (int count = 0; count <= maxActivations; count++) {
            activationCounts.put(paid.getOptionId(), count);

            List<ManaCostSymbol> activationCost = paid.getActivationCost();
            if (count == 0) {
                if (enumeratePaidActivations(cost, allFree, allPaid, index + 1, spentFreeIds, activationCounts, currentFreePool, accumulator, game, ability)) {
                    return true;
                }
            } else {
                List<List<ManaAbilityOption>> poolWithPaidOutput = getPoolWithPaidActivations(activationCost, spentFreeIds, currentFreePool, paid, game, count);
                for ( List<ManaAbilityOption> pool : poolWithPaidOutput) {
                    Set<UUID> newSpentFreeIds = new HashSet<>(spentFreeIds);
                    for (ManaAbilityOption opt : currentFreePool) {
                        if (!pool.contains(opt)) {
                            newSpentFreeIds.add(opt.getOptionId());
                        }
                    }
                    if (enumeratePaidActivations(cost, allFree, allPaid, index + 1, newSpentFreeIds, activationCounts, pool, accumulator, game, ability)) {
                        return true;
                    }
                }
            }
        }

        activationCounts.remove(paid.getOptionId());
        return false;
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

        Set<ManaType> spendableTypes = getSpendableTypes(option, game, ability);
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
    private static Set<ManaType> getSpendableTypes(ManaAbilityOption option, Game game, Ability ability) {
        EnumSet<ManaType> spendableTypes = EnumSet.copyOf(option.getProducibleTypes());

        if (ability == null) {
            return spendableTypes;
        }

        UUID controllerId = ability.getControllerId();
        UUID objectId = ability.getSourceId() != null ? ability.getSourceId() : option.getAbilityId();

        for (ManaType manaType : option.getProducibleTypes()) {
            ManaPoolItem manaItem = new ManaPoolItem(0, 0, 0, 0, 0, 0, null, option.getAbilityId(), false);
            manaItem.add(manaType, 1);
            ManaType asThoughType = game.getContinuousEffects().asThoughMana(
                    manaType, manaItem, objectId, ability, controllerId, game);
            if (asThoughType != null && asThoughType != manaType) {
                spendableTypes.add(asThoughType);
            } else if (asThoughType == manaType) {
                // If the AsThoughEffect returns the same type, it means the mana can be treated as any color
                spendableTypes.add(ManaType.WHITE);
                spendableTypes.add(ManaType.BLUE);
                spendableTypes.add(ManaType.BLACK);
                spendableTypes.add(ManaType.RED);
                spendableTypes.add(ManaType.GREEN);
            }
        }

        return spendableTypes;
    }

    private static List<List<ManaAbilityOption>> getPoolWithPaidActivations(List<ManaCostSymbol> activationCost,
                                                                      Set<UUID> spentFreeIds, List<ManaAbilityOption> freeOptions, ManaAbilityOption paid, Game game, int paidActivations) {
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
                    if (!checkConditions(option, getAbility(paid, game), game)) {
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




