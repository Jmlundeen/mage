package mage.abilities.mana;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.condition.Condition;
import mage.constants.ManaType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ManaEvent;
import mage.game.events.TappedForManaEvent;
import mage.players.Player;

import java.util.*;

/**
 * Represents available mana sources as a list of {@link ManaSourceNode} objects.
 * Extends ArrayList directly for efficient storage.
 *
 * @author BetaSteward_at_googlemail.com, JayDi85
 */
public class ManaOptions extends ArrayList<ManaSourceNode> {

    public ManaOptions() {
    }

    protected ManaOptions(ManaOptions options) {
        super(options);
    }

    public boolean canPayWithFlow(List<ManaCostSymbol> costSymbols) {
        return canPayWithFlow(costSymbols, null, null);
    }

    public boolean canPayWithFlow(List<ManaCostSymbol> costSymbols, Game game, Ability ability) {
        if (costSymbols.isEmpty()) {
            return true;
        }
        if (isEmpty()) {
            return false;
        }
        return ManaPaymentFlowSolver.canPay(costSymbols, this, game, ability);
    }

    public void addMana(List<ActivatedManaAbilityImpl> abilities, Game game) {
        if (abilities.isEmpty()) {
            return;
        }

        Map<UUID, List<ActivatedManaAbilityImpl>> bySource = new LinkedHashMap<>();
        for (ActivatedManaAbilityImpl ability : abilities) {
            bySource.computeIfAbsent(ability.getSourceId(), k -> new ArrayList<>()).add(ability);
        }

        // Track triggered mana by the ability that triggered it (not by source)
        Map<UUID, List<List<Mana>>> triggeredManaByTriggeringAbility = new LinkedHashMap<>();

        for (Map.Entry<UUID, List<ActivatedManaAbilityImpl>> entry : bySource.entrySet()) {
            List<ManaAbilityOption> tapCostOptions = new ArrayList<>();
            // Each inner list is the set of mutually-exclusive alternatives for one non-tap ability.
            List<List<ManaAbilityOption>> nonTapCostGroups = new ArrayList<>();

            abilityLoop:
            for (ActivatedManaAbilityImpl ability : entry.getValue()) {
                List<Mana> netManas = ability.getNetMana(game);
                if (netManas.isEmpty()) {
                    continue;
                }
                List<Condition> conditions = netManas.stream()
                        .filter(Mana::hasConditions)
                        .flatMap(m -> m.getConditions().stream())
                        .toList();
                // check triggered mana and mana replacement.
                // replacement effects should modify in place and triggered mana will be processed at the end
                for (Mana mana : netManas) {
                    if (checkManaReplacementAndTriggeredMana(ability, game, mana)) {
                        for (List<Mana> manaList : getSimulatedTriggeredManaFromPlayer(game, ability)) {
                            // Create triggered mana options that reference their triggering ability
                            List<ManaAbilityOption> triggeredOptions =
                                    ManaAbilityOption.fromNetMana(UUID.randomUUID(), ability.getId(), manaList);
                            add(ManaSourceNode.ofOptions(triggeredOptions));
                        }
                    } else {
                        // If the mana is fully replaced, skip adding any options for this ability.
                        continue abilityLoop;
                    }
                }

                List<ManaAbilityOption> options = ManaAbilityOption.fromAbility(ability, netManas, conditions);
                if (ability.hasTapCost()) {
                    // All tap-cost alternatives share the single tap — flatten into one group.
                    tapCostOptions.addAll(options);
                } else {
                    // Alternatives of one non-tap ability are still mutually exclusive —
                    // keep them together in their own node.
                    nonTapCostGroups.add(options);
                }
            }
            ManaSourceNode tapNode = ManaSourceNode.ofOptions(tapCostOptions);
            if (!tapNode.getAbilityOptions().isEmpty()) {
                add(tapNode);
            }
            for (List<ManaAbilityOption> group : nonTapCostGroups) {
                add(ManaSourceNode.ofOptions(group));
            }
        }
    }

    public void addMana(ManaSourceNode node) {
        add(node);
    }

