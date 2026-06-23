package org.mage.test.utils;

import mage.abilities.mana.ManaAbilityOption;
import mage.abilities.mana.ManaOptions;
import mage.abilities.mana.ManaSourceNode;
import mage.constants.ManaType;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.EnumSet;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;
import static org.mage.test.utils.ManaOptionsTestUtils.assertCanPay;
import static org.mage.test.utils.ManaOptionsTestUtils.assertCannotPay;

/**
 * This test checks if the calculated possible mana options are correct related
 * to the given mana sources available.
 *
 * @author LevelX2, JayDi85
 */
public class ManaOptionsTest extends CardTestPlayerBase {

    @Test
    public void testSimpleMana() {
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 3);

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getManaAvailable(currentGame);

        Assert.assertEquals("mana variations don't fit", 3, manaOptions.size());
        Assert.assertEquals("mana sum doesn't match", 3, manaOptions.stream()
                .mapToInt(ManaSourceNode::getCapacity)
                .sum());

        for (ManaSourceNode node : manaOptions) {
            Assert.assertEquals("mana type doesn't fit", EnumSet.of(ManaType.GREEN), node.getAbilityOptions().stream()
                    .map(ManaAbilityOption::getProducibleTypes)
                    .flatMap(EnumSet::stream)
                    .collect(Collectors.toSet()));
        }
    }

    // Tinder Farm enters the battlefield tapped.
    // {T}: Add {G}.
    // {T}, Sacrifice Tinder Farm: Add {R}{W}.
    @Test
    public void testTinderFarm() {
        addCard(Zone.BATTLEFIELD, playerA, "Tinder Farm", 3);

        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getManaAvailable(currentGame);

        Assert.assertEquals("mana variations don't fit", 3, manaOptions.size());
        Assert.assertEquals("mana sum doesn't match", 6, manaOptions.stream()
                .mapToInt(ManaSourceNode::getCapacity)
                .sum());
        assertCanPay("{G}{G}{G}", manaOptions, currentGame);
        assertCannotPay("{G}".repeat(4), manaOptions, currentGame);
        assertCanPay("{G}".repeat(3) + "{U/P}", manaOptions, currentGame);
        assertCanPay("{R}{G}{G}{W}", manaOptions, currentGame);
        assertCanPay("{R}{R}{G}{W}{W}", manaOptions, currentGame);
        assertCanPay("{R}{R}{R}{W}{W}{W}", manaOptions, currentGame);

    }

    // Adarkar Wastes
    // {T}: Add {C}.
    // {T}: Add {W} or {U}. Adarkar Wastes deals 1 damage to you.
    @Test
    public void testAdarkarWastes() {
        addCard(Zone.BATTLEFIELD, playerA, "Adarkar Wastes", 3);

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getManaAvailable(currentGame);

        Assert.assertEquals("mana variations don't fit", 3, manaOptions.size());
        assertCanPay("{C}{C}{C}", manaOptions, currentGame);
        assertCanPay("{C}{C}{W}", manaOptions, currentGame);
        assertCanPay("{C}{C}{U}", manaOptions, currentGame);
        assertCanPay("{C}{W}{W}", manaOptions, currentGame);
        assertCanPay("{C}{W}{U}", manaOptions, currentGame);
        assertCanPay("{C}{U}{U}", manaOptions, currentGame);
        assertCanPay("{W}{W}{W}", manaOptions, currentGame);
        assertCanPay("{W}{W}{U}", manaOptions, currentGame);
        assertCanPay("{W}{U}{U}", manaOptions, currentGame);
        assertCanPay("{U}{U}{U}", manaOptions, currentGame);
    }

    // Chromatic Sphere
    // {1}, {T}, Sacrifice Chromatic Sphere: Add one mana of any color. Draw a card.
    @Test
    public void testChromaticSphere() {
        addCard(Zone.BATTLEFIELD, playerA, "Forest");
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Chromatic Sphere", 2);

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getManaAvailable(currentGame);

        Assert.assertEquals("mana variations don't fit", 5, manaOptions.size());
        assertCannotPay("{B}".repeat(3), manaOptions, currentGame);
        assertCanPay("{G}{R}{R}", manaOptions, currentGame);
        assertCanPay("{G}{B}", manaOptions, currentGame);
        assertCanPay("{W}{W}", manaOptions, currentGame);
        assertCanPay("{U}{U}", manaOptions, currentGame);
        assertCanPay("{U}{W}", manaOptions, currentGame);
        assertCanPay("{2}", manaOptions, currentGame);
    }

    // Orochi Leafcaller
    // {G}: Add one mana of any color.
    @Test
    public void testOrochiLeafcaller() {
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Orochi Leafcaller");

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getManaAvailable(currentGame);

        Assert.assertEquals("mana variations don't fit", 5, manaOptions.size());
        assertCanPay("{4}", manaOptions, currentGame);
        assertCanPay("{G}{G}{W}{W}", manaOptions, currentGame);
        assertCanPay("{G}{U}{W}{W}", manaOptions, currentGame);
        assertCanPay("{U}{U}{W}{W}", manaOptions, currentGame);
        assertCanPay("{W/G}{W/U}", manaOptions, currentGame);
        assertCanPay("{W/U}{W/U}{2/W}", manaOptions, currentGame);
    }

    // Crystal Quarry
    // {T}: {1} Add .
    // {5}, {T}: Add {W}{U}{B}{R}{G}.
    @Test
    public void testCrystalQuarry() {
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Crystal Quarry", 1);

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getManaAvailable(currentGame);

        Assert.assertEquals("mana variations don't fit", 5, manaOptions.size());
        assertCanPay("{5}", manaOptions, currentGame);
        assertCannotPay("{W}{U}{B}{R}{G}", manaOptions, currentGame);
    }

    // Crystal Quarry
    // {T}: {1} Add .
    // {5}, {T}: Add {W}{U}{B}{R}{G}.
    @Test
    public void testCrystalQuarry2() {
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 3);
        addCard(Zone.BATTLEFIELD, playerA, "Crystal Quarry", 1);

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getManaAvailable(currentGame);

        Assert.assertEquals("mana variations don't fit", 6, manaOptions.size());
        assertCanPay("{C}{G}{G}{G}{W}{W}", manaOptions, currentGame);
        assertCanPay("{W}{U}{B}{R}{G}", manaOptions, currentGame);
        assertCannotPay("{C}{W}{U}{B}{R}{G}", manaOptions, currentGame);
    }

    // Nykthos, Shrine to Nyx
    // {T}: Add {C}.
    // {2}, {T}: Choose a color. Add an amount of mana of that color equal to your devotion to that color. (Your devotion to a color is the number of mana symbols of that color in the mana costs of permanents you control.)
    @Test
    public void testNykthos1() {
        addCard(Zone.BATTLEFIELD, playerA, "Sedge Scorpion", 4); // Creature {G} (1/1)
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 3);
        addCard(Zone.BATTLEFIELD, playerA, "Silvercoat Lion", 4); // Creature {1}{W}
        addCard(Zone.BATTLEFIELD, playerA, "Nykthos, Shrine to Nyx", 1); // Land

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getManaAvailable(currentGame);

        Assert.assertEquals("mana variations don't fit", 4, manaOptions.size());
        assertCanPay("{C}{G}{G}{G}", manaOptions, currentGame);
        assertCanPay("{G}{G}{G}{G}{G}", manaOptions, currentGame);
        assertCanPay("{G}{W}{W}{W}{W}", manaOptions, currentGame);
        assertCannotPay("{G}{G}{W}", manaOptions, currentGame);
    }

    @Test
    public void testNykthos2() {
        addCard(Zone.BATTLEFIELD, playerA, "Sedge Scorpion", 4);
        addCard(Zone.BATTLEFIELD, playerA, "Akroan Crusader", 3);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 3); // {G}
        addCard(Zone.BATTLEFIELD, playerA, "Nykthos, Shrine to Nyx", 1); // {C}

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getManaAvailable(currentGame);

        Assert.assertEquals("mana variations don't fit", 4, manaOptions.size());
        assertCanPay("{C}{G}{G}{G}", manaOptions, currentGame);
        assertCanPay("{G}{G}{G}{G}{G}", manaOptions, currentGame);
        assertCanPay("{R}{R}{R}{G}", manaOptions, currentGame);
        assertCannotPay("{R}".repeat(4), manaOptions, currentGame);
        assertCannotPay("{G}{G}" + "{R}".repeat(3), manaOptions, currentGame);
    }

    @Test
    public void testNykthos3() {
        addCard(Zone.BATTLEFIELD, playerA, "Sylvan Caryatid", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Nykthos, Shrine to Nyx", 1);

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getManaAvailable(currentGame);

        Assert.assertEquals("mana variations don't fit", 3, manaOptions.size());
        assertTrue("Expected to be able to produce {C}{G}{Any}", manaOptions.canProduce("{C}{G}{Any}"));
        assertCanPay("{C}{G}{G}", manaOptions, currentGame);
        assertCanPay("{C}{G}{W}", manaOptions, currentGame);
    }

    // Nykthos, Shrine to Nyx
    @Test
    public void testNykthos4a() {
        addCard(Zone.BATTLEFIELD, playerA, "Sedge Scorpion", 4);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 3);
        // {T}: Add {C}.
        // {2}, {T}: Choose a color. Add an amount of mana of that color equal to your devotion to that color. (Your devotion to a color is the number of mana symbols of that color in the mana costs of permanents you control.)
        addCard(Zone.BATTLEFIELD, playerA, "Nykthos, Shrine to Nyx", 1);

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getManaAvailable(currentGame);

        Assert.assertEquals("mana variations don't fit", 4, manaOptions.size());
        assertCanPay("{C}{G}{G}{G}", manaOptions, currentGame);
        assertCanPay("{G}{G}{G}{G}{G}", manaOptions, currentGame);

    }

    // Nykthos, Shrine to Nyx
    // {T}: Add {C}.
    // {2}, {T}: Choose a color. Add an amount of mana of that color equal to your devotion to that color. (Your devotion to a color is the number of mana symbols of that color in the mana costs of permanents you control.)
    @Test
    public void testNykthos4b() {
        // If a land is tapped for two or more mana, it produces {C} instead of any other type and amount.
        // Each spell a player casts costs {1} more to cast for each other spell that player has cast this turn.
        addCard(Zone.BATTLEFIELD, playerA, "Damping Sphere", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Sedge Scorpion", 4);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 3);
        addCard(Zone.BATTLEFIELD, playerA, "Nykthos, Shrine to Nyx", 1);

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getManaAvailable(currentGame);

        Assert.assertEquals("mana variations don't fit", 4, manaOptions.size());
        assertCanPay("{C}{G}{G}{G}", manaOptions, currentGame);
        assertCannotPay("{G}".repeat(5), manaOptions, currentGame);

    }

    @Test
    public void testNykthos5() {
        addCard(Zone.BATTLEFIELD, playerA, "Silvercoat Lion", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Sedge Scorpion", 4);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 3);
        addCard(Zone.BATTLEFIELD, playerA, "Nykthos, Shrine to Nyx", 1);

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getManaAvailable(currentGame);

        Assert.assertEquals("mana variations don't fit", 4, manaOptions.size());
        assertCanPay("{G}{W}{W}", manaOptions, currentGame);
        assertCannotPay("{G}{G}{W}{W}", manaOptions, currentGame);
        assertCanPay("{C}{G}{G}{G}", manaOptions, currentGame);
        assertCanPay("{G}{G}{G}{G}{G}", manaOptions, currentGame);
    }

    @Test
    public void testNykthos6() {
        addCard(Zone.BATTLEFIELD, playerA, "Sedge Scorpion", 4); // Creature {G} (1/1)
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 3);
        addCard(Zone.BATTLEFIELD, playerA, "Silvercoat Lion", 4); // Creature {1}{W}
        addCard(Zone.BATTLEFIELD, playerA, "Nykthos, Shrine to Nyx", 1); // Land

        addCard(Zone.BATTLEFIELD, playerA, "Radha, Heart of Keld");
        addCard(Zone.BATTLEFIELD, playerA, "Precognition Field");
        addCard(Zone.BATTLEFIELD, playerA, "Mystic Forge");
        addCard(Zone.BATTLEFIELD, playerA, "Experimental Frenzy");
        addCard(Zone.BATTLEFIELD, playerA, "Elsha of the Infinite");
        addCard(Zone.BATTLEFIELD, playerA, "Bolas's Citadel");
        addCard(Zone.BATTLEFIELD, playerA, "Verge Rangers");
        addCard(Zone.BATTLEFIELD, playerA, "Vivien, Monsters' Advocate");
        addCard(Zone.BATTLEFIELD, playerA, "Vizier of the Menagerie");

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getManaAvailable(currentGame);

        Assert.assertEquals("mana variations don't fit", 4, manaOptions.size());
        assertCanPay("{C}{G}{G}{G}", manaOptions, currentGame);
        assertCanPay("{G}".repeat(9), manaOptions, currentGame);
        assertCannotPay("{G}".repeat(10), manaOptions, currentGame);
        assertCanPay("{G}{W}{W}{W}{W}{W}{W}", manaOptions, currentGame);
        assertCanPay("{R}{R}{R}{G}", manaOptions, currentGame);
        assertCanPay("{B}{B}{B}{G}", manaOptions, currentGame);
        assertCanPay("{G}{U}{U}", manaOptions, currentGame);
    }

    @Test
    public void testDuplicatedDontHave1() {
        addCard(Zone.BATTLEFIELD, playerA, "City of Brass", 2); // Any
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getManaAvailable(currentGame);
        Assert.assertEquals("mana variations don't fit", 4, manaOptions.size());

        assertCanPay("{G}".repeat(2) + "{W}".repeat(2), manaOptions, currentGame);
        assertCanPay("{G}".repeat(2) + "{U}".repeat(2), manaOptions, currentGame);
        assertCanPay("{G}".repeat(2) + "{R}".repeat(2), manaOptions, currentGame);
        assertCanPay("{G}".repeat(2) + "{B}".repeat(2), manaOptions, currentGame);
        assertCanPay("{G}".repeat(4), manaOptions, currentGame);
        assertCannotPay("{W}".repeat(3), manaOptions, currentGame);
    }

    @Test
    public void testDuplicatedDontHave3() {
        addCard(Zone.BATTLEFIELD, playerA, "Grove of the Burnwillows", 2); // R or G
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getManaAvailable(currentGame);
        Assert.assertEquals("mana variations don't fit", 4, manaOptions.size());

        assertCanPay("{G}".repeat(4), manaOptions, currentGame);
        assertCanPay("{G}".repeat(2) + "{R}".repeat(2), manaOptions, currentGame);
        assertCannotPay("{R}".repeat(4), manaOptions, currentGame);
    }

    @Test
    public void testDuplicatedHave() {
        // getManaAvailable return any combination of mana variants available to player
        // if mana ability cost another mana then if replaced in mana cost
        // example:
        // 1x forest
        // 1x Chromatic Star ({1}, {T}, Sacrifice Chromatic Star: Add one mana of any color.)
        // give {G}{Any}, but after pay it transform to {Any} (1 green will be pay)
        // That's why there are can be duplicated records in getManaAvailable

        // {1}, {T}, Sacrifice Chromatic Star: Add one mana of any color.
        // When Chromatic Star is put into a graveyard from the battlefield, draw a card.
        addCard(Zone.BATTLEFIELD, playerA, "Chromatic Star", 1);
        // {1}, {T}, Sacrifice Chromatic Sphere: Add one mana of any color. Draw a card.
        addCard(Zone.BATTLEFIELD, playerA, "Chromatic Sphere", 1);
        // {T}: Add {C}. If you control an Urza's Mine and an Urza's Power-Plant, add {C}{C}{C} instead.
        addCard(Zone.BATTLEFIELD, playerA, "Urza's Tower", 1);
        // {T}: Add {C}.
        // {T}: Add {R} or {G}. Each opponent gains 1 life.
        addCard(Zone.BATTLEFIELD, playerA, "Grove of the Burnwillows", 1);

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getManaAvailable(currentGame);

        Assert.assertEquals("mana variations don't fit", 4, manaOptions.size());
        assertCanPay("{C}{C}", manaOptions, currentGame);
        assertCanPay("{G}{G}", manaOptions, currentGame);
        assertCannotPay("{C}{R}{R}", manaOptions, currentGame);
        assertCanPay("{R}{R}", manaOptions, currentGame);
    }

    @Test
    public void testFetidHeath() {
        // {T}: Add {C}.
        // {W/B}, {T}: Add {W}{W}, {W}{B}, or {B}{B}.        
        addCard(Zone.BATTLEFIELD, playerA, "Fetid Heath", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 1);

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);

        Assert.assertEquals("mana variations don't fit", 2, manaOptions.size());
        assertTrue("Expected to be able to produce {C}{W}", manaOptions.canProduce("{C}{W}"));
        assertTrue("Expected to be able to produce {W}{W}", manaOptions.canProduce("{W}{W}"));
        assertTrue("Expected to be able to produce {W}{B}", manaOptions.canProduce("{W}{B}"));
        assertTrue("Expected to be able to produce {B}{B}", manaOptions.canProduce("{B}{B}"));
    }

    /**
     * Don't use mana sources that only reduce available mana
     */
    @Test
    public void testCabalCoffers1() {
        addCard(Zone.BATTLEFIELD, playerA, "Cabal Coffers", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 1);

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);

        Assert.assertEquals("mana variations don't fit", 3, manaOptions.size());
        assertCanPay("{W}{B}", manaOptions, currentGame);
        assertCannotPay("{B}{B}", manaOptions, currentGame);
    }

    @Test
    public void testCabalCoffers2() {
        addCard(Zone.BATTLEFIELD, playerA, "Cabal Coffers", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 2);

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);

        Assert.assertEquals("mana variations don't fit", 4, manaOptions.size());
        assertCanPay("{W}{B}{B}", manaOptions, currentGame);
        assertCanPay("{B}{B}{B}", manaOptions, currentGame);
        assertCannotPay("{B}{B}{B}{B}", manaOptions, currentGame);
    }

    @Test
    public void testMageRingNetwork() {
        // {T}: Add {C}.
        // {T}, {1} : Put a storage counter on Mage-Ring Network.
        // {T}, Remove X storage counters from Mage-Ring Network: Add {X}.
        addCard(Zone.BATTLEFIELD, playerA, "Mage-Ring Network", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 1);

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);

        Assert.assertEquals("mana variations don't fit", 3, manaOptions.size());
        assertCanPay("{C}{W}{B}", manaOptions, currentGame);
    }

    @Test
    public void testMageRingNetwork2() {
        // {T}: Add {C}.
        // {T}, {1} : Put a storage counter on Mage-Ring Network.
        // {T}, Remove any number of storage counters from Mage-Ring Network: Add {C} for each storage counter removed this way.
        addCard(Zone.BATTLEFIELD, playerA, "Mage-Ring Network", 1);
        addCounters(1, PhaseStep.UPKEEP, playerA, "Mage-Ring Network", CounterType.STORAGE, 4);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 1);

        setStopAt(1, PhaseStep.DRAW);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);

        Assert.assertEquals("mana variations don't fit", 3, manaOptions.size());
        assertCanPay("{C}{C}{C}{C}{W}{B}", manaOptions, currentGame);
    }

    @Test
    public void testCryptGhast() {
        //Extort (Whenever you cast a spell, you may pay {WB}. If you do, each opponent loses 1 life and you gain that much life.)
        // Whenever you tap a Swamp for mana, add {B} (in addition to the mana the land produces).
        addCard(Zone.BATTLEFIELD, playerA, "Crypt Ghast", 1);

        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 1);

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);

        Assert.assertEquals("mana variations don't fit", 2, manaOptions.size());
        assertCanPay("{B}{B}", manaOptions, currentGame);
    }

    @Test
    public void testDampingSphere() {
        // If a land is tapped for two or more mana, it produces {C} instead of any other type and amount.
        // Each spell a player casts costs {1} more to cast for each other spell that player has cast this turn.
        addCard(Zone.BATTLEFIELD, playerA, "Damping Sphere", 1);
        // {T}: Add {C}.
        // {T}: Add {C}{C}. Spend this mana only to cast colorless Eldrazi spells or activate abilities of colorless Eldrazi.
        addCard(Zone.BATTLEFIELD, playerA, "Eldrazi Temple", 1);

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);

        Assert.assertEquals("mana variations don't fit", 1, manaOptions.size());
        assertTrue("Expected to be able to produce {C}", manaOptions.canProduce("{C}"));
        assertCannotPay("{C}{C}", manaOptions, currentGame);
    }

    @Test
    public void testCharmedPedant() {
        // {T}, Put the top card of your library into your graveyard: For each colored mana symbol in that card's mana cost, add one mana of that color.
        // Activate this ability only any time you could cast an instant.
        addCard(Zone.BATTLEFIELD, playerA, "Charmed Pendant", 1);
        // {T}: Add {C}.
        // {T}: Add {C}{C}. Spend this mana only to cast colorless Eldrazi spells or activate abilities of colorless Eldrazi.
        addCard(Zone.BATTLEFIELD, playerA, "Eldrazi Temple", 1);

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);

        Assert.assertEquals("mana variations don't fit", 1, manaOptions.size());
        assertTrue("Expected to be able to produce {C}{C}", manaOptions.canProduce("{C}{C}"));
        assertCannotPay("{C}{C}{C}", manaOptions, currentGame);
    }

    @Test
    public void testManaSourcesWithCosts() {
        // {T}: Add {C} to your mana pool.
        // {5}, {T}: Add {W}{U}{B}{R}{G} to your mana pool.
        addCard(Zone.BATTLEFIELD, playerA, "Crystal Quarry", 1);

        // {T}: Add {C} to your mana pool.
        // {W/B}, {T}: Add {W}{W}, {W}{B}, or {B}{B} to your mana pool.
        addCard(Zone.BATTLEFIELD, playerA, "Fetid Heath", 3);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 3);

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);

        Assert.assertEquals("mana variations don't fit", 7, manaOptions.size());
        assertCanPay("{C}{W}{U}{B}{R}{G}", manaOptions, currentGame);
        assertCanPay("{C}" + "{W}".repeat(6), manaOptions, currentGame);
        assertCanPay("{C}" + "{B}".repeat(6), manaOptions, currentGame);
    }

    @Test
    public void testSungrassPrairie() {
        // {1}, {T}: Add {G}{W}.
        addCard(Zone.BATTLEFIELD, playerA, "Sungrass Prairie", 1);
        // {T}: Add one mana of any color to your mana pool.
        addCard(Zone.BATTLEFIELD, playerA, "Alloy Myr", 2);

        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);

        Assert.assertEquals("mana variations don't fit", 3, manaOptions.size());

        assertTrue("Expected to be able to produce {G}{W}{Any}", manaOptions.canProduce("{G}{W}{Any}"));
        assertTrue("Expected to be able to produce {Any}{Any}", manaOptions.canProduce("{Any}{Any}"));
    }

    @Test
    public void testSungrassPrairie2() {
        // {1}, {T}: Add {G}{W}.
        addCard(Zone.BATTLEFIELD, playerA, "Sungrass Prairie", 5);
        // ({T}: Add {U} or {W} to your mana pool.)
        addCard(Zone.BATTLEFIELD, playerA, "Tundra", 9);
        // ({T}: Add {G} or {U} to your mana pool.)
        addCard(Zone.BATTLEFIELD, playerA, "Tropical Island", 3);

        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();
        long startTime = System.currentTimeMillis();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);

        Assert.assertEquals("mana variations don't fit", 17, manaOptions.size());

        assertCanPay("{G}".repeat(8) + "{W}".repeat(7) + "{U}", manaOptions, currentGame);
        assertCanPay("{G}".repeat(8) + "{W}".repeat(8), manaOptions, currentGame);
        logger.info("Sungrass Prairie test completed in " + (System.currentTimeMillis() - startTime) + " ms");
    }

    @Test
    public void testSungrassPrairie3() {
        // {1}, {T}: Add {G}{W}.
        addCard(Zone.BATTLEFIELD, playerA, "Sungrass Prairie", 1);
        // ({T}: Add {U} or {W} to your mana pool.)
        addCard(Zone.BATTLEFIELD, playerA, "Tundra", 1);
        // ({T}: Add {G} or {U} to your mana pool.)
        addCard(Zone.BATTLEFIELD, playerA, "Tropical Island", 1);

        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);
        long startTime = System.currentTimeMillis();

        Assert.assertEquals("mana variations don't fit", 3, manaOptions.size());
        assertCanPay("{U}{U}", manaOptions, currentGame);
        assertCanPay("{G}{W}{U}", manaOptions, currentGame);
        assertCanPay("{G}{W}{W}", manaOptions, currentGame);
        assertCanPay("{G}{G}{W}", manaOptions, currentGame);
        logger.info("Sungrass Prairie test completed in " + (System.currentTimeMillis() - startTime) + " ms");
    }

    /**
     * This is a stress test for the implementation of ManaUtils.
     * Based on the bug from: https://github.com/magefree/mage/issues/7710
     */
    @Test
    public void testCascadingCataracts() {
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 5);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 5);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 5);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 5);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 5);
        addCard(Zone.BATTLEFIELD, playerA, "Desert", 5);
        addCard(Zone.BATTLEFIELD, playerA, "Cascading Cataracts", 3);

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);
        long startTime = System.currentTimeMillis();
        Assert.assertEquals("mana variations don't match", 33, manaOptions.size());
        assertCanPay("{W}".repeat(20), manaOptions, currentGame);
        assertCannotPay("{W}".repeat(21), manaOptions, currentGame);
        assertCanPay("{U}".repeat(20), manaOptions, currentGame);
        assertCannotPay("{U}".repeat(21), manaOptions, currentGame);
        assertCanPay("{B}".repeat(20), manaOptions, currentGame);
        assertCannotPay("{B}".repeat(21), manaOptions, currentGame);
        assertCanPay("{R}".repeat(20), manaOptions, currentGame);
        assertCannotPay("{R}".repeat(21), manaOptions, currentGame);
        assertCanPay("{G}".repeat(20), manaOptions, currentGame);
        assertCannotPay("{G}".repeat(21), manaOptions, currentGame);
        logger.info("Cascading Cataracts test completed in " + (System.currentTimeMillis() - startTime) + " ms");
    }

    /**
     * Similar to above, except without a hardcoded expected result, and used to check scaling.
     * Leave the @Ignore added when pushing commits.
     */
    @Test
    @Ignore("Enable for performance testing")
    public void testCascadingCataractsN() {
        int n = 6;
        addCard(Zone.BATTLEFIELD, playerA, "Plains", n);
        addCard(Zone.BATTLEFIELD, playerA, "Island", n);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", n);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", n);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", n);
        addCard(Zone.BATTLEFIELD, playerA, "Desert", n);
        addCard(Zone.BATTLEFIELD, playerA, "Cascading Cataracts", n);

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);
        long startTime = System.currentTimeMillis();
        assertCanPay("{W}".repeat(n), manaOptions, currentGame);
        assertCanPay("{W}".repeat(n + (n * 5)), manaOptions, currentGame);
        assertCannotPay("{W}".repeat(n + (n * 5) + 1), manaOptions, currentGame);
        assertCanPay("{U}".repeat(n), manaOptions, currentGame);
        assertCanPay("{U}".repeat(n + (n * 5)), manaOptions, currentGame);
        assertCannotPay("{U}".repeat(n + (n * 5) + 1), manaOptions, currentGame);
        assertCanPay("{B}".repeat(n), manaOptions, currentGame);
        assertCanPay("{B}".repeat(n + (n * 5)), manaOptions, currentGame);
        assertCannotPay("{B}".repeat(n + (n * 5) + 1), manaOptions, currentGame);
        assertCanPay("{R}".repeat(n), manaOptions, currentGame);
        assertCanPay("{R}".repeat(n + (n * 5)), manaOptions, currentGame);
        assertCannotPay("{R}".repeat(n + (n * 5) + 1), manaOptions, currentGame);
        assertCanPay("{G}".repeat(n), manaOptions, currentGame);
        assertCanPay("{G}".repeat(n + (n * 5)), manaOptions, currentGame);
        assertCannotPay("{G}".repeat(n + (n * 5) + 1), manaOptions, currentGame);
        logger.info("Cascading Cataracts test completed in " + (System.currentTimeMillis() - startTime) + " ms");
    }

    @Test
    public void testDeathriteShaman() {
        /**
         * {@link mage.cards.d.DeathriteShaman Deathrite Shaman}
         * <br>
         * {B/G}
         * <br>
         * Creature -- Elf Shaman
         * <br>
         * {T}: Exile target land card from a graveyard. Add one mana of any color.
         {B}, {T}: Exile target instant or sorcery card from a graveyard. Each opponent loses 2 life.
         {G}, {T}: Exile target creature card from a graveyard. You gain 2 life.
         * <br>
         * 1/2
         */
        final String deathriteShaman = "Deathrite Shaman";

        addCard(Zone.BATTLEFIELD, playerA, deathriteShaman);
        addCard(Zone.GRAVEYARD, playerA, "Forest");

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);
        Assert.assertEquals("mana variations don't fit", 1, manaOptions.size());
        assertTrue("Expected to be able to produce {W}", manaOptions.canProduce("{W}"));
        assertTrue("Expected to be able to produce {U}", manaOptions.canProduce("{U}"));
        assertTrue("Expected to be able to produce {B}", manaOptions.canProduce("{B}"));
        assertTrue("Expected to be able to produce {R}", manaOptions.canProduce("{R}"));
        assertTrue("Expected to be able to produce {G}", manaOptions.canProduce("{G}"));
    }

    @Test
    public void testCrucibleOfTheSpiritDragon() {
        /**
         * {@link mage.cards.c.CrucibleOfTheSpiritDragon Crucible of the Spirit Dragon}
         * <br>
         *
         * <br>
         * Land
         * <br>
         * {T}: Add {C}.
         {1}, {T}: Put a storage counter on this land.
         {T}, Remove X storage counters from this land: Add X mana in any combination of colors. Spend this mana only to cast Dragon spells or activate abilities of Dragons.
         */
        final String crucibleOfTheSpiritDragon = "Crucible of the Spirit Dragon";

        addCard(Zone.BATTLEFIELD, playerA, crucibleOfTheSpiritDragon);
        addCounters(1, PhaseStep.UPKEEP, playerA, crucibleOfTheSpiritDragon, CounterType.STORAGE, 3);

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);
        Assert.assertEquals("mana variations don't fit", 1, manaOptions.size());
        assertTrue("Expected to be able to produce {C}", manaOptions.canProduce("{C}"));
        assertTrue("Expected to be able to produce {C}{C}{C}", manaOptions.canProduce("{C}{C}{C}"));
    }
}
