package org.mage.test.cards.single.zen;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class EternityVesselTest extends CardTestPlayerBase {

    /*
    Eternity Vessel
    {6}
    Artifact
    Eternity Vessel enters the battlefield with X charge counters on it, where X is your life total.
    Landfall - Whenever a land enters the battlefield under your control, you may have your life total become the number of charge counters on Eternity Vessel.
    */
    private static final String eternityVessel = "Eternity Vessel";

    /*
    Lightning Bolt
    {R}
    Instant
    Lightning Bolt deals 3 damage to any target.
    */
    private static final String lightningBolt = "Lightning Bolt";

    @Test
    public void testEternityVessel() {
        addCard(Zone.HAND, playerA, lightningBolt);
        addCard(Zone.HAND, playerA, eternityVessel);
        addCard(Zone.HAND, playerA, "Mountain");
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 8);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, eternityVessel);

        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, lightningBolt, playerA);
        waitStackResolved(1, PhaseStep.POSTCOMBAT_MAIN);
        playLand(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Mountain");
        setChoice(playerA, true);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertLife(playerA, 20);
    }
}