    public void addMana(Mana mana) {
        add(ManaSourceNode.fromTriggeredMana(mana));
    }

    /**
     * Backward-compatible method to add Mana objects.
     */
    public boolean add(Mana mana) {
        add(ManaSourceNode.fromTriggeredMana(mana));
        return true;
    }

    public boolean addMana(List<ManaSourceNode> newNodes) {
        return addAll(newNodes);
    }

    public void addMana(ManaOptions options) {
        addAll(options);
    }

    public boolean addManaPoolDependant(List<ActivatedManaAbilityImpl> abilities, Game game) {
        if (abilities.size() != 1) {
            return false;
        }
        ManaSourceNode node = ManaSourceNode.fromAbilitiesAll(abilities, game);
        add(node);
        return true;
    }

    public boolean addManaWithCost(List<ActivatedManaAbilityImpl> abilities, Game game) {
        if (abilities.isEmpty()) {
            return false;
        }

        boolean wasUsable = false;

        for (ActivatedManaAbilityImpl ability : abilities) {
            List<Mana> netManas = ability.getNetMana(game);
            if (netManas.isEmpty()) {
                continue;
            }

            List<Mana> processedManas = new ArrayList<>();
            for (Mana mana : netManas) {
                if (checkManaReplacementAndTriggeredMana(ability, game, mana)) {
                    processedManas.add(mana);
                }
            }

            if (processedManas.isEmpty()) {
                continue;
            }

            List<ManaAbilityOption> options = new ArrayList<>();

            for (Mana baseMana : processedManas) {
                List<Mana> triggeredVariations = getTriggeredManaVariations(game, ability, baseMana);
                List<Mana> allMana = new ArrayList<>(triggeredVariations);

                int totalCapacity = allMana.stream().mapToInt(Mana::count).max().orElse(0);

                EnumSet<ManaType> types = EnumSet.noneOf(ManaType.class);
                boolean producesAny = false;

                for (Mana m : allMana) {
                    if (m.getWhite() > 0) types.add(ManaType.WHITE);
                    if (m.getBlue() > 0) types.add(ManaType.BLUE);
                    if (m.getBlack() > 0) types.add(ManaType.BLACK);
                    if (m.getRed() > 0) types.add(ManaType.RED);
                    if (m.getGreen() > 0) types.add(ManaType.GREEN);
                    if (m.getColorless() > 0) types.add(ManaType.COLORLESS);
                    if (m.getAny() > 0) producesAny = true;
                }

                options.add(ManaAbilityOption.of(
                        ability.getId(),
                        totalCapacity,
                        types,
                        producesAny,
                        ManaAbilityOption.manaToSymbols(ability.getManaCosts().getMana())));
            }

            // All alternatives from this single ability are mutually exclusive —
            // group them in one node so the solver picks at most one per activation.
            add(ManaSourceNode.ofOptions(options));

            wasUsable = true;
        }

        return wasUsable;
    }

    private static List<List<Mana>> getSimulatedTriggeredManaFromPlayer(Game game, Ability ability) {
        Player player = game.getPlayer(ability.getControllerId());
        List<List<Mana>> newList = new ArrayList<>();
        if (player != null) {
            newList.addAll(player.getAvailableTriggeredMana());
            player.getAvailableTriggeredMana().clear();
        }
        return newList;
    }

    private static boolean checkManaReplacementAndTriggeredMana(Ability ability, Game game, Mana mana) {
        if (ability.hasTapCost()) {
            ManaEvent event = new TappedForManaEvent(ability.getSourceId(), ability, ability.getControllerId(), mana, game);
            if (game.replaceEvent(event)) {
                return false;
            }
            game.fireEvent(event);
        }
        ManaEvent manaEvent = new ManaEvent(GameEvent.EventType.MANA_ADDED, ability.getSourceId(), ability, ability.getControllerId(), mana);
        manaEvent.setData(mana.toString());
        game.fireEvent(manaEvent);
        return true;
    }

