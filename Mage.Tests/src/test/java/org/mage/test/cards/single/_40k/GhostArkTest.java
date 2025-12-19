package org.mage.test.cards.single._40k;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author anonymous
 */
public class GhostArkTest extends CardTestPlayerBase {

    /*
    Ghost Ark
    {4}
    Artifact - Vehicle
    Flying
    Repair Barge -- Whenever Ghost Ark becomes crewed, each artifact creature card in your graveyard gains unearth {3} until end of turn.
    Crew 2
    3/3
    */
    private static final String ghostArk = "Ghost Ark";

    /*
    Balduvian Bears
    {1}{G}
    Creature - Bear
    
    2/2
    */
    private static final String balduvianBears = "Balduvian Bears";

    /*
    Memnite
    {0}
    Artifact Creature - Construct

    1/1
    */
    private static final String memnite = "Memnite";

    @Test
    public void testGhostArk() {
        addCard(Zone.BATTLEFIELD, playerA, ghostArk);
        addCard(Zone.BATTLEFIELD, playerA, balduvianBears);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 3);
        addCard(Zone.GRAVEYARD, playerA, memnite);
        addCard(Zone.GRAVEYARD, playerB, memnite);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Crew 2");
        setChoice(playerA, balduvianBears);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Unearth {3}");

        attack(1, playerA, ghostArk);
        attack(1, playerA, memnite);

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        assertLife(playerB, 20 - 3 - 1);
        assertPermanentCount(playerA, ghostArk, 1);
        assertPermanentCount(playerA, balduvianBears, 1);
        assertExileCount(playerA, memnite, 1);
    }
}