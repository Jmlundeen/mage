package mage.abilities.mana;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.condition.Condition;
import mage.abilities.costs.mana.ManaCost;
import mage.abilities.mana.conditional.ManaCondition;
import mage.constants.ManaType;
import mage.filter.Filter;
import mage.game.Game;
import mage.players.Player;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a single activatable mana ability on a permanent.
 * One permanent with two mana abilities produces two {@code ManaAbilityOption} instances.
 * This is the source-side node used during both activation-cost resolution and the
 * spell-payment flow graph.
 *
 * <h2>producibleMap semantics</h2>
 * <p>Each key is a {@link ManaType} the ability can produce.  The value encodes
 * whether the amount of that type is <em>fixed</em> (value&nbsp;&gt;&nbsp;0) or
 * <em>flexible</em> (value&nbsp;=&nbsp;0).</p>
 * <ul>
 *   <li><b>Fixed</b> — the ability always produces exactly {@code value} pips of that type.
 *       Example: {@code {T}, Sac: Add {R}{W}} → {@code {RED=1, WHITE=1}}.
 *       Capacity is the sum of all values.</li>
 *   <li><b>Flexible</b> — the player may distribute up to {@code capacity} pips among the
 *       0-valued types.  Example: "Add two mana in any combination of {W} and {U}"
 *       → {@code {WHITE=0, BLUE=0}, capacity=2}.</li>
 * </ul>
 *
 * @author jmlundeen
 */
public final class ManaAbilityOption implements Serializable {

    private final UUID optionId = UUID.randomUUID();
    private final UUID abilityId;
    private UUID sourceId;
    /**
     * If this mana option is triggered mana that depends on another ability being activated,
     * this field holds the triggering ability's ID. Otherwise null.
     */
    private final UUID triggeringAbilityId;
    /**
     * If this ability's output depends on the current mana pool state.
     * Example: Doubling Cube doubles whatever mana is currently in the pool.
     * These abilities must be evaluated dynamically using getNetMana(game, possibleManaInPool).
     */
    private final boolean isPoolDependent;
    private final int capacity;
    /**
     * Map of producible types to their fixed pip count.
     * Value 0 = flexible (player chooses), value &gt; 0 = static (exact pips).
     */
    private final EnumMap<ManaType, Integer> producibleMap;
    private final boolean producesAny;
    /** Full mana activation cost as typed symbols, e.g. [{1},{G}] for {T}{1}{G}: ... */
    private boolean hasTapCost;
    private final List<ManaCostSymbol> activationCost;
    private final List<Condition> conditions;
    private Filter.ComparisonScope scope = Filter.ComparisonScope.All;
    private int maxActivationsPerTurn = Integer.MAX_VALUE;

    private ManaAbilityOption(UUID abilityId,
                               UUID triggeringAbilityId,
                               boolean isPoolDependent,
                               int capacity,
                               EnumMap<ManaType, Integer> producibleMap,
                               boolean producesAny,
                               List<ManaCostSymbol> activationCost,
                               List<Condition> conditions) {
        this.abilityId = abilityId;
        this.triggeringAbilityId = triggeringAbilityId;
        this.isPoolDependent = isPoolDependent;
        this.capacity = capacity;
        this.producibleMap = new EnumMap<>(producibleMap);
        this.producesAny = producesAny;
        this.activationCost = List.copyOf(activationCost);
        this.conditions = Collections.unmodifiableList(conditions);
    }

