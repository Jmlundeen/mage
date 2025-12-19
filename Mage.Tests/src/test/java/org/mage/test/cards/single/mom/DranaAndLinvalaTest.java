package org.mage.test.cards.single.mom;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author anonymous
 */
public class DranaAndLinvalaTest extends CardTestPlayerBase {

    /*
    Drana and Linvala
    {1}{W}{W}{B}
    Legendary Creature - Vampire Angel
    Flying, vigilance
    Activated abilities of creatures your opponents control can't be activated.
    Drana and Linvala has all activated abilities of all creatures your opponents control. You may spend mana as though it were mana of any color to activate those abilities.
    3/4
    */
    private static final String dranaAndLinvala = "Drana and Linvala";

    /*
    Soul of Windgrace
    {1}{B}{R}{G}
    Legendary Creature - Cat Avatar
    Whenever Soul of Windgrace enters the battlefield or attacks, you may put a land card from a graveyard onto the battlefield tapped under your control.
    {G}, Discard a land card: You gain 3 life.
    {1}{R}, Discard a land card: Draw a card.
    {2}{B}, Discard a land card: Soul of Windgrace gains indestructible until end of turn. Tap it.
    5/4
    */
    private static final String soulOfWindgrace = "Soul of Windgrace";

    /*
    Groundskeeper
    {G}
    Creature - Human Druid
    {1}{G}: Return target basic land card from your graveyard to your hand.
    1/1
    */
    private static final String groundskeeper = "Groundskeeper";

    @Test
    public void testDranaAndLinvala() {
        addCard(Zone.BATTLEFIELD, playerA, dranaAndLinvala);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 6);
        addCard(Zone.BATTLEFIELD, playerB, soulOfWindgrace);
        addCard(Zone.BATTLEFIELD, playerB, groundskeeper);
        addCard(Zone.BATTLEFIELD, playerB, "Forest", 2);
        addCard(Zone.HAND, playerA, "Island");
        addCard(Zone.HAND, playerB, "Forest");
        addCard(Zone.GRAVEYARD, playerB, "Forest");

        checkPlayableAbility("Player B can't activate Groundskeeper", 1, PhaseStep.PRECOMBAT_MAIN, playerB,
                "{1}{G}: Return target basic land card from your graveyard to your hand.", false);
        checkPlayableAbility("Player B can't activate Soul of Windgrace", 1, PhaseStep.PRECOMBAT_MAIN, playerB,
                "{G}, Discard a land card: You gain 3 life.", false);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{G}, Discard a land card: You gain 3 life.");
        setChoice(playerA, "Island");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{1}{G}: Return ");
        addTarget(playerA, "Island");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();
    }
}