    public static List<Mana> getTriggeredManaVariations(Game game, Ability ability, Mana baseMana) {
        List<Mana> baseManaPlusTriggeredMana = new ArrayList<>();
        baseManaPlusTriggeredMana.add(baseMana);
        List<List<Mana>> availableTriggeredManaList = getSimulatedTriggeredManaFromPlayer(game, ability);
        for (List<Mana> availableTriggeredMana : availableTriggeredManaList) {
            if (availableTriggeredMana.size() == 1) {
                for (Mana prevMana : baseManaPlusTriggeredMana) {
                    prevMana.add(availableTriggeredMana.getFirst());
                }
            } else if (availableTriggeredMana.size() > 1) {
                List<Mana> copy = new ArrayList<>(baseManaPlusTriggeredMana);
                baseManaPlusTriggeredMana.clear();
                for (Mana triggeredMana : availableTriggeredMana) {
                    for (Mana prevMana : copy) {
                        Mana newMana = new Mana();
                        newMana.add(prevMana);
                        newMana.add(triggeredMana);
                        baseManaPlusTriggeredMana.add(newMana);
                    }
                }
            }
        }
        return baseManaPlusTriggeredMana;
    }

    public ManaOptions copy() {
        return new ManaOptions(this);
    }

    /**
     * Enumerates every possible total-mana combination reachable by independently activating
     * each source node and choosing exactly one option (and, for multi-type options with
     * capacity 1, one colour).  The result is deduplicated as bags (order of pips does not
     * matter).
     *
     * <p>Algorithm: iterative Cartesian product — start with {@code [new Mana()]} and, for
     * each node, multiply the running list by the node's possible contributions.
     *
     * @return deduplicated list of reachable {@link Mana} totals; never {@code null}
     */
    public List<Mana> toManaList() {
        Set<Mana> combinations = new LinkedHashSet<>();

        for (ManaSourceNode node : this) {
            List<Mana> nodeContribs = nodeContributions(node);
            if (nodeContribs.isEmpty()) {
                continue;
            }
            Set<Mana> next = new LinkedHashSet<>();
            if (combinations.isEmpty()) {
                next.addAll(nodeContribs);
            } else {
                for (Mana base : combinations) {
                    for (Mana contrib : nodeContribs) {
                        Mana combined = base.copy();
                        combined.add(contrib);
                        next.add(combined);
                    }
                }
            }
            combinations = next;
        }
        combinations.removeIf(mana -> mana.count() == 0);

        // Deduplicate — Mana.equals() compares all pip fields
        Set<Mana> seen = new LinkedHashSet<>();
        List<Mana> result = new ArrayList<>(0);
        for (Mana m : combinations) {
            if (seen.add(m)) {
                result.add(m);
            }
        }
        return result;
    }

    /**
     * Returns all distinct {@link Mana} objects a single node can contribute in one activation.
     * Exactly one option from the node is chosen; within each option, one colour assignment
     * is made per pip.
     */
    private static List<Mana> nodeContributions(ManaSourceNode node) {
        Set<Mana> seen = new LinkedHashSet<>();
        for (ManaAbilityOption option : node.getAbilityOptions()) {
            seen.addAll(optionContributions(option));
        }
        return new ArrayList<>(seen);
    }

