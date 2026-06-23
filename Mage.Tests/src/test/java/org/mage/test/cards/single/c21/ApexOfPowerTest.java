package org.mage.test.cards.single.c21;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class ApexOfPowerTest extends CardTestPlayerBase {

    /**
    * {@link mage.cards.a.ApexOfPower Apex of Power}
    * <br>
    * {7}{R}{R}{R}
    * <br>
    * Sorcery
    * <br>
    * Exile the top seven cards of your library. Until end of turn, you may cast spells from among them.
    * If this spell was cast from your hand, add ten mana of any one color.
    */
    private static final String apexOfPower = "Apex of Power";


    @Test
    public void testApexOfPower() {
        setStrictChooseMode(true);
        addCard(Zone.HAND, playerA, apexOfPower);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 10);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, apexOfPower);
        setChoice(playerA, "Red");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkManaPool("Mana pool should have 10 red mana", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "R", 10);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }
}