package mage.abilities.mana;

import mage.constants.ManaType;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * A value class representing a single parsed symbol in a mana cost.
 * Used as the cost-side node for the mana flow graph.
 */
public final class ManaCostSymbol implements Serializable {

    public enum SymbolType {
        MONO_COLOR,
        HYBRID_COLOR,
        HYBRID_GENERIC,
        GENERIC,
        COLORLESS,
        PHYREXIAN
    }

    private final SymbolType type;
    private final Set<ManaType> colorOptions;
    private final int genericCost;

    private ManaCostSymbol(SymbolType type, Set<ManaType> colorOptions, int genericCost) {
        this.type = type;
        // Immutable defensive copy
        this.colorOptions = colorOptions.isEmpty()
                ? Collections.emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(colorOptions));
        this.genericCost = genericCost;
    }

    /** {W}, {U}, {B}, {R}, {G} */
    public static ManaCostSymbol monoColor(ManaType color) {
        return new ManaCostSymbol(SymbolType.MONO_COLOR, EnumSet.of(color), 0);
    }

    /** {W/U}, {B/R}, etc. */
    public static ManaCostSymbol hybridColor(ManaType color1, ManaType color2) {
        return new ManaCostSymbol(SymbolType.HYBRID_COLOR, EnumSet.of(color1, color2), 0);
    }

    /** {W/2}, {U/2}, etc. (twobrid) */
    public static ManaCostSymbol twobrid(ManaType color, int genericAlternativeCost) {
        return new ManaCostSymbol(SymbolType.HYBRID_GENERIC, EnumSet.of(color), genericAlternativeCost);
    }

    /** {1}, {2}, {X}, {0} */
    public static ManaCostSymbol generic(int amount) {
        return new ManaCostSymbol(SymbolType.GENERIC, EnumSet.noneOf(ManaType.class), amount);
    }

    /** {C} */
    public static ManaCostSymbol colorless() {
        return new ManaCostSymbol(SymbolType.COLORLESS, EnumSet.of(ManaType.COLORLESS), 0);
    }

    /** {W/P}, {U/P}, etc. */
    public static ManaCostSymbol phyrexian(ManaType color) {
        return new ManaCostSymbol(SymbolType.PHYREXIAN, EnumSet.of(color), 0);
    }

    /** Returns true for HYBRID_COLOR and HYBRID_GENERIC. */
    public boolean isHybrid() {
        return type == SymbolType.HYBRID_COLOR || type == SymbolType.HYBRID_GENERIC;
    }

    /**
     * Minimum number of pips required to pay this symbol.
     * For GENERIC this is the numeric value; for everything else it is 1.
     */
    public int minCost() {
        if (type == SymbolType.GENERIC) {
            return genericCost;
        }
        return 1;
    }

    /**
     * Maximum number of pips required to pay this symbol.
     * For HYBRID_GENERIC this is the genericCost alternative; otherwise equals minCost().
     */
    public int maxCost() {
        if (type == SymbolType.HYBRID_GENERIC) {
            return genericCost;
        }
        return minCost();
    }

    public SymbolType getType() {
        return type;
    }

    /** Immutable set of colors this symbol can be paid with. */
    public Set<ManaType> getColorOptions() {
        return colorOptions;
    }

    public int getGenericCost() {
        return genericCost;
    }

    @Override
    public String toString() {
        return "ManaCostSymbol{" + type + ", colors=" + colorOptions
                + (genericCost > 0 ? ", generic=" + genericCost : "") + '}';
    }
}

