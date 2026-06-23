package org.mage.test.cards.single.who;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class BiggerOnTheInsideTest extends CardTestPlayerBase {

    /**
    * {@link mage.cards.b.BiggerOnTheInside Bigger on the Inside}
    * <br>
    * {3}{R}{G}
    * <br>
    * Enchantment — Aura
    * <br>
    * Enchant artifact or land
    * Enchanted permanent has "{T}: Target player adds two mana of any one color. The next spell they cast this turn has cascade." (When they cast their next spell, they exile cards from the top of their library until they exile a nonland card that costs less. They may cast it without paying its mana cost. They put the exiled cards on the bottom in a random order.)
    */
    private static final String biggerOnTheInside = "Bigger on the Inside";


    /**
    * {@link mage.cards.b.BalduvianBears Balduvian Bears}
    * <br>
    * {1}{G}
    * <br>
    * Creature — Bear
    * <br>
    * 
    * <br>
    * 2/2
    */
    private static final String balduvianBears = "Balduvian Bears";


    /**
    * {@link mage.cards.f.FugitiveWizard Fugitive Wizard}
    * <br>
    * {U}
    * <br>
    * Creature — Human Wizard
    * <br>
    * 
    * <br>
    * 1/1
    */
    private static final String fugitiveWizard = "Fugitive Wizard";

    /**
     * {@link mage.cards.u.Upwelling Upwelling}
     * <br>
     * {3}{G}
     * <br>
     * Enchantment
     * <br>
     * Players don't lose unspent mana as steps and phases end.
     */
    private static final String upwelling = "Upwelling";


    @Test
    public void testBiggerOnTheInsideController() {
        setStrictChooseMode(true);
        addCard(Zone.HAND, playerA, biggerOnTheInside);
        addCard(Zone.BATTLEFIELD, playerA, "Taiga", 5);
        addCard(Zone.BATTLEFIELD, playerA, "Island");
        addCard(Zone.HAND, playerA, balduvianBears);
        addCard(Zone.LIBRARY, playerA, fugitiveWizard);

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {R}", 4);
        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {G}");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, biggerOnTheInside, "Island");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Target player adds", playerA);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        setChoice(playerA, "Green");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, balduvianBears);
        setChoice(playerA, true);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, biggerOnTheInside, 1);
        assertPermanentCount(playerA, balduvianBears, 1);
        assertPermanentCount(playerA, fugitiveWizard, 1);
    }

    @Test
    public void testBiggerOnTheInsideOpponent() {
        setStrictChooseMode(true);
        addCard(Zone.HAND, playerA, biggerOnTheInside);
        addCard(Zone.BATTLEFIELD, playerA, "Taiga", 5);
        addCard(Zone.BATTLEFIELD, playerA, "Island");
        addCard(Zone.BATTLEFIELD, playerB, upwelling);
        addCard(Zone.HAND, playerB, balduvianBears);
        addCard(Zone.LIBRARY, playerB, fugitiveWizard);

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {R}", 4);
        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {G}");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, biggerOnTheInside, "Island");

        activateAbility(2, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Target player adds", playerB);
        waitStackResolved(2, PhaseStep.PRECOMBAT_MAIN, playerB);
        setChoice(playerB, "Green");

        castSpell(2, PhaseStep.POSTCOMBAT_MAIN, playerB, balduvianBears);
        setChoice(playerB, true);

        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, biggerOnTheInside, 1);
        assertPermanentCount(playerB, balduvianBears, 1);
        assertPermanentCount(playerB, fugitiveWizard, 1);
    }
}