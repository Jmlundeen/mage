package org.mage.test.cards.abilities.mana;

import mage.abilities.mana.ManaOptions;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import static org.junit.Assert.assertEquals;
import static org.mage.test.utils.ManaOptionsTestUtils.assertCanPay;
import static org.mage.test.utils.ManaOptionsTestUtils.assertCannotPay;

public class ReplaceManaEffectTest extends CardTestPlayerBase {

    /**
     * {@link mage.cards.h.HarvestMage Harvest Mage}
     * <br>
     * {G}
     * <br>
     * Creature -- Human Spellshaper
     * <br>
     * {G}, {T}, Discard a card: Until end of turn, if you tap a land for mana, it produces one mana of a color of your choice instead of any other type and amount.
     * <br>
     * 1/1
     */
    private static final String harvestMage = "Harvest Mage";

    /**
     * {@link mage.cards.l.LightningBolt Lightning Bolt}
     * <br>
     * {R}
     * <br>
     * Instant
     * <br>
     * Lightning Bolt deals 3 damage to any target.
     */
    private static final String lightningBolt = "Lightning Bolt";

    /**
     * {@link mage.cards.n.NyxbloomAncient Nyxbloom Ancient}
     * <br>
     * {4}{G}{G}{G}
     * <br>
     * Enchantment Creature -- Elemental
     * <br>
     * Trample
     If you tap a permanent for mana, it produces three times as much of that mana instead.
     * <br>
     * 5/5
     */
    private static final String nyxbloomAncient = "Nyxbloom Ancient";


    /**
     * {@link mage.cards.g.GruulTurf Gruul Turf}
     * <br>
     *
     * <br>
     * Land
     * <br>
     * This land enters tapped.
     When this land enters, return a land you control to its owner's hand.
     {T}: Add {R}{G}.
     */
    private static final String gruulTurf = "Gruul Turf";

    /**
     * {@link mage.cards.c.Contamination Contamination}
     * <br>
     * {2}{B}
     * <br>
     * Enchantment
     * <br>
     * At the beginning of your upkeep, sacrifice this enchantment unless you sacrifice a creature.
     If a land is tapped for mana, it produces {B} instead of any other type and amount.
     */
    private static final String contamination = "Contamination";

    /**
     * {@link mage.cards.b.BalduvianBears Balduvian Bears}
     * <br>
     * {1}{G}
     * <br>
     * Creature -- Bear
     * <br>
     *
     * <br>
     * 2/2
     */
    private static final String balduvianBears = "Balduvian Bears";

    /**
     * {@link mage.cards.f.FalseDawn False Dawn}
     * <br>
     * {1}{W}
     * <br>
     * Sorcery
     * <br>
     * Until end of turn, spells and abilities you control that would add colored mana instead add that much white mana. Until end of turn, you may spend white mana as though it were mana of any color.
     Draw a card.
     */
    private static final String falseDawn = "False Dawn";

    /**
     * {@link mage.cards.h.HallOfGemstone Hall of Gemstone}
     * <br>
     * {1}{G}{G}
     * <br>
     * World Enchantment
     * <br>
     * At the beginning of each player's upkeep, that player chooses a color. Until end of turn, lands tapped for mana produce mana of the chosen color instead of any other color.
     */
    private static final String hallOfGemstone = "Hall of Gemstone";

    /**
     * {@link mage.cards.n.NakedSingularity Naked Singularity}
     * <br>
     * {5}
     * <br>
     * Artifact
     * <br>
     * Cumulative upkeep {3} (At the beginning of your upkeep, put an age counter on this permanent, then sacrifice it unless you pay its upkeep cost for each age counter on it.)
     If tapped for mana, Plains produce {R}, Islands produce {G}, Swamps produce {W}, Mountains produce {U}, and Forests produce {B} instead of any other type.
     */
    private static final String nakedSingularity = "Naked Singularity";

    /**
     * {@link mage.cards.u.UrborgTombOfYawgmoth Urborg, Tomb of Yawgmoth}
     * <br>
     *
     * <br>
     * Legendary Land
     * <br>
     * Each land is a Swamp in addition to its other land types.
     */
    private static final String urborgTombOfYawgmoth = "Urborg, Tomb of Yawgmoth";