    /**
     * Builds one {@code ManaAbilityOption} per element in {@code netManas}.
     * Each element is a distinct, mutually-exclusive alternative the ability can produce
     * (e.g. {@code [UU, WW]} → two options with static maps {@code {BLUE=2}} and {@code {WHITE=2}}).
     * <p>
     * All entries in the returned map are <b>static</b> (value&nbsp;&gt;&nbsp;0) because
     * {@code getNetMana()} reports exact pip counts.
     */
    public static List<ManaAbilityOption> fromAbility(ActivatedManaAbilityImpl ability,
                                                      List<Mana> netManas,
                                                      List<Condition> conditions, Player player) {
        List<ManaCostSymbol> activationSymbols = ability.getManaCosts().isEmpty()
                ? Collections.emptyList()
                : ManaCostSymbolParser.fromManaCosts(ability.getManaCosts(), player.canPayLifeCost(ability));

        // Check if this ability is pool-dependent (e.g., Doubling Cube)
        boolean isPoolDependent = ability.isPoolDependant();

        List<ManaAbilityOption> result = new ArrayList<>();

        // Pool-dependent abilities may have empty netManas when called without a pool state
        // Create a placeholder option that will be evaluated dynamically later
        if (isPoolDependent && netManas.isEmpty()) {
            // Create a placeholder with no static mana output (empty map, capacity 0)
            // This will be recognized and handled specially by the flow solver
            EnumMap<ManaType, Integer> emptyMap = new EnumMap<>(ManaType.class);
            ManaAbilityOption option = new ManaAbilityOption(
                ability.getId(),
                null,  // not triggered
                true,  // pool dependent
                0,     // no static capacity
                emptyMap,
                false, // not any
                activationSymbols,
                conditions
            );
            option.setSourceId(ability.getSourceId());
            if (ability.hasTapCost()) {
                option.setHasTapCost(true);
                option.setMaxActivationsPerTurn(1);
            }
            result.add(option);
            return result;
        }

        for (Mana m : netManas) {
            EnumMap<ManaType, Integer> map = new EnumMap<>(ManaType.class);
            boolean anyFlag = false;
            if (m.getWhite() > 0)     map.put(ManaType.WHITE, m.getWhite());
            if (m.getBlue() > 0)      map.put(ManaType.BLUE, m.getBlue());
            if (m.getBlack() > 0)     map.put(ManaType.BLACK, m.getBlack());
            if (m.getRed() > 0)       map.put(ManaType.RED, m.getRed());
            if (m.getGreen() > 0)     map.put(ManaType.GREEN, m.getGreen());
            if (m.getColorless() > 0) map.put(ManaType.COLORLESS, m.getColorless());
            if (m.getAny() > 0)       anyFlag = true;
            ManaAbilityOption option = new ManaAbilityOption(
                ability.getId(),
                null,  // not triggered
                isPoolDependent,
                m.count(),
                map,
                anyFlag,
                activationSymbols,
                conditions
            );
            option.setScope(m.getConditionScope());
            option.setSourceId(ability.getSourceId());
            if (ability.hasTapCost()) {
                option.setHasTapCost(true);
                option.setMaxActivationsPerTurn(1);
            }
            result.add(option);
        }
        return result;
    }

    public static List<ManaAbilityOption> fromNetMana(UUID abilityId, List<Mana> netManas) {
        return fromNetMana(abilityId, null, netManas);
    }

    /**
     * Creates triggered mana options that depend on a triggering ability.
     * Used for effects like Sasaya's Essence that add extra mana when another ability is activated.
     *
     * @param triggeredManaAbilityId the ID for these triggered mana options
     * @param triggeringAbilityId the ID of the ability that must be activated for this mana to be available
     * @param netManas the mana produced by the trigger
     * @return list of triggered mana options
     */
    public static List<ManaAbilityOption> fromNetMana(UUID triggeredManaAbilityId, UUID triggeringAbilityId, List<Mana> netManas) {
        List<ManaAbilityOption> result = new ArrayList<>();
        for (Mana m : netManas) {
            EnumMap<ManaType, Integer> map = new EnumMap<>(ManaType.class);
            boolean anyFlag = false;
            if (m.getWhite() > 0)     map.put(ManaType.WHITE, m.getWhite());
            if (m.getBlue() > 0)      map.put(ManaType.BLUE, m.getBlue());
            if (m.getBlack() > 0)     map.put(ManaType.BLACK, m.getBlack());
            if (m.getRed() > 0)       map.put(ManaType.RED, m.getRed());
            if (m.getGreen() > 0)     map.put(ManaType.GREEN, m.getGreen());
            if (m.getColorless() > 0) map.put(ManaType.COLORLESS, m.getColorless());
            if (m.getAny() > 0)       anyFlag = true;
            result.add(new ManaAbilityOption(triggeredManaAbilityId, triggeringAbilityId, false, m.count(), map, anyFlag,
                    Collections.emptyList(), m.getConditions()));
        }
        return result;
    }

