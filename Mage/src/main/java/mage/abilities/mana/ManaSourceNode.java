package mage.abilities.mana;

import mage.Mana;
import mage.abilities.condition.Condition;
import mage.constants.ManaType;
import mage.game.Game;
import mage.players.Player;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a mana source (permanent or triggered mana).
 * Holds one or more {@link ManaAbilityOption} instances that would typically share the tap cost, only one
 * can be used per activation cycle.
 */
public final class ManaSourceNode implements Serializable {

    private final List<ManaAbilityOption> abilityOptions;
    private int maxActivations = Integer.MAX_VALUE;

    private ManaSourceNode(List<ManaAbilityOption> abilityOptions) {
        this.abilityOptions = List.copyOf(abilityOptions);
        for (ManaAbilityOption option : abilityOptions) {
            maxActivations = Math.min(maxActivations, option.getMaxActivationsPerTurn());
        }
    }

    /**
     * Builds a single node from all mana abilities on one permanent (no tap-cost grouping).
     */
    public static ManaSourceNode fromAbilitiesAll(List<ActivatedManaAbilityImpl> abilities, Game game, Player player) {
        List<ManaAbilityOption> options = new ArrayList<>();
        for (ActivatedManaAbilityImpl ability : abilities) {
            List<mage.Mana> netManas = ability.getNetMana(game);
            if (netManas.isEmpty()) {
                continue;
            }
            List<Condition> conditions = netManas.stream()
                    .filter(Mana::hasConditions)
                    .flatMap(m -> m.getConditions().stream())
                    .toList();
            options.addAll(ManaAbilityOption.fromAbility(ability, netManas, conditions, player));
        }
        return new ManaSourceNode(options);
    }

    /**
     * Convenience: single-ability permanent (the common case).
     */
    public static ManaSourceNode fromSingleAbility(ActivatedManaAbilityImpl ability, Game game, Player player) {
        return fromAbilitiesAll(Collections.singletonList(ability), game, player);
    }

    /**
     * Builds a source node from mana.
     */
    public static ManaSourceNode fromMana(Mana mana) {
        EnumMap<ManaType, Integer> map = new EnumMap<>(ManaType.class);
        if (mana.getWhite() > 0) map.put(ManaType.WHITE, mana.getWhite());
        if (mana.getBlue() > 0) map.put(ManaType.BLUE, mana.getBlue());
        if (mana.getBlack() > 0) map.put(ManaType.BLACK, mana.getBlack());
        if (mana.getRed() > 0) map.put(ManaType.RED, mana.getRed());
        if (mana.getGreen() > 0) map.put(ManaType.GREEN, mana.getGreen());
        if (mana.getColorless() > 0) map.put(ManaType.COLORLESS, mana.getColorless());

        ManaAbilityOption option = new ManaAbilityOption(
                UUID.randomUUID(),
                null,
                false,
                mana.count(),
                map,
                mana.getAny() > 0,
                Collections.emptyList(),
                mana.getConditions()
        );
        option.setScope(mana.getComparisonScope());
        return new ManaSourceNode(List.of(option));
    }

    /**
     * Build directly from pre-built options (used by solver internals and tests).
     */
    public static ManaSourceNode ofOptions(List<ManaAbilityOption> options) {
        return new ManaSourceNode(options);
    }

    /** All options have no additional mana cost. */
    public boolean isAllFree() {
        return abilityOptions.stream().noneMatch(ManaAbilityOption::hasCost);
    }

    /** All options require additional mana to activate. */
    public boolean isAllPaid() {
        return !abilityOptions.isEmpty()
                && abilityOptions.stream().allMatch(ManaAbilityOption::hasCost);
    }

    /** Has both free and paid options. */
    public boolean isMixed() {
        boolean hasFree = abilityOptions.stream().anyMatch(o -> !o.hasCost());
        boolean hasPaid = abilityOptions.stream().anyMatch(ManaAbilityOption::hasCost);
        return hasFree && hasPaid;
    }

    public boolean hasSingleOption() {
        return abilityOptions.size() == 1;
    }

    public boolean hasConditions() {
        return abilityOptions.stream().anyMatch(ManaAbilityOption::hasConditions);
    }

    /**
     * Returns the single option on this node.
     *
     * @throws IllegalStateException if this node has more than one option
     */
    public ManaAbilityOption getSingleOption() {
        if (!hasSingleOption()) {
            throw new IllegalStateException(
                    "ManaSourceNode has " + abilityOptions.size()
                            + " options; call getSingleOption() only when hasSingleOption() is true");
        }
        return abilityOptions.getFirst();
    }

    public List<ManaAbilityOption> getAbilityOptions() {
        return abilityOptions;
    }

    @Override
    public String toString() {
        return "ManaSourceNode{options=" + abilityOptions + '}';
    }

    public int getColorless() {
        int total = 0;
        total = getMax(ManaType.COLORLESS);
        return total;
    }

    public int count() {
        return abilityOptions.stream().mapToInt(ManaAbilityOption::getCapacity).sum();
    }

    public int getBlack() {
        return getMax(ManaType.BLACK);
    }

    public int getAny() {
        int total = 0;
        for (ManaAbilityOption option : abilityOptions) {
            if (option.isProducesAny()) {
                total += option.getCapacity();
            }
        }
        return total;
    }

    public int getBlue() {
        return getMax(ManaType.BLUE);
    }

    public int getRed() {
        return getMax(ManaType.RED);
    }

    public int getWhite() {
        return getMax(ManaType.WHITE);
    }

    public int getGreen() {
        return getMax(ManaType.GREEN);
    }

    public int getGeneric() {
        int total = 0;
        for (ManaAbilityOption option : abilityOptions) {
            if (option.getProducibleTypes().isEmpty()) {
                total += option.getCapacity();
            }
        }
        return total;
    }

    public int getCapacity() {
        int max = 0;
        for (ManaAbilityOption option : abilityOptions) {
            if (option.getCapacity() > max) {
                max = option.getCapacity();
            }
        }
        return max;
    }

    private int getMax(ManaType blue) {
        int max = 0;
        for (ManaAbilityOption option : abilityOptions) {
            if (option.getProducibleTypes().contains(blue)) {
                max = Math.max(max, option.getCapacity());
            }
        }
        return max;
    }

    public int getMaxActivations() {
        return maxActivations;
    }

    public void setMaxActivations(int maxActivations) {
        this.maxActivations = maxActivations;
    }
}