    /**
     * Returns all distinct {@link Mana} objects an option can produce.
     * <ul>
     *   <li>{@code producesAny} → single entry: {@code Mana(any=capacity)}</li>
     *   <li>{@linkplain ManaAbilityOption#isStatic() Static} (all map values&nbsp;&gt;&nbsp;0) →
     *       single entry built from the map via {@link ManaAbilityOption#toMana()}.</li>
     *   <li>{@linkplain ManaAbilityOption#isFlexible() Flexible} (some or all map values&nbsp;=&nbsp;0) →
     *       all distributions of the remaining capacity among the flexible types,
     *       with the fixed portion added to each.</li>
     *   <li>Empty map, no any → single empty Mana.</li>
     * </ul>
     */
    private static List<Mana> optionContributions(ManaAbilityOption option) {
        if (option.isProducesAny()) {
            Mana m = new Mana();
            m.setAny(option.getCapacity());
            return List.of(m);
        }

        Map<ManaType, Integer> map = option.getProducibleMap();
        if (map.isEmpty()) {
            return List.of(new Mana());
        }

        // Static: every type has a fixed count — single exact output
        if (option.isStatic()) {
            return List.of(option.toMana());
        }

        // Flexible: separate fixed and flexible parts
        Mana fixedBase = new Mana();
        int fixedTotal = 0;
        List<ManaType> flexTypes = new ArrayList<>();
        for (Map.Entry<ManaType, Integer> e : map.entrySet()) {
            if (e.getValue() > 0) {
                addManaType(fixedBase, e.getKey(), e.getValue());
                fixedTotal += e.getValue();
            } else {
                flexTypes.add(e.getKey());
            }
        }

        int flexCapacity = option.getCapacity() - fixedTotal;
        if (flexTypes.isEmpty() || flexCapacity <= 0) {
            return List.of(fixedBase);
        }

        // Enumerate all ways to distribute flexCapacity pips among flexTypes
        List<int[]> distributions = new ArrayList<>();
        distributeAmong(flexCapacity, flexTypes.size(), new int[flexTypes.size()], 0, distributions);
        List<Mana> result = new ArrayList<>(distributions.size());
        for (int[] dist : distributions) {
            Mana m = fixedBase.copy();
            for (int i = 0; i < flexTypes.size(); i++) {
                if (dist[i] > 0) {
                    addManaType(m, flexTypes.get(i), dist[i]);
                }
            }
            result.add(m);
        }
        return result;
    }

    private static void addManaType(Mana m, ManaType type, int amount) {
        switch (type) {
            case WHITE     -> m.setWhite(m.getWhite() + amount);
            case BLUE      -> m.setBlue(m.getBlue() + amount);
            case BLACK     -> m.setBlack(m.getBlack() + amount);
            case RED       -> m.setRed(m.getRed() + amount);
            case GREEN     -> m.setGreen(m.getGreen() + amount);
            case COLORLESS -> m.setColorless(m.getColorless() + amount);
            case GENERIC   -> m.setGeneric(m.getGeneric() + amount);
            default        -> {}
        }
    }

    /**
     * Recursively enumerates all ways to distribute {@code remaining} units into
     * {@code buckets - idx} remaining buckets, storing each complete assignment in
     * {@code result}.
     */
    private static void distributeAmong(int remaining, int buckets, int[] current,
                                         int idx, List<int[]> result) {
        if (idx == buckets - 1) {
            current[idx] = remaining;
            result.add(current.clone());
            return;
        }
        for (int i = 0; i <= remaining; i++) {
            current[idx] = i;
            distributeAmong(remaining - i, buckets, current, idx + 1, result);
        }
    }

    /**
     * Returns an iterator over Mana objects for backward compatibility.
     * Each ManaSourceNode is converted to a Mana object representing its producible types.
     */
    public Iterator<Mana> manaIterator() {
        return new Iterator<>() {
            private final Iterator<ManaSourceNode> nodeIter = ManaOptions.this.iterator();

            @Override
            public boolean hasNext() {
                return nodeIter.hasNext();
            }

            @Override
            public Mana next() {
                ManaSourceNode node = nodeIter.next();
                Mana mana = new Mana();
                for (ManaAbilityOption option : node.getAbilityOptions()) {
                    for (ManaType type : option.getProducibleTypes()) {
                        switch (type) {
                            case WHITE -> mana.setWhite(mana.getWhite() + 1);
                            case BLUE -> mana.setBlue(mana.getBlue() + 1);
                            case BLACK -> mana.setBlack(mana.getBlack() + 1);
                            case RED -> mana.setRed(mana.getRed() + 1);
                            case GREEN -> mana.setGreen(mana.getGreen() + 1);
                            case COLORLESS -> mana.setColorless(mana.getColorless() + 1);
                            case GENERIC -> mana.setGeneric(mana.getGeneric() + 1);
                            default -> {}
                        }
                    }
                }
                return mana;
            }
        };
    }

    @Override
    public String toString() {
        return "ManaOptions{" + super.toString() + '}';
    }
}
