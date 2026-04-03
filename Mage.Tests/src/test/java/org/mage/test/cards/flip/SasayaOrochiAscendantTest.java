package org.mage.test.cards.flip;

import mage.abilities.mana.ManaOptions;
import mage.constants.ManaType;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import static org.mage.test.utils.ManaOptionsTestUtils.*;

/**
 * @author LevelX2
 */
public class SasayaOrochiAscendantTest extends CardTestPlayerBase {

    /*
    Sasaya, Orochi Ascendant
    {1}{G}{G}
    Legendary Creature - Snake Monk
    Reveal your hand: If you have seven or more land cards in your hand, flip Sasaya, Orochi Ascendant.
    2/3

    Sasaya's Essence
    {1}{G}{G}
    Legendary Enchantment
    Whenever a land you control is tapped for mana, for each other land you control with the same name, add one mana of any type that land produced.
    */
    private static final String sasayaOrochiAscendant = "Sasaya, Orochi Ascendant";
    private static final String sasayasEssence = "Sasaya's Essence";

    @Test
    public void test_SasayasEssence_SimpleManaCalculation() {
        addCard(Zone.HAND, playerA, "Plains", 7);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 3);

        // Reveal your hand: If you have seven or more land cards in your hand, flip Sasaya, Orochi Ascendant.
        // Sasaya's Essence: Legendary Enchantment
        // Whenever a land you control is tapped for mana, for each other land you control with the same name, add one mana of any type that land produced.
        addCard(Zone.BATTLEFIELD, playerA, sasayaOrochiAscendant, 1);
        //
        // Mana pools don't empty as steps and phases end.
        addCard(Zone.HAND, playerA, "Upwelling", 1); // Enchantment {3}{G}
        //
        // At the beginning of your upkeep, you gain 1 life.
        addCard(Zone.BATTLEFIELD, playerB, "Fountain of Renewal", 1);

        // prepare Sasaya's Essence
        checkPermanentCount("before prepare", 1, PhaseStep.PRECOMBAT_MAIN, playerA, sasayasEssence, 0);
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Reveal your hand: If you have seven or more land cards in your hand, flip");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPermanentCount("after prepare", 1, PhaseStep.PRECOMBAT_MAIN, playerA, sasayasEssence, 1);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        // possible error: additional triggers from neutral card can break mana triggers and will calc wrong mana
        // reason: random triggers order on triggers iterator, can be fixed by linked map usage
        // x3 forest + x2 for each other forest
        ManaOptions manaOptions = playerA.getManaAvailable(currentGame);
        assertManaOptions("{G}{G}{G}" + "{G}{G}" + "{G}{G}" + "{G}{G}", manaOptions);
    }

    @Test
    public void testSasayasEssence() {
        addCard(Zone.HAND, playerA, "Plains", 7);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 3);

        // Reveal your hand: If you have seven or more land cards in your hand, flip Sasaya, Orochi Ascendant.
        // Sasaya's Essence: Legendary Enchantment
        // Whenever a land you control is tapped for mana, for each other land you control with the same name, add one mana of any type that land produced.
        addCard(Zone.BATTLEFIELD, playerA, sasayaOrochiAscendant, 1);
        // Mana pools don't empty as steps and phases end.
        addCard(Zone.HAND, playerA, "Upwelling", 1); // Enchantment {3}{G}

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Reveal your hand: If you have seven or more land cards in your hand, flip");

        activateManaAbility(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "{T}: Add {G}");
        activateManaAbility(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "{T}: Add {G}");
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Upwelling");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerA, sasayasEssence, 1);
        assertPermanentCount(playerA, "Upwelling", 1);

        assertManaPool(playerA, ManaType.GREEN, 2);

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);

        Assert.assertEquals("mana variations don't fit", 3, manaOptions.size());
        assertCanPay("{G}{G}{G}{G}{G}", manaOptions, currentGame);

    }

    @Test
    public void testSasayasEssence2() {
        addCard(Zone.HAND, playerA, "Plains", 7);
        addCard(Zone.BATTLEFIELD, playerA, "Brushland", 3);

        // Reveal your hand: If you have seven or more land cards in your hand, flip Sasaya, Orochi Ascendant.
        // Sasaya's Essence: Legendary Enchantment
        // Whenever a land you control is tapped for mana, for each other land you control with the same name, add one mana of any type that land produced.
        addCard(Zone.BATTLEFIELD, playerA, sasayaOrochiAscendant, 1);
        // Mana pools don't empty as steps and phases end.
        addCard(Zone.HAND, playerA, "Upwelling", 1); // Enchantment {3}{G}

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Reveal your hand: If you have seven or more land cards in your hand, flip");
        activateManaAbility(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "{T}: Add {G}");
        activateManaAbility(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "{T}: Add {G}");
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Upwelling");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerA, sasayasEssence, 1);
        assertPermanentCount(playerA, "Upwelling", 1);

        assertManaPool(playerA, ManaType.GREEN, 2);

        assertLife(playerA, 18);

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);

        Assert.assertEquals("mana variations don't fit", 5, manaOptions.size());
        assertCannotPay("{G}{G}{G}{W}{W}", manaOptions, currentGame);
        assertCanPay("{C}{C}{C}{G}{G}", manaOptions, currentGame);
        assertCanPay("{G}{G}{G}{G}{G}", manaOptions, currentGame);
        assertCanPay("{G}{G}{W}{W}{W}", manaOptions, currentGame);

    }

    @Test
    public void testSasayasEssence3() {
        addCard(Zone.HAND, playerA, "Plains", 7);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 3);
        // {1}, {T}: Add {R}{G}.
        addCard(Zone.BATTLEFIELD, playerA, "Mossfire Valley", 2);

        // Reveal your hand: If you have seven or more land cards in your hand, flip Sasaya, Orochi Ascendant.
        // Sasaya's Essence: Legendary Enchantment
        // Whenever a land you control is tapped for mana, for each other land you control with the same name, add one mana of any type that land produced.
        addCard(Zone.BATTLEFIELD, playerA, sasayaOrochiAscendant, 1);
        // Mana pools don't empty as steps and phases end.
        addCard(Zone.HAND, playerA, "Upwelling", 1); // Enchantment {3}{G}

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Reveal your hand: If you have seven or more land cards in your hand, flip");
        activateManaAbility(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "{T}: Add {G}");
        activateManaAbility(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "{T}: Add {G}");
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Upwelling");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerA, sasayasEssence, 1);
        assertPermanentCount(playerA, "Upwelling", 1);

        assertManaPool(playerA, ManaType.GREEN, 2);

        assertLife(playerA, 20);

        ManaOptions manaOptions = playerA.getAvailableManaTest(currentGame);

        Assert.assertEquals("mana variations don't fit", 7, manaOptions.size());
        assertCanPay("{R}{R}{R}{R}{G}{G}{G}{G}{G}", manaOptions, currentGame);
        assertCanPay("{R}{R}{R}{G}{G}{G}{G}{G}{G}", manaOptions, currentGame);
        assertCanPay("{R}{R}{G}{G}{G}{G}{G}{G}{G}", manaOptions, currentGame);
        assertCanPay("{R}{G}{G}{G}{G}{G}{G}{G}{G}", manaOptions, currentGame);
    }

}