    /**
     * {@link mage.cards.j.JetmirsGarden Jetmir's Garden}
     * <br>
     *
     * <br>
     * Land -- Mountain Forest Plains
     * <br>
     * ({T}: Add {R}, {G}, or {W}.)
     This land enters tapped.
     Cycling {3} ({3}, Discard this card: Draw a card.)
     */
    private static final String jetmirsGarden = "Jetmir's Garden";


    @Test
    public void testHarvestMage() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, harvestMage);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);
        addCard(Zone.HAND, playerA, "Swamp");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{G}, {T}, Discard a card");
        setChoice(playerA, "Swamp");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {G}");
        setChoice(playerA, "Red");
        checkManaPool("mana should have 1 red mana", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "R", 1);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }

    @Test
    public void testHarvestMagePlayable() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, harvestMage);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);
        addCard(Zone.HAND, playerA, "Swamp");
        addCard(Zone.HAND, playerA, lightningBolt);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{G}, {T}, Discard a card");
        setChoice(playerA, "Swamp");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);

        checkPlayableAbility("Should be able to cast lightning bolt", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast " + lightningBolt, true);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, lightningBolt, playerB);
        setChoice(playerA, "Red");

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertLife(playerB, 20 - 3);
    }

    @Test
    public void testHarvestMageOpponent() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, harvestMage);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);
        addCard(Zone.BATTLEFIELD, playerB, "Forest");
        addCard(Zone.HAND, playerA, "Swamp");

        setChoice(playerA, "Swamp");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{G}, {T}, Discard a card");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerB, "{T}: Add {G}");
        checkManaPool("Player B should have 1 green mana", 1, PhaseStep.PRECOMBAT_MAIN, playerB, "G", 1);
        checkManaPool("Player B should have 0 of other types", 1, PhaseStep.PRECOMBAT_MAIN, playerB, "WUBR", 0);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }

    @Test
    public void testNyxbloomAncient() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, nyxbloomAncient);
        addCard(Zone.BATTLEFIELD, playerA, gruulTurf);

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {R}{G}");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkManaPool("mana should have 3 red and 3 green mana", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "RG", 3);
        checkManaPool("mana should have 0 of other types", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "WUB", 0);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }

    @Test
    public void testContamination() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, contamination);
        addCard(Zone.BATTLEFIELD, playerA, balduvianBears);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);

        setChoice(playerA, true); // choose to sacrifice creature
        setChoice(playerA, balduvianBears);
        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {G}", 2);
        checkManaPool("mana should have 2 black mana", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "B", 2);
        checkManaPool("mana should have 0 of other types", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "WURG", 0);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }

    @Test
    public void testContaminationOpponent() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, contamination);
        addCard(Zone.BATTLEFIELD, playerA, balduvianBears);
        addCard(Zone.BATTLEFIELD, playerB, "Forest", 2);

        setChoice(playerA, true); // choose to sacrifice creature
        setChoice(playerA, balduvianBears);

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerB, "{T}: Add {G}", 2);
        checkManaPool("mana should have 2 black mana", 1, PhaseStep.PRECOMBAT_MAIN, playerB, "B", 2);
        checkManaPool("mana should have 0 of other types", 1, PhaseStep.PRECOMBAT_MAIN, playerB, "WURG", 0);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }

    @Test
    public void testContaminationAndMage() {
        setStrictChooseMode(true);
        addCard(Zone.HAND, playerA, contamination);
        addCard(Zone.BATTLEFIELD, playerA, harvestMage);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 3);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 3);
        addCard(Zone.HAND, playerA, "Swamp");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{G}, {T}, Discard a card");
        setChoice(playerA, "Swamp");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {B}", 3);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, contamination);
        setChoice(playerA, "Black", 3);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);

        // applying harvest mage first will result in black mana
        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {G}");
        setChoice(playerA, "Harvest Mage");
        setChoice(playerA, "Red");
        checkManaPool("mana should have 1 black mana", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "B", 1);
        checkManaPool("mana should have 0 of other types", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "WURG", 0);

        // applying contamination first will result in chosen mana
        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {G}");
        setChoice(playerA, "Contamination");
        setChoice(playerA, "Red");
        checkManaPool("mana should have 1 black mana and 1 red mana", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "BR", 1);
        checkManaPool("mana should have 0 of other types", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "WUG", 0);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }

    @Test
    public void testFalseDawn() {
        setStrictChooseMode(true);
        addCard(Zone.HAND, playerA, falseDawn);
        addCard(Zone.HAND, playerA, lightningBolt);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {W}", 2);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, falseDawn);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {U}", 2);
        checkManaPool("mana should have 2 white mana", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "W", 2);
        checkManaPool("mana should have 0 of other types", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "UBRG", 0);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, lightningBolt, playerB);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertLife(playerB, 20 - 3);
    }

    @Test
    public void testHallOfGemstone() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, hallOfGemstone);
        addCard(Zone.BATTLEFIELD, playerA, "Wastes", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);
        addCard(Zone.BATTLEFIELD, playerB, "Island", 2);

        setChoice(playerA, "Black");
        setChoice(playerB, "Red");

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {G}", 1);
        checkManaPool("mana should have 1 black mana - T1 - PlayerA", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "B", 1);
        checkManaPool("mana should have 0 of other types - T1 - PlayerA", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "WURG", 0);

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {C}", 1);
        checkManaPool("mana should have 1 black mana and 1 colorless mana - T1 - PlayerA", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "BC", 1);
        checkManaPool("mana should have 0 of other types - T1 - PlayerA", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "WURG", 0);

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerB, "{T}: Add {U}");
        checkManaPool("mana should have 1 black mana - T1 - PlayerB", 1, PhaseStep.PRECOMBAT_MAIN, playerB, "B", 1);
        checkManaPool("mana should have 0 of other types - T1 - PlayerB", 1, PhaseStep.PRECOMBAT_MAIN, playerB, "WURG", 0);

        activateManaAbility(2, PhaseStep.PRECOMBAT_MAIN, playerB, "{T}: Add {U}");
        checkManaPool("mana should have 1 red mana - T2 - PlayerB", 2, PhaseStep.PRECOMBAT_MAIN, playerB, "R", 1);
        checkManaPool("mana should have 0 of other types - T2 - PlayerB", 2, PhaseStep.PRECOMBAT_MAIN, playerB, "WUBG", 0);

        activateManaAbility(2, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {G}");
        checkManaPool("mana should have 1 red mana - T2 - PlayerA", 2, PhaseStep.PRECOMBAT_MAIN, playerA, "R", 1);
        checkManaPool("mana should have 0 of other types - T2 - PlayerA", 2, PhaseStep.PRECOMBAT_MAIN, playerA, "WUBG", 0);

        activateManaAbility(2, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {C}");
        checkManaPool("mana should have 1 red mana and 1 colorless mana - T2 - PlayerA", 2, PhaseStep.PRECOMBAT_MAIN, playerA, "RC", 1);
        checkManaPool("mana should have 0 of other types - T2 - PlayerA", 2, PhaseStep.PRECOMBAT_MAIN, playerA, "WUBG", 0);

        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }

    @Test
    public void testNakedSingularity() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, nakedSingularity);
        addCard(Zone.BATTLEFIELD, playerA, urborgTombOfYawgmoth);
        addCard(Zone.BATTLEFIELD, playerA, jetmirsGarden);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 3);

        activateManaAbility(1, PhaseStep.UPKEEP, playerA, "{T}: Add {U}", 3);
        setChoice(playerA, true); // pay upkeep
        setChoice(playerA, "Green", 3);

        runCode("check mana options for garden", 1, PhaseStep.PRECOMBAT_MAIN, playerA, ((info, player, game) -> {
            ManaOptions options = player.getManaAvailable(game);
            assertEquals(2, options.size());
            assertCanPay("{W}", options, game);
            assertCanPay("{U}", options, game);
            assertCanPay("{B}", options, game);
            assertCannotPay("{G}", options, game);
            assertCanPay("{R}", options, game);
        }));

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {W}");
        setChoice(playerA, "Black");
        checkManaPool("mana should have 1 black mana", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "B", 1);
        checkManaPool("mana should have 0 of other types", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "WURG", 0);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }
}
