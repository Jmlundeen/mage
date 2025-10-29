package org.mage.test.cards.single.bro;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class RecommissionTest extends CardTestPlayerBase {

    /*
    Recommission
    {1}{W}
    Sorcery
    Return target artifact or creature card with mana value 3 or less from your graveyard to the battlefield. If a creature enters the battlefield this way, it enters with an additional +1/+1 counter on it.
    */
    private static final String recommission = "Recommission";

    /*
    Balduvian Bears
    {1}{G}
    Creature - Bear
    
    2/2
    */
    private static final String balduvianBears = "Balduvian Bears";

    @Test
    public void testRecommission() {
        setStrictChooseMode(true);

        addCard(Zone.GRAVEYARD, playerA, balduvianBears);
        addCard(Zone.HAND, playerA, recommission);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, recommission, balduvianBears);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, balduvianBears, 1);
        assertCounterCount(balduvianBears, CounterType.P1P1, 1);
    }
}