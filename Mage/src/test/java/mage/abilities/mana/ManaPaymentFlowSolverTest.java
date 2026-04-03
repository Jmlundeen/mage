package mage.abilities.mana;

import mage.Mana;
import mage.constants.ManaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the complete mana payment flow solver stack (Task 8).
 * All tests use constructed {@link ManaCostSymbol} and {@link ManaSourceNode} objects —
 * no game engine required.
 *
 * @author mana-flow-solver
 */
class ManaPaymentFlowSolverTest {

    /** Creates a free source option (no activation cost) producing the given types. */
    private static ManaAbilityOption freeOption(int capacity, ManaType... types) {
        EnumSet<ManaType> set = types.length == 0
                ? EnumSet.noneOf(ManaType.class)
                : EnumSet.of(types[0], types);
        return ManaAbilityOption.of(UUID.randomUUID(), capacity, set, false, 0);
    }

    /** Creates a paid option with a pure-generic activation cost producing the given types. */
    private static ManaAbilityOption paidOption(int capacity, int activationCost, ManaType... types) {
        EnumSet<ManaType> set = types.length == 0
                ? EnumSet.noneOf(ManaType.class)
                : EnumSet.of(types[0], types);
        return ManaAbilityOption.of(UUID.randomUUID(), capacity, set, false, activationCost);
    }

    /**
     * Creates a paid option with a fully typed activation cost (colored + generic).
     * E.g. {@code paidOptionColored(2, List.of(generic(1), monoColor(GREEN)), ManaType.BLUE)}
     * models {@code {T}{1}{G}: Add {U}{U}}.
     */
    private static ManaAbilityOption paidOptionColored(int capacity,
                                                         List<ManaCostSymbol> activationCost,
                                                         ManaType... types) {
        EnumSet<ManaType> set = types.length == 0
                ? EnumSet.noneOf(ManaType.class)
                : EnumSet.of(types[0], types);
        return ManaAbilityOption.of(UUID.randomUUID(), capacity, set, false, activationCost);
    }

    /** Wraps a single option in a ManaSourceNode. */
    private static ManaSourceNode node(ManaAbilityOption option) {
        return ManaSourceNode.ofOptions(List.of(option));
    }

    /** Calls the solver's canPay entry point. */
    private static boolean canPay(List<ManaCostSymbol> cost, List<ManaSourceNode> sources) {
        return ManaPaymentFlowSolver.canPay(cost, sources);
    }

    @Test
    @DisplayName("TC-1: {W} payable with 1×W source")
    void tc1_monoWhitePaidByWhiteSource() {
        List<ManaCostSymbol> cost = List.of(ManaCostSymbol.monoColor(ManaType.WHITE));
        List<ManaSourceNode> sources = List.of(node(freeOption(1, ManaType.WHITE)));
        assertTrue(canPay(cost, sources));
    }

    @Test
    @DisplayName("TC-2: {W} not payable with 1×U source")
    void tc2_monoWhiteNotPaidByBlueSource() {
        List<ManaCostSymbol> cost = List.of(ManaCostSymbol.monoColor(ManaType.WHITE));
        List<ManaSourceNode> sources = List.of(node(freeOption(1, ManaType.BLUE)));
        assertFalse(canPay(cost, sources));
    }

    @Test
    @DisplayName("TC-3: {2}{G}{G} payable with A(C→GG cost2), B(C), C(U), D(CCC cost1)")
    void tc3_complexCostWithChainedPaidSources() {
        // A: produces GG, costs 2 generic to activate
        ManaAbilityOption optA = paidOption(2, 2, ManaType.GREEN);
        // B: produces 1 colorless, free
        ManaAbilityOption optB = freeOption(1, ManaType.COLORLESS);
        // C: produces 1 blue, free
        ManaAbilityOption optC = freeOption(1, ManaType.BLUE);
        // D: produces 3 colorless, costs 1 generic to activate
        ManaAbilityOption optD = paidOption(3, 1, ManaType.COLORLESS);

        List<ManaSourceNode> sources = List.of(node(optA), node(optB), node(optC), node(optD));
        List<ManaCostSymbol> cost = List.of(
                ManaCostSymbol.generic(2),
                ManaCostSymbol.monoColor(ManaType.GREEN),
                ManaCostSymbol.monoColor(ManaType.GREEN)
        );
        assertTrue(canPay(cost, sources));
    }