    /**
     * Primary direct constructor — builds a <b>flexible</b> option (all map values&nbsp;=&nbsp;0)
     * with an explicit capacity.  Used in tests and solver internals.
     */
    public static ManaAbilityOption of(UUID abilityId,
                                        int capacity,
                                        Set<ManaType> producibleTypes,
                                        boolean producesAny,
                                        List<ManaCostSymbol> activationCost) {
        EnumMap<ManaType, Integer> map = new EnumMap<>(ManaType.class);
        for (ManaType t : producibleTypes) {
            map.put(t, 0);
        }
        return new ManaAbilityOption(abilityId, null, false, capacity, map, producesAny, activationCost,
                Collections.emptyList());
    }

    /**
     * Convenience overload for pure-generic activation costs (e.g. {@code {T}{2}: ...}).
     */
    public static ManaAbilityOption of(UUID abilityId,
                                        int capacity,
                                        Set<ManaType> producibleTypes,
                                        boolean producesAny,
                                        int genericCost) {
        List<ManaCostSymbol> cost = genericCost > 0
                ? List.of(ManaCostSymbol.generic(genericCost))
                : Collections.emptyList();
        return of(abilityId, capacity, producibleTypes, producesAny, cost);
    }

    /**
     * Creates a simple synthetic mana option for a specific mana type and amount.
     * Used when converting produced mana from pool-dependent abilities into options.
     *
     * @param sourceAbilityId the ability that produced this mana
     * @param type the mana type produced
     * @param amount the amount of mana produced
     * @return a new static mana option
     */
    public static ManaAbilityOption createSyntheticOption(UUID sourceAbilityId, ManaType type, int amount) {
        EnumMap<ManaType, Integer> map = new EnumMap<>(ManaType.class);
        map.put(type, amount);
        return new ManaAbilityOption(
                sourceAbilityId,
                null,  // not triggered
                false, // not pool dependent
                amount,
                map,
                false, // not any
                Collections.emptyList(), // no activation cost
                Collections.emptyList()  // no conditions
        );
    }

    /**
     * Builder for flexible construction of ManaAbilityOption with all fields.
     */
    public static Builder builder() {
        return new Builder();
    }

    public int getMaxActivationsPerTurn() {
        return maxActivationsPerTurn;
    }

    public void setMaxActivationsPerTurn(int maxActivationsPerTurn) {
        this.maxActivationsPerTurn = maxActivationsPerTurn;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public void setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
    }

    public ManaAbilityOption withCapacity(int difference) {
        return new ManaAbilityOption(abilityId, triggeringAbilityId, isPoolDependent, difference, producibleMap, producesAny,
                activationCost, conditions);
    }

    public boolean isHasTapCost() {
        return hasTapCost;
    }

    public UUID getOptionId() {
        return optionId;
    }

    public UUID getTriggeringAbilityId() {
        return triggeringAbilityId;
    }

    /**
     * Returns true if this is triggered mana that depends on another ability being activated.
     */
    public boolean isTriggeredMana() {
        return triggeringAbilityId != null;
    }

    /**
     * Returns true if this ability's output depends on the current pool state.
     * Pool-dependent abilities like Doubling Cube must be evaluated dynamically.
     */
    public boolean isPoolDependent() {
        return isPoolDependent;
    }

