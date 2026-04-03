package mage.abilities.mana;

import mage.Mana;
import mage.constants.ManaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ManaCostSymbolParser}.
 *
 * @author mana-flow-solver
 */
class ManaCostSymbolParserTest {

    @Test
    @DisplayName("parse: single mono-color {G}")
    void parseSingleGreen() {
        List<ManaCostSymbol> symbols = ManaCostSymbolParser.parse("{G}");
        assertEquals(1, symbols.size());
        assertEquals(ManaCostSymbol.SymbolType.MONO_COLOR, symbols.getFirst().getType());
        assertTrue(symbols.getFirst().getColorOptions().contains(ManaType.GREEN));
    }

    @Test
    @DisplayName("parse: {1}{W} → [generic(1), monoColor(W)]")
    void parseGenericPlusWhite() {
        List<ManaCostSymbol> symbols = ManaCostSymbolParser.parse("{1}{W}");
        assertEquals(2, symbols.size());
        assertEquals(ManaCostSymbol.SymbolType.GENERIC, symbols.getFirst().getType());
        assertEquals(1, symbols.getFirst().getGenericCost());
        assertEquals(ManaCostSymbol.SymbolType.MONO_COLOR, symbols.get(1).getType());
        assertTrue(symbols.get(1).getColorOptions().contains(ManaType.WHITE));
    }

    @Test
    @DisplayName("parse: {2}{G}{G} → [generic(2), monoColor(G), monoColor(G)]")
    void parseTwoGreenPlusGeneric() {
        List<ManaCostSymbol> symbols = ManaCostSymbolParser.parse("{2}{G}{G}");
        assertEquals(3, symbols.size());
        assertEquals(ManaCostSymbol.SymbolType.GENERIC, symbols.getFirst().getType());
        assertEquals(2, symbols.getFirst().getGenericCost());
        assertEquals(ManaCostSymbol.SymbolType.MONO_COLOR, symbols.get(1).getType());
        assertEquals(ManaCostSymbol.SymbolType.MONO_COLOR, symbols.get(2).getType());
    }

    @Test
    @DisplayName("parse: {C} → [colorless]")
    void parseColorless() {
        List<ManaCostSymbol> symbols = ManaCostSymbolParser.parse("{C}");
        assertEquals(1, symbols.size());
        assertEquals(ManaCostSymbol.SymbolType.COLORLESS, symbols.getFirst().getType());
    }

    @Test
    @DisplayName("parse: {0} → empty (zero cost)")
    void parseZeroCost() {
        List<ManaCostSymbol> symbols = ManaCostSymbolParser.parse("{0}");
        assertTrue(symbols.isEmpty());
    }

    @Test
    @DisplayName("parse: null/empty string → empty")
    void parseNullOrEmpty() {
        assertTrue(ManaCostSymbolParser.parse(null).isEmpty());
        assertTrue(ManaCostSymbolParser.parse("").isEmpty());
        assertTrue(ManaCostSymbolParser.parse("   ").isEmpty());
    }

    @Test
    @DisplayName("parse: {W/U} → hybridColor(W, U)")
    void parseHybridWU() {
        List<ManaCostSymbol> symbols = ManaCostSymbolParser.parse("{W/U}");
        assertEquals(1, symbols.size());
        ManaCostSymbol s = symbols.getFirst();
        assertEquals(ManaCostSymbol.SymbolType.HYBRID_COLOR, s.getType());
        assertTrue(s.getColorOptions().contains(ManaType.WHITE));
        assertTrue(s.getColorOptions().contains(ManaType.BLUE));
    }

    @Test
    @DisplayName("parse: {2/U} → twobrid(BLUE, 2)")
    void parseTwobridU() {
        List<ManaCostSymbol> symbols = ManaCostSymbolParser.parse("{2/U}");
        assertEquals(1, symbols.size());
        ManaCostSymbol s = symbols.getFirst();
        assertEquals(ManaCostSymbol.SymbolType.HYBRID_GENERIC, s.getType());
        assertTrue(s.getColorOptions().contains(ManaType.BLUE));
        assertEquals(2, s.getGenericCost());
    }

    @Test
    @DisplayName("parse: {R/P} → phyrexian(RED)")
    void parsePhyrexianRed() {
        List<ManaCostSymbol> symbols = ManaCostSymbolParser.parse("{R/P}");
        assertEquals(1, symbols.size());
        ManaCostSymbol s = symbols.getFirst();
        assertEquals(ManaCostSymbol.SymbolType.PHYREXIAN, s.getType());
        assertTrue(s.getColorOptions().contains(ManaType.RED));
    }

    @Test
    @DisplayName("parse: {W/U/P} → hybridColor(W, U)  [phyrexian hybrid treated as hybrid for mana purposes]")
    void parseHybridPhyrexian() {
        List<ManaCostSymbol> symbols = ManaCostSymbolParser.parse("{W/U/P}");
        assertEquals(1, symbols.size());
        ManaCostSymbol s = symbols.getFirst();
        assertEquals(ManaCostSymbol.SymbolType.HYBRID_COLOR, s.getType());
        assertTrue(s.getColorOptions().contains(ManaType.WHITE));
        assertTrue(s.getColorOptions().contains(ManaType.BLUE));
    }

    @Test
    @DisplayName("parse: {C/R} → hybridColor(COLORLESS, RED)")
    void parseColorlessHybrid() {
        List<ManaCostSymbol> symbols = ManaCostSymbolParser.parse("{C/R}");
        assertEquals(1, symbols.size());
        ManaCostSymbol s = symbols.getFirst();
        assertEquals(ManaCostSymbol.SymbolType.HYBRID_COLOR, s.getType());
        assertTrue(s.getColorOptions().contains(ManaType.COLORLESS));
        assertTrue(s.getColorOptions().contains(ManaType.RED));
    }

    @Test
    @DisplayName("parse: {S} → generic(1)  [snow treated as generic]")
    void parseSingle_Snow() {
        List<ManaCostSymbol> symbols = ManaCostSymbolParser.parse("{S}");
        assertEquals(1, symbols.size());
        assertEquals(ManaCostSymbol.SymbolType.GENERIC, symbols.getFirst().getType());
        assertEquals(1, symbols.getFirst().getGenericCost());
    }

    @Test
    @DisplayName("parse: {X} → empty (X not yet announced)")
    void parseX() {
        // X before announcement has manaValue() == 0 → omitted
        assertTrue(ManaCostSymbolParser.parse("{X}").isEmpty());
    }

    @Test
    @DisplayName("parseInner: multi-digit numeric like 10 → generic(10)")
    void parseInnerLargeNumeric() {
        ManaCostSymbol s = ManaCostSymbolParser.parseInner("10");
        assertNotNull(s);
        assertEquals(ManaCostSymbol.SymbolType.GENERIC, s.getType());
        assertEquals(10, s.getGenericCost());
    }

    @Test
    @DisplayName("fromMana: {1}{G} → [generic(1), monoColor(G)]")
    void fromManaOneGenericOneGreen() {
        Mana mana = new Mana(0, 0, 0, 0, 1, 1, 0, 0); // 1 green, 1 generic
        List<ManaCostSymbol> symbols = ManaCostSymbolParser.fromMana(mana);
        assertEquals(2, symbols.size());
        assertTrue(symbols.stream().anyMatch(s -> s.getType() == ManaCostSymbol.SymbolType.MONO_COLOR
                && s.getColorOptions().contains(ManaType.GREEN)));
        assertTrue(symbols.stream().anyMatch(s -> s.getType() == ManaCostSymbol.SymbolType.GENERIC
                && s.getGenericCost() == 1));
    }
}
