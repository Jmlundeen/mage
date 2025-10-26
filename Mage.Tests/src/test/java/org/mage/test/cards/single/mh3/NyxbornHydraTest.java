package org.mage.test.cards.single.mh3;

import mage.abilities.keyword.ReachAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class NyxbornHydraTest extends CardTestPlayerBase {

    /*
    Nyxborn Hydra
    {X}{G}
    Enchantment Creature - Hydra
    Bestow {X}{G}{G}
    Reach, trample
    Nyxborn Hydra enters the battlefield with X +1/+1 counters on it.
    Enchanted creature gets +1/+1 for each +1/+1 counter on Nyxborn Hydra and has reach and trample.
    */
    private static final String nyxbornHydra = "Nyxborn Hydra";

    /*
    Balduvian Bears
    {1}{G}
    Creature - Bear
    
    2/2
    */
    private static final String balduvianBears = "Balduvian Bears";

    @Test
    public void testNyxbornHydra() {
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 20);
        addCard(Zone.BATTLEFIELD, playerA, balduvianBears);
        addCard(Zone.HAND, playerA, nyxbornHydra);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, nyxbornHydra + " using bestow" , balduvianBears);
        setChoiceAmount(playerA, 18);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, nyxbornHydra, 1);
        assertPowerToughness(playerA, nyxbornHydra, 18, 19);
        assertPowerToughness(playerA, balduvianBears, 20, 20);
        assertAbility(playerA, balduvianBears, ReachAbility.getInstance(), true);
        assertAbility(playerA, balduvianBears, TrampleAbility.getInstance(), true);
    }
}