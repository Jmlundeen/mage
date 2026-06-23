package org.mage.test.cards.single.bot;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class JetfireIngeniousScientistJetfireAirGuardianTest extends CardTestPlayerBase {

    /**
    * {@link mage.cards.j.JetfireIngeniousScientist}
    * <br>
    * Jetfire, Ingenious Scientist
    * <br>
    * {4}{U}
    * <br>
    * Legendary Artifact Creature — Robot
    * <br>
    * More Than Meets the Eye {3}{U} (You may cast this card converted for {3}{U}.)
    * Flying
    * Remove one or more +1/+1 counters from among artifacts you control: Target player adds that much {C}. This mana can't be spent to cast nonartifact spells. Convert Jetfire.
    * <br>
    * 3/4
    */
    private static final String jetfireIngeniousScientist = "Jetfire, Ingenious Scientist";
    /**
    * {@link mage.cards.j.JetfireIngeniousScientist}
    * <br>
    * Jetfire, Air Guardian
    * <br>
    * Legendary Artifact — Vehicle
    * <br>
    * Living metal (During your turn, this Vehicle is also a creature.)
    * Flying
    * {U}{U}{U}: Convert Jetfire, then adapt 3. (If it has no +1/+1 counters on it, put three +1/+1 counters on it.)
    * <br>
    * 3/4
    */
    private static final String jetfireAirGuardian = "Jetfire, Air Guardian";

    /**
     * {@link mage.cards.t.TormodsCrypt Tormod's Crypt}
     * <br>
     * {0}
     * <br>
     * Artifact
     * <br>
     * {T}, Sacrifice this artifact: Exile target player's graveyard.
     */
    private static final String tormodsCrypt = "Tormod's Crypt";

    /**
     * {@link mage.cards.b.BalduvianBears Balduvian Bears}
     * <br>
     * {1}{G}
     * <br>
     * Creature -- Bear
     * <br>
     *
     * <br>
     * 2/2
     */
    private static final String balduvianBears = "Balduvian Bears";

    @Test
    public void testJetfireIngeniousScientist() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, jetfireIngeniousScientist);
        addCard(Zone.BATTLEFIELD, playerA, tormodsCrypt);
        addCard(Zone.HAND, playerA, balduvianBears);
        addCard(Zone.BATTLEFIELD, playerA, "Forest");

        addCounters(1, PhaseStep.PRECOMBAT_MAIN, playerA, jetfireIngeniousScientist, CounterType.P1P1, 2);
        addCounters(1, PhaseStep.PRECOMBAT_MAIN, playerA, tormodsCrypt, CounterType.P1P1, 2);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Remove one or more");
        addTarget(playerA, playerA);
        setChoiceAmount(playerA, 4);
        setChoice(playerA, jetfireIngeniousScientist);
        setChoice(playerA, tormodsCrypt);
        setChoice(playerA, "X=2");
        setChoice(playerA, "X=2");

        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkManaPool("Mana should be added to pool", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "C", 4);
        checkPlayableAbility("Can't cast bear", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast " + balduvianBears, false);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }
}