    @Test
    @DisplayName("TC-4: {2}{G}{G} not payable with only B(C) and C(U)")
    void tc4_complexCostNotPayableWithoutGreenSource() {
        ManaAbilityOption optB = freeOption(1, ManaType.COLORLESS);
        ManaAbilityOption optC = freeOption(1, ManaType.BLUE);

        List<ManaSourceNode> sources = List.of(node(optB), node(optC));
        List<ManaCostSymbol> cost = List.of(
                ManaCostSymbol.generic(2),
                ManaCostSymbol.monoColor(ManaType.GREEN),
                ManaCostSymbol.monoColor(ManaType.GREEN)
        );
        assertFalse(canPay(cost, sources));
    }

    @Test
    @DisplayName("TC-5: {U/2} payable as {U} with 1×U source")
    void tc5_twobridPaidAsColor() {
        List<ManaCostSymbol> cost = List.of(ManaCostSymbol.twobrid(ManaType.BLUE, 2));
        List<ManaSourceNode> sources = List.of(node(freeOption(1, ManaType.BLUE)));
        assertTrue(canPay(cost, sources));
    }

    @Test
    @DisplayName("TC-6: {U/2} payable as {2} with 2×generic sources")
    void tc6_twobridPaidAsGeneric() {
        // Two colorless sources; colorless cannot pay generic in the model,
        // but two blue sources can pay the 2-generic alternative
        // Per the plan: colored sources pay generic. Use blue sources for generic payment.
        List<ManaCostSymbol> cost = List.of(ManaCostSymbol.twobrid(ManaType.BLUE, 2));
        List<ManaSourceNode> sources = List.of(
                node(freeOption(1, ManaType.BLUE)),
                node(freeOption(1, ManaType.BLUE))
        );
        assertTrue(canPay(cost, sources));
    }

    @Test
    @DisplayName("TC-7: {U/2} not payable with only 1×generic source")
    void tc7_twobridNotPayableWithOneGenericSource() {
        // Twobrid resolved as generic(2) needs 2 pips; only 1 available.
        // Resolved as color needs 1 blue; but source is a non-blue colored source.
        List<ManaCostSymbol> cost = List.of(ManaCostSymbol.twobrid(ManaType.BLUE, 2));
        // Use exactly 1 green source (not blue, so color path fails; and only 1 pip so generic path fails)
        List<ManaSourceNode> sources = List.of(node(freeOption(1, ManaType.GREEN)));
        assertFalse(canPay(cost, sources));
    }

    @Test
    @DisplayName("TC-8: {W/U} payable with 1×W/U source (capacity 1)")
    void tc8_hybridColorPaidByHybridSource() {
        // Source can produce W or U (modeled as a source that can produce either)
        ManaAbilityOption opt = ManaAbilityOption.of(UUID.randomUUID(), 1,
                EnumSet.of(ManaType.WHITE, ManaType.BLUE), false, 0);
        List<ManaCostSymbol> cost = List.of(ManaCostSymbol.hybridColor(ManaType.WHITE, ManaType.BLUE));
        List<ManaSourceNode> sources = List.of(node(opt));
        assertTrue(canPay(cost, sources));
    }

    @Test
    @DisplayName("TC-9: {W}{U} not payable with 1×W/U source (capacity 1 — only one pip)")
    void tc9_wuNotPayableWithSingleCapacityDualSource() {
        ManaAbilityOption opt = ManaAbilityOption.of(UUID.randomUUID(), 1,
                EnumSet.of(ManaType.WHITE, ManaType.BLUE), false, 0);
        List<ManaCostSymbol> cost = List.of(
                ManaCostSymbol.monoColor(ManaType.WHITE),
                ManaCostSymbol.monoColor(ManaType.BLUE)
        );
        List<ManaSourceNode> sources = List.of(node(opt));
        assertFalse(canPay(cost, sources));
    }

    @Test
    @DisplayName("TC-10: {W}{U} payable with 1×W source and 1×U source")
    void tc10_wuPayableWithSeparateSources() {
        List<ManaCostSymbol> cost = List.of(
                ManaCostSymbol.monoColor(ManaType.WHITE),
                ManaCostSymbol.monoColor(ManaType.BLUE)
        );
        List<ManaSourceNode> sources = List.of(
                node(freeOption(1, ManaType.WHITE)),
                node(freeOption(1, ManaType.BLUE))
        );
        assertTrue(canPay(cost, sources));
    }

