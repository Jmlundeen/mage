package org.mage.test.cards.continuous;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class CanBlockAdditionalCreatureAttachedEffectTest extends CardTestPlayerBase {

    /*
    Echo Circlet
    {2}
    Artifact — Equipment

    Equipped creature can block an additional creature each combat.

    Equip {1}
     */
    public static final String echoCirclet = "Echo Circlet";

    @Test
    public void testCanBlockAttached() {
        setStrictChooseMode(true);

        addCard(Zone.BATTLEFIELD, playerA, echoCirclet);
        addCard(Zone.BATTLEFIELD, playerA, "Island");
        addCard(Zone.BATTLEFIELD, playerA, "Balduvian Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Noble Hierarch");
        addCard(Zone.BATTLEFIELD, playerB, "Carrion Ants");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Equip", "Balduvian Bears");

        attack(2, playerB, "Noble Hierarch");
        attack(2, playerB, "Carrion Ants");
        block(2, playerA, "Balduvian Bears", "Noble Hierarch");
        block(2, playerA, "Balduvian Bears", "Carrion Ants");

        setChoice(playerA, "X=1", 2);

        setStopAt(2, PhaseStep.END_TURN);
        execute();

        assertGraveyardCount(playerB, "Noble Hierarch", 1);
        assertGraveyardCount(playerB, "Carrion Ants", 1);
    }
}
