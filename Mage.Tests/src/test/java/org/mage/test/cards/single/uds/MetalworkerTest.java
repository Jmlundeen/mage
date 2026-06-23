package org.mage.test.cards.single.uds;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class MetalworkerTest extends CardTestPlayerBase {

    /**
    * {@link mage.cards.m.Metalworker Metalworker}
    * <br>
    * {3}
    * <br>
    * Artifact Creature — Construct
    * <br>
    * {T}: Reveal any number of artifact cards in your hand. Add {C}{C} for each card revealed this way.
    * <br>
    * 1/2
    */
    private static final String metalworker = "Metalworker";


    @Test
    public void testMetalworker() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, metalworker);
        addCard(Zone.HAND, playerA, metalworker, 3);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Reveal any number");
        setChoice(playerA, metalworker, 3);

        checkManaPool("Check mana", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "C", 6);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertTapped(metalworker, true);
    }
}