    public Filter.ComparisonScope getScope() {
        return scope;
    }

    public void setScope(Filter.ComparisonScope scope) {
        this.scope = scope;
    }

    /**
     * Builder for flexible construction of ManaAbilityOption with all fields.
     */
    public static class Builder {
        private UUID abilityId;
        private int capacity = 0;
        private EnumMap<ManaType, Integer> producibleMap = new EnumMap<>(ManaType.class);
        private boolean producesAny = false;
        private List<ManaCostSymbol> activationCost = Collections.emptyList();
        private List<Condition> conditions = Collections.emptyList();

        public Builder abilityId(UUID abilityId) {
            this.abilityId = abilityId;
            return this;
        }

        public Builder capacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        /** Adds types as <b>flexible</b> entries (value&nbsp;=&nbsp;0). */
        public Builder producibleTypes(Set<ManaType> producibleTypes) {
            for (ManaType t : producibleTypes) {
                this.producibleMap.put(t, 0);
            }
            return this;
        }

        /** Adds a single <b>static</b> entry (value&nbsp;&gt;&nbsp;0). */
        public Builder staticType(ManaType type, int amount) {
            this.producibleMap.put(type, amount);
            return this;
        }

        /** Replaces the entire map. */
        public Builder producibleMap(Map<ManaType, Integer> map) {
            this.producibleMap = new EnumMap<>(map);
            return this;
        }

        public Builder producesAny(boolean producesAny) {
            this.producesAny = producesAny;
            return this;
        }

        public Builder activationCost(List<ManaCostSymbol> activationCost) {
            this.activationCost = activationCost;
            return this;
        }

        public Builder conditions(List<Condition> conditions) {
            this.conditions = conditions;
            return this;
        }

        public ManaAbilityOption build() {
            return new ManaAbilityOption(abilityId, null, false, capacity, producibleMap, producesAny,
                    activationCost, conditions);
        }
    }

    /**
     * Converts a {@link Mana} aggregate (as returned by {@code ManaCosts.getMana()}) into
     * a flat list of {@link ManaCostSymbol} instances — one per pip for colored/colorless,
     * one batched symbol for generic.
     */
    public static List<ManaCostSymbol> manaToSymbols(Mana mana) {
        if (mana.count() == 0) {
            return Collections.emptyList();
        }
        List<ManaCostSymbol> symbols = new ArrayList<>();
        for (int i = 0; i < mana.getWhite(); i++)     symbols.add(ManaCostSymbol.monoColor(ManaType.WHITE));
        for (int i = 0; i < mana.getBlue(); i++)      symbols.add(ManaCostSymbol.monoColor(ManaType.BLUE));
        for (int i = 0; i < mana.getBlack(); i++)     symbols.add(ManaCostSymbol.monoColor(ManaType.BLACK));
        for (int i = 0; i < mana.getRed(); i++)       symbols.add(ManaCostSymbol.monoColor(ManaType.RED));
        for (int i = 0; i < mana.getGreen(); i++)     symbols.add(ManaCostSymbol.monoColor(ManaType.GREEN));
        for (int i = 0; i < mana.getColorless(); i++) symbols.add(ManaCostSymbol.colorless());
        if (mana.getGeneric() > 0) symbols.add(ManaCostSymbol.generic(mana.getGeneric()));
        return Collections.unmodifiableList(symbols);
    }

    /**
     * Returns true if this option can produce the requested mana type.
     * <ul>
     *   <li>Always true if {@code producesAny}.</li>
     *   <li>For {@link ManaType#GENERIC}: true for any source that produces mana (including
     *       colorless), because generic costs can be paid with any mana type.</li>
     *   <li>Otherwise: direct map-key membership.</li>
     * </ul>
     */
    public boolean canProduce(ManaType type) {
        if (producesAny) {
            return true;
        }
        if (type == ManaType.GENERIC) {
            return !producibleMap.isEmpty();
        }
        return producibleMap.containsKey(type);
    }

