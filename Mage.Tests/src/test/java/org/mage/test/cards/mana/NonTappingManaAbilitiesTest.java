package org.mage.test.cards.mana;

import mage.abilities.mana.ManaOptions;
import mage.constants.ManaType;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import static org.junit.Assert.assertTrue;
import static org.mage.test.utils.ManaOptionsTestUtils.assertCanPay;
import static org.mage.test.utils.ManaOptionsTestUtils.assertManaOptions;

/**
 *
 * @author LevelX2
 */
public class NonTappingManaAbilitiesTest extends CardTestPlayerBase {

    @Test
    public void druidsRepositoryTest() {
        setStrictChooseMode(true);

        addCard(Zone.HAND, playerA, "Alaborn Grenadier", 1); //Creature {W}{W}

        addCard(Zone.BATTLEFIELD, playerA, "Silvercoat Lion", 2);
        // Whenever a creature you control attacks, put a charge counter on Druids' Repository.
        // Remove a charge counter from Druids' Repository: Add one mana of any color.
        addCard(Zone.BATTLEFIELD, playerA, "Druids' Repository", 1); // Enchantment {1}{G}{G}

        attack(1, playerA, "Silvercoat Lion");
        attack(1, playerA, "Silvercoat Lion");
        setChoice(playerA, "Whenever a creature you control");

        setStopAt(1, PhaseStep.END_COMBAT);
        execute();

        assertTappedCount("Silvercoat Lion", true, 2);
        assertCounterCount(playerA, "Druids' Repository", CounterType.CHARGE, 2);

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);
        Assert.assertEquals("mana variations don't fit", 1, manaOptions.size());
        assertManaOptions("{Any}{Any}", manaOptions);

        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Alaborn Grenadier");
        setChoice(playerA, "White");
        setChoice(playerA, "White");

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertCounterCount(playerA, "Druids' Repository", CounterType.CHARGE, 0);
        assertPermanentCount(playerA, "Alaborn Grenadier", 1);
    }

    @Test
    public void TestWorkhorse() {
        setStrictChooseMode(true);

        // Workhorse enters the battlefield with four +1/+1 counters on it.
        // Remove a +1/+1 counter from Workhorse: Add {C}.
        addCard(Zone.BATTLEFIELD, playerA, "Workhorse", 1);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);
        Assert.assertEquals("mana variations don't fit", 1, manaOptions.size());
        assertManaOptions("{C}{C}{C}{C}", manaOptions);
    }

    @Test
    public void TestMorselhoarder() {
        setStrictChooseMode(true);
        // Morselhoarder enters the battlefield with two -1/-1 counters on it.
        // Remove a -1/-1 counter from Morselhoarder: Add one mana of any color.
        addCard(Zone.BATTLEFIELD, playerA, "Morselhoarder", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 2);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);
        Assert.assertEquals("mana variations don't fit", 4, manaOptions.size());
        assertTrue("Expected to be able to produce {B}{B}{Any}{Any}{Any}{Any}", manaOptions.canProduce("{B}{B}{Any}{Any}{Any}{Any}"));
    }

    @Test
    public void TestFarrelitePriest() {
        setStrictChooseMode(true);
        // {1}: Add {W}. If this ability has been activated four or more times this turn, sacrifice Farrelite Priest at the beginning of the next end step.
        addCard(Zone.BATTLEFIELD, playerA, "Farrelite Priest", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 4);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);
        Assert.assertEquals("mana variations don't fit", 5, manaOptions.size());
        assertTrue("Expected to be able to produce 4 Black", manaOptions.canProduce(ManaType.BLACK, 4));
        assertCanPay("{W}".repeat(4), manaOptions);
        assertCanPay("{W}{W}{B}{B}", manaOptions);
    }

    @Test
    public void TestCrystallineCrawler() {
        setStrictChooseMode(true);
        // Converge - Crystalline Crawler enters the battlefield with a +1/+1 counter on it for each color of mana spent to cast it.
        // Remove a +1/+1 counter from Crystalline Crawler: Add one mana of any color.
        // {T}: Put a +1/+1 counter on Crystalline Crawler.
        addCard(Zone.BATTLEFIELD, playerA, "Crystalline Crawler", 1);
        addCard(Zone.HAND, playerA, "Balduvian Bears");
        addCounters(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Crystalline Crawler", CounterType.P1P1, 2);

        checkPlayableAbility(String.format("Can cast %s with 2 counters", "Balduvian Bears"), 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast Balduvian Bears", true);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);
        Assert.assertEquals("mana variations don't fit", 1, manaOptions.size());
        assertManaOptions("{Any}{Any}", manaOptions);
    }

    @Test
    public void TestCoalGolemAndDromarsAttendant() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 1);

        // {1}, Sacrifice Dromar's Attendant: Add {W}{U}{B}.
        addCard(Zone.BATTLEFIELD, playerA, "Dromar's Attendant", 1);

        // {3}, Sacrifice Coal Golem: Add {R}{R}{R}.
        addCard(Zone.BATTLEFIELD, playerA, "Coal Golem", 1);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);
        Assert.assertEquals("mana variations don't fit", 3, manaOptions.size());
        assertTrue("Expected to be able to produce {G}", manaOptions.canProduce("{G}"));
        assertTrue("Expected to be able to produce {W}{U}{B}", manaOptions.canProduce("{W}{U}{B}"));
        assertTrue("Expected to be able to produce {R}{R}{R}", manaOptions.canProduce("{R}{R}{R}"));
    }

    /**
     * The order the mana abilities are checked during available mana
     * calculation does matter. Because Coal Golem can not be used as long as
     * Dromar's Attendant is not used yet to produce the 3 mana.
     */
    @Test
    public void TestCoalGolemAndDromarsAttendantOrder2() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 1);

        // {3}, Sacrifice Coal Golem: Add {R}{R}{R}.
        addCard(Zone.BATTLEFIELD, playerA, "Coal Golem", 1);

        // {1}, Sacrifice Dromar's Attendant: Add {W}{U}{B}.
        addCard(Zone.BATTLEFIELD, playerA, "Dromar's Attendant", 1);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);
        Assert.assertEquals("mana variations don't fit", 3, manaOptions.size());
        assertTrue("Expected to be able to produce {G}", manaOptions.canProduce("{G}"));
        assertTrue("Expected to be able to produce {W}{U}{B}", manaOptions.canProduce("{W}{U}{B}"));
        assertTrue("Expected to be able to produce {R}{R}{R}", manaOptions.canProduce("{R}{R}{R}"));
    }

    @Test
    public void TestJunglePatrol() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);
        // {1}{G}, {T}: Create a 0/1 green Wall creature token with defender named Wood.
        // Sacrifice a token named Wood: Add {R}.
        addCard(Zone.BATTLEFIELD, playerA, "Jungle Patrol", 1);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{1}{G}, {T}: Create");
        activateAbility(3, PhaseStep.PRECOMBAT_MAIN, playerA, "{1}{G}, {T}: Create");
        activateAbility(5, PhaseStep.PRECOMBAT_MAIN, playerA, "{1}{G}, {T}: Create");

        setStopAt(5, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, "Wood", 3);

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);
        Assert.assertEquals("mana variations don't fit", 1, manaOptions.size());
        assertManaOptions("{R}{R}{R}", manaOptions);
    }

    @Test
    public void TestSquanderedResources() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Taiga", 1);  // ({T}: Add {R} or {G}.)
        // {T}: Add {C}.
        // {1}, {T}: Put a storage counter on Calciform Pools.
        // {1}, Remove X storage counters from Calciform Pools: Add X mana in any combination of {W} and/or {U}.
        addCard(Zone.BATTLEFIELD, playerA, "Calciform Pools", 1);
        // {T}: Add {U}. If you played a land this turn, add {B} instead.
        addCard(Zone.BATTLEFIELD, playerA, "River of Tears", 1);

        // Sacrifice a land: Add one mana of any type the sacrificed land could produce.
        addCard(Zone.BATTLEFIELD, playerA, "Squandered Resources", 1);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);
        Assert.assertEquals("mana variations don't fit", 5, manaOptions.size());
        assertTrue("Expected to be able to produce 3 Green", manaOptions.canProduce(ManaType.GREEN, 3));
        assertTrue("Expected to be able to produce 2 Blue", manaOptions.canProduce(ManaType.BLUE, 2));
        assertTrue("Expected to be able to produce 2 Red", manaOptions.canProduce(ManaType.RED, 2));
        assertTrue("Expected to be able to produce 2 Colorless", manaOptions.canProduce(ManaType.COLORLESS, 2));
        assertTrue("Expected to be able to produce 1 White", manaOptions.canProduce(ManaType.WHITE, 1));
        assertTrue("Expected to be able to produce {C}{R}{G}{G}{G}{W}{U}{U}", manaOptions.canProduce("{C}{R}{G}{G}{G}{W}{U}{U}"));
    }

    @Test
    public void TestSquanderedResourcesWithManaConfluence() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 1);
        // {T}, Pay 1 life: Add one mana of any color.
        addCard(Zone.BATTLEFIELD, playerA, "Mana Confluence", 1);

        // Sacrifice a land: Add one mana of any type the sacrificed land could produce.
        addCard(Zone.BATTLEFIELD, playerA, "Squandered Resources", 1);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);
        Assert.assertEquals("mana variations don't fit", 3, manaOptions.size());
        assertTrue("Expected to be able to produce 2 Green", manaOptions.canProduce(ManaType.GREEN, 2));
        assertTrue("Expected to be able to produce {G}{G}{Any}{Any}", manaOptions.canProduce("{G}{G}{Any}{Any}"));
    }

    @Test
    public void TestTreasonousOgre() {
        setStrictChooseMode(true);
        // Dethrone
        // Pay 3 life: Add {R}.
        addCard(Zone.BATTLEFIELD, playerA, "Treasonous Ogre", 1);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);
        Assert.assertEquals("mana variations don't fit", 1, manaOptions.size());
        assertManaOptions("{R}{R}{R}{R}{R}{R}", manaOptions);
    }

    @Test
    public void TestSquanderedResourcesTwoSwamps() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 2);

        // Sacrifice a land: Add one mana of any type the sacrificed land could produce.
        addCard(Zone.BATTLEFIELD, playerA, "Squandered Resources", 1);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);
        Assert.assertEquals("mana variations don't fit", 3, manaOptions.size());
        assertTrue("Expected to be able to produce 3 Black", manaOptions.canProduce(ManaType.BLACK, 3));
    }
    
   @Test
    public void Test_ManaCache() {
        setStrictChooseMode(true);
        
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 2);

        // At the beginning of each player's end step, put a charge counter on Mana Cache for each untapped land that player controls.
        // Remove a charge counter from Mana Cache: Add {C}. Any player may activate this ability but only during their turn before the end step.
        addCard(Zone.BATTLEFIELD, playerA, "Mana Cache", 1);

        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();

       assertCounterCount("Mana Cache", CounterType.CHARGE, 2);
        
        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);
        Assert.assertEquals("mana variations don't fit", 3, manaOptions.size());
        assertTrue("Expected to be able to produce {C}{C}{B}{B}", manaOptions.canProduce("{C}{C}{B}{B}"));
    }

    @Test
    public void Test_ManaCacheOpponent() {
        setStrictChooseMode(true);
        
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 2);

        // At the beginning of each player's end step, put a charge counter on Mana Cache for each untapped land that player controls.
        // Remove a charge counter from Mana Cache: Add {C}. Any player may activate this ability but only during their turn before the end step.
        addCard(Zone.BATTLEFIELD, playerA, "Mana Cache", 1);

        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertCounterCount("Mana Cache", CounterType.CHARGE, 2);
        
        ManaOptions manaOptions = playerB.getAvailableManaTest(currentGame);
        Assert.assertEquals("mana variations don't fit", 1, manaOptions.size());
        assertManaOptions("{C}{C}", manaOptions);
    }    
    
    @Test
    public void testAvailableManaWithSpiritGuides() {
        // Exile Simian Spirit Guide from your hand: Add {R}.
        addCard(Zone.HAND, playerA, "Simian Spirit Guide", 1);
        // Exile Simian Spirit Guide from your hand: Add {R}.
        addCard(Zone.HAND, playerA, "Elvish Spirit Guide", 1);

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);
        Assert.assertEquals("mana variations don't fit", 2, manaOptions.size());
        assertTrue("Expected to be able to produce {R}{G}", manaOptions.canProduce("{R}{G}"));
    }
    
}
