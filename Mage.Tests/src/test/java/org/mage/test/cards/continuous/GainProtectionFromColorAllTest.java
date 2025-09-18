package org.mage.test.cards.continuous;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class GainProtectionFromColorAllTest extends CardTestPlayerBase {

    /*
    Akroma's Blessing
    {2}{W}
    Instant
    Choose a color. Creatures you control gain protection from the chosen color until end of turn.
    Cycling {W} <i>({W}, Discard this card: Draw a card.)</i>
    */
    private static final String akromasBlessing = "Akroma's Blessing";


    /*
    Lightning Bolt
    {R}
    Instant
    Lightning Bolt deals 3 damage to any target.
    */
    private static final String lightningBolt = "Lightning Bolt";

    /*
    Bear Cub
    {1}{G}
    Creature - Bear

    2/2
    */
    private static final String bearCub = "Bear Cub";

    @Test
    public void testGainProtectionFromColorAll() {
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 3);
        addCard(Zone.BATTLEFIELD, playerB, "Mountain", 1);
        addCard(Zone.BATTLEFIELD, playerA, bearCub, 1);

        addCard(Zone.HAND, playerA, akromasBlessing);
        addCard(Zone.HAND, playerB, lightningBolt);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, akromasBlessing);
        setChoice(playerA, "Red"); // Choose a color

        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerB, lightningBolt);
        addTarget(playerB, bearCub);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        try {
            execute();
            Assert.fail("Expected to fail on targeting bear cub with protection from red");
        } catch (AssertionError e) {
            Assert.assertTrue("Expected to fail on targeting bear cub",
                    e.getMessage().contains("Targets list was setup by addTarget with [Bear Cub], but not used"));
        }
    }
}