    @Test
    @DisplayName("TC-11: {C} payable with 1×colorless source")
    void tc11_colorlessPaidByColorlessSource() {
        List<ManaCostSymbol> cost = List.of(ManaCostSymbol.colorless());
        List<ManaSourceNode> sources = List.of(node(freeOption(1, ManaType.COLORLESS)));
        assertTrue(canPay(cost, sources));
    }

    @Test
    @DisplayName("TC-12: {C} not payable with 1×green source")
    void tc12_colorlessNotPayableByGreenSource() {
        List<ManaCostSymbol> cost = List.of(ManaCostSymbol.colorless());
        List<ManaSourceNode> sources = List.of(node(freeOption(1, ManaType.GREEN)));
        assertFalse(canPay(cost, sources));
    }

    @Test
    @DisplayName("TC-13: {5} payable with 3 sources of capacity 2 (total=6)")
    void tc13_genericFivePayableWithThreeSourcesCapTwoEach() {
        List<ManaCostSymbol> cost = List.of(ManaCostSymbol.generic(5));
        List<ManaSourceNode> sources = List.of(
                node(freeOption(2, ManaType.GREEN)),
                node(freeOption(2, ManaType.GREEN)),
                node(freeOption(2, ManaType.GREEN))
        );
        assertTrue(canPay(cost, sources));
    }

    @Test
    @DisplayName("TC-14: {5} not payable with 2 sources of capacity 2 (total=4)")
    void tc14_genericFiveNotPayableWithTwoSourcesCapTwoEach() {
        List<ManaCostSymbol> cost = List.of(ManaCostSymbol.generic(5));
        List<ManaSourceNode> sources = List.of(
                node(freeOption(2, ManaType.GREEN)),
                node(freeOption(2, ManaType.GREEN))
        );
        assertFalse(canPay(cost, sources));
    }

    @Test
    @DisplayName("TC-15: {0} payable with no sources (zero cost always payable)")
    void tc15_zeroCostAlwaysPayable() {
        List<ManaCostSymbol> cost = List.of(ManaCostSymbol.generic(0));
        List<ManaSourceNode> sources = Collections.emptyList();
        assertTrue(canPay(cost, sources));
    }

    @Test
    @DisplayName("TC-16: colored activation cost {1}{G} — payable when forest + plains fund it")
    void tc16_coloredActivationCostPayable() {
        // Ability: {T}{1}{G}: Add {U}{U} — costs 1 generic + 1 green to activate, produces 2 blue
        List<ManaCostSymbol> activationCost = List.of(
                ManaCostSymbol.generic(1),
                ManaCostSymbol.monoColor(ManaType.GREEN)
        );
        ManaAbilityOption paidBlue = paidOptionColored(2, activationCost, ManaType.BLUE);

        // Free pool: one forest (G), one plains (W)
        ManaAbilityOption forest = freeOption(1, ManaType.GREEN);
        ManaAbilityOption plains  = freeOption(1, ManaType.WHITE);

        List<ManaSourceNode> sources = List.of(node(forest), node(plains), node(paidBlue));
        // Spell cost: {U}{U} — satisfiable by activating paidBlue (W pays {1}, G pays {G})
        List<ManaCostSymbol> spellCost = List.of(
                ManaCostSymbol.monoColor(ManaType.BLUE),
                ManaCostSymbol.monoColor(ManaType.BLUE)
        );
        assertTrue(canPay(spellCost, sources));
    }

    @Test
    @DisplayName("TC-17: colored activation cost {1}{G} — not payable without a green source")
    void tc17_coloredActivationCostNotPayableWithoutGreen() {
        // Same ability as TC-16 but free pool has no green mana
        List<ManaCostSymbol> activationCost = List.of(
                ManaCostSymbol.generic(1),
                ManaCostSymbol.monoColor(ManaType.GREEN)
        );
        ManaAbilityOption paidBlue = paidOptionColored(2, activationCost, ManaType.BLUE);

        // Only a plains — can pay the {1} generic but not the {G} activation pip
        ManaAbilityOption plains = freeOption(1, ManaType.WHITE);

        List<ManaSourceNode> sources = List.of(node(plains), node(paidBlue));
        List<ManaCostSymbol> spellCost = List.of(
                ManaCostSymbol.monoColor(ManaType.BLUE),
                ManaCostSymbol.monoColor(ManaType.BLUE)
        );
        assertFalse(canPay(spellCost, sources));
    }

