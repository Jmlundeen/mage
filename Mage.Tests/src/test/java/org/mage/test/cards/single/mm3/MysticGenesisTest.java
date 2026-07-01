package org.mage.test.cards.single.mm3;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class MysticGenesisTest extends CardTestPlayerBase {

    /**
    * {@link mage.cards.m.MysticGenesis Mystic Genesis}
    * <br>
    * {2}{G}{U}{U}
    * <br>
    * Instant
    * <br>
    * Counter target spell. Create an X/X green Ooze creature token, where X is that spell's mana value.
    */
    private static final String mysticGenesis = "Mystic Genesis";


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


    @Test
    public void testMysticGenesis() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, "Tropical Island", 5);
        addCard(Zone.HAND, playerA, mysticGenesis);
        addCard(Zone.HAND, playerB, balduvianBears);
        addCard(Zone.BATTLEFIELD, playerB, "Forest", 2);

        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, balduvianBears);
        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerA, mysticGenesis, balduvianBears);

        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Ooze Token", 1);
        assertPowerToughness(playerA, "Ooze Token", 2, 2);
    }
}