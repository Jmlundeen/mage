package org.mage.test.cards.single.mom;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class TandemTakedownTest extends CardTestPlayerBase {

    /*
    Tandem Takedown
    {1}{G}{G}
    Instant
    Up to two target creatures you control each get +1/+0 until end of turn. They each deal damage equal to their power to another target creature, planeswalker, or battle.
    */
    private static final String tandemTakedown = "Tandem Takedown";

    /*
    Balduvian Bears
    {1}{G}
    Creature - Bear
    
    2/2
    */
    private static final String balduvianBears = "Balduvian Bears";

    /*
    Bear Cub
    {1}{G}
    Creature - Bear
    
    2/2
    */
    private static final String bearCub = "Bear Cub";

    /*
    Catacomb Slug
    {4}{B}
    Creature - Slug
    
    2/6
    */
    private static final String catacombSlug = "Catacomb Slug";

    @Test
    public void testTandemTakedown() {
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 3);
        addCard(Zone.BATTLEFIELD, playerA, balduvianBears);
        addCard(Zone.BATTLEFIELD, playerA, bearCub);
        addCard(Zone.BATTLEFIELD, playerB, catacombSlug);
        addCard(Zone.HAND, playerA, tandemTakedown);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, tandemTakedown, balduvianBears + "^" + bearCub);
        addTarget(playerA, catacombSlug);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        setStrictChooseMode(true);
        execute();

        assertPermanentCount(playerA, balduvianBears, 1);
        assertPermanentCount(playerA, bearCub, 1);
        assertGraveyardCount(playerB, catacombSlug, 1);
    }
}