    public boolean canProduce(ManaCostSymbol symbol) {
        if (producesAny) {
            return true;
        }
        if (symbol.getType() == ManaCostSymbol.SymbolType.GENERIC) {
            return !producibleMap.isEmpty();
        }
        return symbol.getColorOptions().stream().anyMatch(this::canProduce);
    }

    /**
     * {@code true} when every entry in the map has a fixed amount&nbsp;&gt;&nbsp;0
     * (and the option is not pure-any).  The output shape is fully determined.
     */
    public boolean isStatic() {
        if (producesAny || producibleMap.isEmpty()) {
            return false;
        }
        for (int v : producibleMap.values()) {
            if (v == 0) return false;
        }
        return true;
    }

    /**
     * {@code true} when at least one entry has value&nbsp;=&nbsp;0, meaning the
     * player may choose how many pips of that type to produce.
     */
    public boolean isFlexible() {
        return producibleMap.values().stream().anyMatch(v -> v == 0);
    }

    /**
     * Builds a {@link Mana} from the static entries in the map.
     * Only meaningful when {@link #isStatic()} is {@code true}; for flexible options
     * the result omits the flexible portion.
     */
    public Mana toMana() {
        Mana m = new Mana();
        for (Map.Entry<ManaType, Integer> e : producibleMap.entrySet()) {
            if (e.getValue() > 0) {
                addManaType(m, e.getKey(), e.getValue());
            }
        }
        return m;
    }

    /** Returns true if activating this ability requires paying additional mana. */
    public boolean hasCost() {
        return !activationCost.isEmpty();
    }

    /** Returns true if this ability has attached conditions. */
    public boolean hasConditions() {
        return !conditions.isEmpty();
    }

    public boolean applyConditions(Ability ability, Game game, UUID playerId, ManaCost cost) {
        for (Condition condition : conditions) {
            boolean applied = (condition instanceof ManaCondition manaCondition)
                    ? manaCondition.apply(game, ability, playerId, cost)
                    : condition.apply(game, ability);
            if (!applied) {
                if (scope == Filter.ComparisonScope.All) {
                    return false;
                }
            } else {
                if (scope == Filter.ComparisonScope.Any) {
                    return true;
                }
            }
        }
        return scope == Filter.ComparisonScope.All;
    }

    public UUID getAbilityId() {
        return abilityId;
    }

    public int getCapacity() {
        return capacity;
    }

    /**
     * Returns the set of producible mana types (map keys) as an {@link EnumSet}.
     * Callers that only need containment checks can use this; callers that need
     * the fixed/flexible distinction should use {@link #getProducibleMap()}.
     */
    public EnumSet<ManaType> getProducibleTypes() {
        return producibleMap.isEmpty()
                ? EnumSet.noneOf(ManaType.class)
                : EnumSet.copyOf(producibleMap.keySet());
    }

    /** Returns an unmodifiable view of the full producible map. */
    public Map<ManaType, Integer> getProducibleMap() {
        return Collections.unmodifiableMap(producibleMap);
    }

    public boolean isProducesAny() {
        return producesAny;
    }

    /**
     * Returns the full typed activation cost as a list of {@link ManaCostSymbol} instances.
     * Empty list means this ability is free to activate (no mana cost).
     */
    public List<ManaCostSymbol> getActivationCost() {
        return activationCost;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public boolean hasTapCost() {
        return hasTapCost;
    }

    public void setHasTapCost(boolean hasTapCost) {
        this.hasTapCost = hasTapCost;
    }

    @Override
    public String toString() {
        return "ManaAbilityOption{cap=" + capacity + ", map=" + producibleMap
                + (producesAny ? ", any" : "")
                + (!activationCost.isEmpty() ? ", cost=" + activationCost : "") + '}';
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
}