    @Test
    @DisplayName("ManaCostSymbol: twobrid minCost=1, maxCost=2")
    void manaCostSymbol_twobridMinMax() {
        ManaCostSymbol s = ManaCostSymbol.twobrid(ManaType.BLUE, 2);
        assertEquals(1, s.minCost());
        assertEquals(2, s.maxCost());
    }

    @Test
    @DisplayName("ManaCostSymbol: generic(3) minCost=3")
    void manaCostSymbol_genericMinCost() {
        ManaCostSymbol s = ManaCostSymbol.generic(3);
        assertEquals(3, s.minCost());
    }

    @Test
    @DisplayName("ManaAbilityOption: canProduce(GENERIC) true for green source")
    void manaAbilityOption_canProduceGenericFromGreen() {
        ManaAbilityOption opt = freeOption(1, ManaType.GREEN);
        assertTrue(opt.canProduce(ManaType.GENERIC));
    }

    @Test
    @DisplayName("ManaAbilityOption: canProduce(GENERIC) true for colorless source (generic = any mana in MTG)")
    void manaAbilityOption_canProduceGenericFromColorless() {
        ManaAbilityOption opt = freeOption(1, ManaType.COLORLESS);
        assertTrue(opt.canProduce(ManaType.GENERIC));
    }

    @Test
    @DisplayName("ManaAbilityOption: hasCost() false when activationCost is empty")
    void manaAbilityOption_hasCostFalseWhenFree() {
        ManaAbilityOption free = freeOption(1, ManaType.GREEN);
        assertFalse(free.hasCost());
    }

    @Test
    @DisplayName("ManaAbilityOption: hasCost() true for colored activation cost {1}{G}")
    void manaAbilityOption_hasCostTrueForColoredActivation() {
        List<ManaCostSymbol> cost = List.of(
                ManaCostSymbol.generic(1),
                ManaCostSymbol.monoColor(ManaType.GREEN)
        );
        ManaAbilityOption opt = paidOptionColored(2, cost, ManaType.BLUE);
        assertTrue(opt.hasCost());
        assertEquals(2, opt.getActivationCost().size());
    }

    @Test
    @DisplayName("ManaAbilityOption.manaToSymbols: {1}{G} produces [generic(1), monoColor(GREEN)]")
    void manaAbilityOption_manaToSymbolsMixed() {
        Mana mana = new Mana(0, 0, 0, 0, 1, 1, 0, 0); // 1 green, 1 generic
        List<ManaCostSymbol> symbols = ManaAbilityOption.manaToSymbols(mana);
        assertEquals(2, symbols.size());
        assertTrue(symbols.stream().anyMatch(s -> s.getType() == ManaCostSymbol.SymbolType.GENERIC
                && s.getGenericCost() == 1));
        assertTrue(symbols.stream().anyMatch(s -> s.getType() == ManaCostSymbol.SymbolType.MONO_COLOR
                && s.getColorOptions().contains(ManaType.GREEN)));
    }

    @Test
    @DisplayName("ManaAbilityOption.manaToSymbols: {R}{R} produces two MONO_COLOR RED symbols")
    void manaAbilityOption_manaToSymbolsDoubleRed() {
        Mana mana = new Mana(0, 0, 0, 2, 0, 0, 0, 0); // 2 red
        List<ManaCostSymbol> symbols = ManaAbilityOption.manaToSymbols(mana);
        assertEquals(2, symbols.size());
        assertTrue(symbols.stream().allMatch(s -> s.getType() == ManaCostSymbol.SymbolType.MONO_COLOR
                && s.getColorOptions().contains(ManaType.RED)));
    }

    @Test
    @DisplayName("ManaSourceNode: mixed node detection")
    void manaSourceNode_mixedDetection() {
        ManaAbilityOption free = freeOption(1, ManaType.COLORLESS);
        ManaAbilityOption paid = paidOption(2, 2, ManaType.GREEN);
        ManaSourceNode mixedNode = ManaSourceNode.ofOptions(List.of(free, paid));

        assertTrue(mixedNode.isMixed());
        assertFalse(mixedNode.isAllFree());
        assertFalse(mixedNode.isAllPaid());
    }

    @Test
    @DisplayName("ManaSourceNode: plain Forest is allFree, singleOption")
    void manaSourceNode_forestIsAllFree() {
        ManaAbilityOption free = freeOption(1, ManaType.GREEN);
        ManaSourceNode forest = ManaSourceNode.ofOptions(List.of(free));

        assertTrue(forest.isAllFree());
        assertFalse(forest.isMixed());
        assertTrue(forest.hasSingleOption());
    }

