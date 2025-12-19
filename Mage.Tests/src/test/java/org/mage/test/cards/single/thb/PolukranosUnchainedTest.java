package org.mage.test.cards.single.thb;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class PolukranosUnchainedTest extends CardTestPlayerBase {

    /*
    Polukranos, Unchained
    {2}{B}{G}
    Legendary Creature - Zombie Hydra
    Polukranos enters the battlefield with six +1/+1 counters on it. It escapes with twelve +1/+1 counters on it instead.
    If damage would be dealt to Polukranos while it has a +1/+1 counter on it, prevent that damage and remove that many +1/+1 counters from it.
    {1}{B}{G}: Polukranos fights another target creature.
    Escape—{4}{B}{G}, Exile six other cards from your graveyard.
    */
    private static final String polukranosUnchained = "Polukranos, Unchained";

    @Test
    public void testPolukranosUnchainedEscape() {
        addCard(Zone.GRAVEYARD, playerA, polukranosUnchained);
        addCard(Zone.GRAVEYARD, playerA, "Swamp", 6);
        addCard(Zone.BATTLEFIELD, playerA, "Bayou", 6);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, polukranosUnchained + " with Escape");
        setChoice(playerA, "Swamp", 6);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, polukranosUnchained, 1);
        assertCounterCount(polukranosUnchained, CounterType.P1P1, 12);
    }
}