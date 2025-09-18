package org.mage.test.cards.single.ons;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class CustodyBattleTest extends CardTestPlayerBase {

    /*
    Custody Battle
    {1}{R}
    Enchantment - Aura
    Enchant creature
    Enchanted creature has "At the beginning of your upkeep, target opponent gains control of this creature unless you sacrifice a land."
    */
    private static final String custodyBattle = "Custody Battle";

    /*
    Bear Cub
    {1}{G}
    Creature - Bear
    
    2/2
    */
    private static final String bearCub = "Bear Cub";

    @Test
    public void testCustodyBattleSacrificeLand() {
        addCard(Zone.BATTLEFIELD, playerB, "Forest", 2);
        addCard(Zone.BATTLEFIELD, playerB, bearCub);
        addCard(Zone.HAND, playerA, custodyBattle);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, custodyBattle, bearCub);
        addTarget(playerB, playerA);
        setChoice(playerB, true); // Sacrifice a land
        setChoice(playerB, "Forest");

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, bearCub, 0);
        assertPermanentCount(playerB, bearCub, 1);
    }

    @Test
    public void testCustodyBattleNoSacrificeLand() {
        addCard(Zone.BATTLEFIELD, playerB, "Forest", 2);
        addCard(Zone.BATTLEFIELD, playerB, bearCub);
        addCard(Zone.HAND, playerA, custodyBattle);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, custodyBattle, bearCub);
        addTarget(playerB, playerA);
        setChoice(playerB, false); // Do not Sacrifice a land

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, bearCub, 1);
        assertPermanentCount(playerB, bearCub, 0);
    }

}