    @Test
    @DisplayName("ManaSourceNode: getSingleOption() throws on multi-option node")
    void manaSourceNode_getSingleOptionThrows() {
        ManaAbilityOption a = freeOption(1, ManaType.WHITE);
        ManaAbilityOption b = freeOption(1, ManaType.BLUE);
        ManaSourceNode multi = ManaSourceNode.ofOptions(List.of(a, b));

        assertThrows(IllegalStateException.class,
                multi::getSingleOption);
    }

    @Test
    @DisplayName("Test ability source with multiple options {T},{G}: Add three mana of any one color")
    void abilitySourceMultipleOptions() {
        ManaAbilityOption opt1 = paidOptionColored(3, List.of(ManaCostSymbol.monoColor(ManaType.GREEN)), ManaType.WHITE, ManaType.BLUE, ManaType.BLACK, ManaType.RED, ManaType.GREEN);
        ManaAbilityOption opt2 = freeOption(1, ManaType.GREEN);
        List<ManaSourceNode> sources = List.of(node(opt1), node(opt2));
        List<ManaCostSymbol> cost = List.of(
                ManaCostSymbol.monoColor(ManaType.GREEN),
                ManaCostSymbol.monoColor(ManaType.GREEN),
                ManaCostSymbol.monoColor(ManaType.GREEN)
        );
        assertTrue(canPay(cost, sources));
        List<ManaCostSymbol> cost2 = List.of(
                ManaCostSymbol.monoColor(ManaType.WHITE),
                ManaCostSymbol.monoColor(ManaType.WHITE),
                ManaCostSymbol.monoColor(ManaType.WHITE)
        );
        assertTrue(canPay(cost2, sources));
        List<ManaCostSymbol> cost3 = List.of(
                ManaCostSymbol.monoColor(ManaType.WHITE),
                ManaCostSymbol.monoColor(ManaType.WHITE),
                ManaCostSymbol.monoColor(ManaType.WHITE),
                ManaCostSymbol.monoColor(ManaType.WHITE)
        );
        assertFalse(canPay(cost3, sources));
    }

    // -------------------------------------------------------------------------
    // UU-or-WW alternative-choice tests
    // -------------------------------------------------------------------------

    /**
     * A single node holding two mutually-exclusive alternatives: produce UU or produce WW.
     * Models e.g. Astral Cornucopia with 2 charge counters.
     */
    private static ManaSourceNode uuOrWwChoiceNode() {
        ManaAbilityOption optUU = freeOption(2, ManaType.BLUE);
        ManaAbilityOption optWW = freeOption(2, ManaType.WHITE);
        return ManaSourceNode.ofOptions(List.of(optUU, optWW));
    }

    @Test
    @DisplayName("UU/WW choice: {U}{U} is payable (pick UU alternative)")
    void uuWw_canPayUU() {
        List<ManaCostSymbol> cost = List.of(
                ManaCostSymbol.monoColor(ManaType.BLUE),
                ManaCostSymbol.monoColor(ManaType.BLUE));
        assertTrue(canPay(cost, List.of(uuOrWwChoiceNode())));
    }

    @Test
    @DisplayName("UU/WW choice: {W}{W} is payable (pick WW alternative)")
    void uuWw_canPayWW() {
        List<ManaCostSymbol> cost = List.of(
                ManaCostSymbol.monoColor(ManaType.WHITE),
                ManaCostSymbol.monoColor(ManaType.WHITE));
        assertTrue(canPay(cost, List.of(uuOrWwChoiceNode())));
    }

    @Test
    @DisplayName("UU/WW choice: {U}{W} is NOT payable (would require splitting across both alternatives)")
    void uuWw_cannotPayUW() {
        List<ManaCostSymbol> cost = List.of(
                ManaCostSymbol.monoColor(ManaType.BLUE),
                ManaCostSymbol.monoColor(ManaType.WHITE));
        assertFalse(canPay(cost, List.of(uuOrWwChoiceNode())));
    }

    @Test
    @DisplayName("UU/WW choice: {U} is payable from a single UU-or-WW source")
    void uuWw_canPaySingleU() {
        List<ManaCostSymbol> cost = List.of(ManaCostSymbol.monoColor(ManaType.BLUE));
        assertTrue(canPay(cost, List.of(uuOrWwChoiceNode())));
    }

