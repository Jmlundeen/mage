package mage.abilities.mana;

import mage.Mana;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.costs.mana.ManaCost;
import mage.abilities.costs.mana.ManaCosts;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.costs.mana.VariableManaCost;
import mage.constants.ManaType;
import mage.filter.FilterMana;
import mage.players.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Converts mana cost representations into {@link ManaCostSymbol} lists for the flow solver.
 *
 * <p>Three entry points:
 * <ol>
 *   <li>{@link #parse(String)} — accepts a raw cost string such as {@code "{2}{G}{2/U}"}.</li>
 *   <li>{@link #fromManaCosts(ManaCosts, Player, Ability)} — accepts a live {@link ManaCosts} object (preserves
 *       hybrid structure by iterating individual {@link ManaCost} instances).</li>
 *   <li>{@link #fromMana(Mana)} — converts an already-resolved {@link Mana} aggregate
 *       (delegates to {@link ManaAbilityOption#manaToSymbols}).</li>
 * </ol>
 *
 * <p>Symbol mapping:
 * <pre>
 *  {W}/{U}/{B}/{R}/{G}          → monoColor
 *  {C}                          → colorless
 *  {1}/{2}/{3}/…                → generic(n)
 *  {0}                          → (omitted — zero cost)
 *  {X}                          → generic(xvalue) if already set, else minimum x
 *  {S}                          → generic(1)  (snow, paid by any mana from a snow source)
 *  {W/U}/{B/R}/… (hybrid)      → hybridColor
 *  {2/W}/{2/U}/… (twobrid)     → twobrid(color, 2)
 *  {C/R}/{C/U}/… (colorless H) → hybridColor(COLORLESS, color)
 *  {W/P}/{U/P}/… (phyrexian)   → phyrexian(color)
 *  {W/U/P}/… (hybrid Phyrex.)  → hybridColor(color1, color2)
 * </pre>
 */
public final class ManaCostSymbolParser {

    private ManaCostSymbolParser() {
    }

    /**
     * Parses a mana cost string (e.g. {@code "{2}{G}{2/U}"}) into a list of
     * {@link ManaCostSymbol} instances.
     *
     * @param costString the cost string, e.g. {@code "{1}{W}{W/U}"}; may be {@code null}
     * @return immutable list; empty if the string is null, blank, or zero-cost
     */
    public static List<ManaCostSymbol> parse(String costString) {
        if (costString == null || costString.isBlank()) {
            return Collections.emptyList();
        }
        // Leverage the existing parser to get typed ManaCost objects, then convert
        ManaCostsImpl<?> parsed = new ManaCostsImpl<>(costString);
        return fromManaCosts(parsed, null, null);
    }

    /**
     * Parses a mana cost string (e.g. {@code "{2}{G}{2/U}"}) into a list of
     * {@link ManaCostSymbol} instances.
     *
     * @param costString the cost string, e.g. {@code "{1}{W}{W/U}"}; may be {@code null}
     * @param phyrexianColors the colors of phyrexian mana the player can pay
     * @param canPayLife whether to include phyrexian symbols
     * @return immutable list; empty if the string is null, blank, or zero-cost
     */
    public static List<ManaCostSymbol> parse(String costString, FilterMana phyrexianColors, boolean canPayLife) {
        if (costString == null || costString.isBlank()) {
            return Collections.emptyList();
        }
        // Leverage the existing parser to get typed ManaCost objects, then convert
        ManaCostsImpl<?> parsed = new ManaCostsImpl<>(costString);
        return fromManaCosts(parsed, phyrexianColors, canPayLife);
    }

    /**
     * Converts a live {@link ManaCosts} instance into symbols. Preserves hybrid
     * structure by iterating individual {@link ManaCost} elements rather than using
     * the aggregated {@link Mana} value.
     *
     * @param costs   the cost list; may be {@code null}
     * @param player
     * @param ability
     * @return immutable list; empty if null or empty
     */
    public static List<ManaCostSymbol> fromManaCosts(ManaCosts<? extends ManaCost> costs, Player player, Ability ability) {
        if (costs == null || costs.isEmpty()) {
            return Collections.emptyList();
        }
        boolean canPayLife = true;
        if (player != null && ability != null) {
            canPayLife = player.canPayLifeCost(ability);
        }
        FilterMana phyrexianColors = player != null ? player.getPhyrexianColors() : new FilterMana();
        return fromManaCosts(costs, phyrexianColors, canPayLife);
    }

    /**
     * Converts a live {@link ManaCosts} instance into symbols. Preserves hybrid
     * structure by iterating individual {@link ManaCost} elements rather than using
     * the aggregated {@link Mana} value.
     *
     * @param costs   the cost list; may be {@code null}
     * @param phyrexianColors the colors of phyrexian mana the player can pay
     * @param canPayLife whether to include phyrexian symbols (if false, phyrexian costs are omitted as if paid with life)
     * @return immutable list; empty if null or empty
     */
    public static List<ManaCostSymbol> fromManaCosts(ManaCosts<? extends ManaCost> costs, FilterMana phyrexianColors, boolean canPayLife) {
        if (costs == null || costs.isEmpty()) {
            return Collections.emptyList();
        }
        List<ManaCostSymbol> result = new ArrayList<>(costs.size() * 2);
        for (ManaCost cost : costs) {
            appendSymbolsForCost(cost, result, canPayLife, phyrexianColors);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Converts a resolved {@link Mana} aggregate into symbols. Hybrid information is
     * already lost at this point (each color is treated as a separate mono-color symbol).
     * Use {@link #fromManaCosts} when hybrid structure matters.
     *
     * @param mana the resolved mana aggregate
     * @return immutable list
     */
    public static List<ManaCostSymbol> fromMana(Mana mana) {
        return ManaAbilityOption.manaToSymbols(mana);
    }

    /**
     * Appends one or more {@link ManaCostSymbol}s for the given {@link ManaCost} by
     * parsing its {@link ManaCost#getText()} token(s).
     */
    private static void appendSymbolsForCost(ManaCost cost, List<ManaCostSymbol> out, boolean canPayLife, FilterMana phyrexianColors) {
        if (cost instanceof VariableManaCost variableCost) {
            int xValue = variableCost.wasAnnounced() ? variableCost.getAmount() : variableCost.getMinX();
            int instances = variableCost.getXInstancesCount();
            for (int i = 0; i < instances; i++) {
                if (xValue > 0) {
                    out.add(ManaCostSymbol.generic(xValue));
                }
            }
            return;
        }
        if (cost.isPhyrexian() && canPayLife) {
            return;
        }
        String text = cost.getText();
        if (text == null || text.isBlank()) {
            return;
        }
        parseTokens(text, out, canPayLife, phyrexianColors);
    }

    /**
     * Splits a token string such as {@code "{W/U}{3}"} into {@code {...}} tokens and
     * appends the resulting symbols to {@code out}.
     */
    private static void parseTokens(String text, List<ManaCostSymbol> out, boolean canPayLife, FilterMana phyrexianColors) {
        int i = 0;
        while (i < text.length()) {
            int open = text.indexOf('{', i);
            if (open == -1) break;
            int close = text.indexOf('}', open);
            if (close == -1) break;
            String inner = text.substring(open + 1, close);
            ManaCostSymbol sym = parseInner(inner);
            // if the symbol is mono color and matches a phyrexian color, convert to phyrexian
            if (sym != null && phyrexianColors != null && sym.getType() == ManaCostSymbol.SymbolType.MONO_COLOR) {
                ManaType color = sym.getColorOptions().iterator().next();
                for (ObjectColor phyColor : phyrexianColors.getColors()) {
                    boolean match = (phyColor.isWhite() && color == ManaType.WHITE)
                            || (phyColor.isBlue() && color == ManaType.BLUE)
                            || (phyColor.isBlack() && color == ManaType.BLACK)
                            || (phyColor.isRed() && color == ManaType.RED)
                            || (phyColor.isGreen() && color == ManaType.GREEN);
                    if (match && canPayLife) {
                        sym = null;
                        break;
                    }
                }
            }
            if (sym != null) {

                out.add(sym);
            }
            i = close + 1;
        }
    }

    /**
     * Converts the inner text of a single {@code {...}} token to a {@link ManaCostSymbol}.
     *
     * @param inner text without braces, e.g. {@code "W"}, {@code "2/U"}, {@code "W/U/P"}
     * @return the symbol, or {@code null} for zero-cost or unknown tokens
     */
    static ManaCostSymbol parseInner(String inner) {
        if (inner == null || inner.isEmpty()) {
            return null;
        }

        if (inner.endsWith("/P")) {
            String body = inner.substring(0, inner.length() - 2); // strip /P
            if (body.contains("/")) {
                // Hybrid phyrexian: W/U/P → hybridColor(W, U)
                String[] parts = body.split("/", 2);
                ManaType t1 = charToManaType(parts[0]);
                ManaType t2 = charToManaType(parts[1]);
                if (t1 != null && t2 != null) {
                    return ManaCostSymbol.hybridColor(t1, t2);
                }
            } else {
                // Mono phyrexian: W/P → phyrexian(W)
                ManaType t = charToManaType(body);
                if (t != null) {
                    return ManaCostSymbol.phyrexian(t);
                }
            }
            return null;
        }

        if (inner.contains("/")) {
            String[] parts = inner.split("/", 2);
            // {2/W} — numeric first = twobrid
            if (isNumeric(parts[0])) {
                int n = Integer.parseInt(parts[0]);
                ManaType t = charToManaType(parts[1]);
                if (t != null) {
                    return ManaCostSymbol.twobrid(t, n);
                }
                return null;
            }
            // {C/R} — colorless hybrid
            if ("C".equals(parts[0])) {
                ManaType t = charToManaType(parts[1]);
                if (t != null) {
                    return ManaCostSymbol.hybridColor(ManaType.COLORLESS, t);
                }
                return null;
            }
            // {W/U} — standard hybrid
            ManaType t1 = charToManaType(parts[0]);
            ManaType t2 = charToManaType(parts[1]);
            if (t1 != null && t2 != null) {
                return ManaCostSymbol.hybridColor(t1, t2);
            }
            return null;
        }

        if (isNumeric(inner)) {
            int n = Integer.parseInt(inner);
            return n > 0 ? ManaCostSymbol.generic(n) : null;
        }

        if (inner.length() == 1) {
            char c = inner.charAt(0);
            switch (c) {
                case 'C': return ManaCostSymbol.colorless();
                case 'S': return ManaCostSymbol.generic(1); // snow = any mana from snow source
                case 'X': return null;                      // X not yet announced; omit
                default:
                    ManaType t = charToManaType(inner);
                    if (t != null) {
                        return ManaCostSymbol.monoColor(t);
                    }
            }
        }

        return null; // unknown token
    }

    /**
     * Maps a single-character string (or char) to a {@link ManaType}, or {@code null}
     * if unrecognized.
     */
    private static ManaType charToManaType(String s) {
        if (s == null || s.isEmpty()) return null;
        return charToManaType(s.charAt(0));
    }

    static ManaType charToManaType(char c) {
        return switch (c) {
            case 'W' -> ManaType.WHITE;
            case 'U' -> ManaType.BLUE;
            case 'B' -> ManaType.BLACK;
            case 'R' -> ManaType.RED;
            case 'G' -> ManaType.GREEN;
            default -> null;
        };
    }

    private static boolean isNumeric(String s) {
        if (s == null || s.isEmpty()) return false;
        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }
}

