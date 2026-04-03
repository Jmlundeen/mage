package org.mage.test.utils;

import mage.Mana;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.mana.ManaCostSymbol;
import mage.abilities.mana.ManaCostSymbolParser;
import mage.abilities.mana.ManaOptions;
import mage.game.Game;
import org.junit.Assert;

import java.util.List;

public class ManaOptionsTestUtils {

    public static String bear1W = "Silvercoat Lion"; // {1}{W}
    public static String bearG = "Basking Rootwalla"; // {G}
    public static String bear1 = "Augmenting Automaton"; // {1}
    public static String bear1G = "Balduvian Bears"; // {1}{G}
    public static String bear2C = "Matter Reshaper"; // {2}{C}

    public static void assertCanPay(String cost, ManaOptions manaOptions) {
        List<ManaCostSymbol> manaCosts = ManaCostSymbolParser.parse(cost);
        Assert.assertTrue("Expected to be able to pay " + cost + " with options " + manaOptions, manaOptions.canPayWithFlow(manaCosts));
    }

    public static void assertCanPay(String cost, ManaOptions manaOptions, Game game) {
        List<ManaCostSymbol> manaCosts = ManaCostSymbolParser.parse(cost);
        Assert.assertTrue("Expected to be able to pay " + cost + " with options " + manaOptions, manaOptions.canPayWithFlow(manaCosts, game, null));
    }

    public static void assertCannotPay(String cost, ManaOptions manaOptions, Game game) {
        List<ManaCostSymbol> manaCosts = ManaCostSymbolParser.parse(cost);
        Assert.assertFalse("Expected to NOT be able to pay " + cost + " with options " + manaOptions, manaOptions.canPayWithFlow(manaCosts, game, null));
    }
    /**
     * Returns {@code true} if {@code searchMana} matches any entry in the full enumeration
     * of possible mana totals for the given options.
     *
     * <p>Comparison is semantic (field-by-field via {@link Mana#equals}), not string-based,
     * so the pip order in {@code searchMana} does not matter.  {@code {Any}} tokens are
     * counted separately and added to the {@code any} field of the target before comparison.
     */
    public static boolean manaOptionsContain(String searchMana, ManaOptions manaOptions) {
        Mana target = parseManaString(searchMana);
        for (Mana mana : manaOptions.toManaList()) {
            if (mana.equals(target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses a human-readable mana string like {@code "{R}{G}{G}{W}"} or
     * {@code "{C}{G}{Any}"} into a {@link Mana} object suitable for comparison.
     *
     * <p>{@code {Any}} tokens are stripped and counted before the remainder is fed
     * to {@link ManaCostsImpl} (which does not understand {@code {Any}}).
     */
    private static Mana parseManaString(String searchMana) {
        // Count and remove {Any} tokens — ManaCostsImpl cannot parse them.
        int anyCount = 0;
        String remaining = searchMana;
        while (remaining.contains("{Any}")) {
            anyCount++;
            remaining = remaining.replaceFirst("\\{Any}", "");
        }

        Mana result = remaining.isEmpty()
                ? new Mana()
                : new ManaCostsImpl<>(remaining).getMana();
        if (anyCount > 0) {
            result.setAny(result.getAny() + anyCount);
        }
        return result;
    }

    public static void assertManaOptions(String searchMana, ManaOptions manaOptions) {
        if (!manaOptionsContain(searchMana, manaOptions)) {
            Assert.fail("Can't find '" + searchMana + "' in " + manaOptions.toManaList());
        }
    }
}