    @Test
    @DisplayName("UU/WW choice: {W}{W}{U} needs a second source alongside UU-or-WW")
    void uuWw_wwuRequiresExtraSource() {
        // One UU/WW node + one extra W source → {W}{W}{U} not payable ({W}{W} ok, but no U)
        List<ManaCostSymbol> cost = List.of(
                ManaCostSymbol.monoColor(ManaType.WHITE),
                ManaCostSymbol.monoColor(ManaType.WHITE),
                ManaCostSymbol.monoColor(ManaType.BLUE));
        assertFalse(canPay(cost, List.of(uuOrWwChoiceNode(), node(freeOption(1, ManaType.WHITE)))));
    }

    @Test
    @DisplayName("UU/WW choice: two UU-or-WW nodes can pay {W}{W}{U}{U}")
    void uuWw_twoNodes_payWWUU() {
        List<ManaCostSymbol> cost = List.of(
                ManaCostSymbol.monoColor(ManaType.WHITE),
                ManaCostSymbol.monoColor(ManaType.WHITE),
                ManaCostSymbol.monoColor(ManaType.BLUE),
                ManaCostSymbol.monoColor(ManaType.BLUE));
        assertTrue(canPay(cost, List.of(uuOrWwChoiceNode(), uuOrWwChoiceNode())));
    }


    @Test
    @DisplayName("toManaList: single Forest → [{G}]")
    void toManaList_singleForest() {
        ManaOptions opts = new ManaOptions();
        opts.add(ManaSourceNode.ofOptions(List.of(freeOption(1, ManaType.GREEN))));
        List<Mana> result = opts.toManaList();
        assertEquals(1, result.size());
        assertEquals("{G}", result.getFirst().toString());
    }

    @Test
    @DisplayName("toManaList: UU-or-WW single node → [{U}{U}, {W}{W}]")
    void toManaList_uuOrWwNode() {
        ManaOptions opts = new ManaOptions();
        opts.add(uuOrWwChoiceNode());
        List<Mana> result = opts.toManaList();
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(m -> m.toString().equals("{U}{U}")));
        assertTrue(result.stream().anyMatch(m -> m.toString().equals("{W}{W}")));
    }

    @Test
    @DisplayName("toManaList: Forest + UU-or-WW → [{W}{W}{G}, {U}{U}{G}] (WUBRG canonical order)")
    void toManaList_forestPlusUuWw() {
        ManaOptions opts = new ManaOptions();
        opts.add(ManaSourceNode.ofOptions(List.of(freeOption(1, ManaType.GREEN))));
        opts.add(uuOrWwChoiceNode());
        List<Mana> result = opts.toManaList();
        assertEquals(2, result.size());
        // Mana.toString() outputs in WUBRG order: white comes before green
        assertTrue(result.stream().anyMatch(m -> m.toString().equals("{W}{W}{G}")));
        assertTrue(result.stream().anyMatch(m -> m.toString().equals("{U}{U}{G}")));
    }

    /**
     * Verifies that a synthetic option (created via {@code of()}, all-flexible map) with
     * multiple types distributes pips — e.g. capacity=2, types={W,R} → {W}{W}, {W}{R}, {R}{R}.
     * This is the flexible path for test-constructed options.
     * The real TinderFarm case (fromAbility with static map) is covered by the integration
     * test {@code ManaOptionsTest.testTinderFarm}.
     */
    @Test
    @DisplayName("toManaList: synthetic flexible multi-type option distributes pips")
    void toManaList_syntheticMultiTypeDistributes() {
        // of() builds a flexible map (all values=0) → distributeAmong path
        ManaAbilityOption synthetic = ManaAbilityOption.of(
                UUID.randomUUID(), 2,
                java.util.EnumSet.of(ManaType.WHITE, ManaType.RED), false, 0);
        ManaOptions opts = new ManaOptions();
        opts.add(ManaSourceNode.ofOptions(List.of(synthetic)));
        List<Mana> result = opts.toManaList();
        // cap=2, 2 types → distributions: (2,0),(1,1),(0,2) → {W}{W}, {W}{R}, {R}{R}
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(m -> m.toString().equals("{W}{W}")));
        assertTrue(result.stream().anyMatch(m -> m.toString().equals("{W}{R}")));
        assertTrue(result.stream().anyMatch(m -> m.toString().equals("{R}{R}")));
    }
}


