package org.mage.test.cards.single.fin;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.filter.Filter;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class TorgalAFineHoundTest extends CardTestPlayerBase {

    /*
    Torgal, A Fine Hound
    {1}{G}
    Legendary Creature - Wolf
    Whenever you cast your first Human creature spell each turn, that creature enters with an additional +1/+1 counter on it for each Dog and/or Wolf you control.
    {T}: Add one mana of any color.
    2/2
    */
    private static final String torgalAFineHound = "Torgal, A Fine Hound";

    /*
    Tundra Wolves
    {W}
    Creature - Wolf
    First strike <i>(This creature deals combat damage before creatures without first strike.)</i>
    1/1
    */
    private static final String tundraWolves = "Tundra Wolves";

    /*
    Elite Vanguard
    {W}
    Creature - Human Soldier
    
    2/1
    */
    private static final String eliteVanguard = "Elite Vanguard";

    @Test
    public void testTorgalAFineHound() {
        setStrictChooseMode(true);

        addCard(Zone.BATTLEFIELD, playerA, torgalAFineHound);
        addCard(Zone.BATTLEFIELD, playerA, tundraWolves, 10);
        addCard(Zone.HAND, playerA, eliteVanguard, 2);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 4);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, eliteVanguard);

        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, eliteVanguard);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, eliteVanguard, 2);
        assertPowerToughness(playerA, eliteVanguard, 13, 12, Filter.ComparisonScope.Any);
        assertPowerToughness(playerA, eliteVanguard, 2, 1, Filter.ComparisonScope.Any);
    }
}