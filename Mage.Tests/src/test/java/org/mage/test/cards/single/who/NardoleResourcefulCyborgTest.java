package org.mage.test.cards.single.who;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class NardoleResourcefulCyborgTest extends CardTestPlayerBase {

    /**
    * {@link mage.cards.n.NardoleResourcefulCyborg Nardole, Resourceful Cyborg}
    * <br>
    * {1}{U}
    * <br>
    * Legendary Artifact Creature — Scientist
    * <br>
    * {T}: Add {U} for each counter on Nardole. Spend this mana only to cast noncreature spells.
    * Undying (When this creature dies, if it had no +1/+1 counters on it, return it to the battlefield under its owner's control with a +1/+1 counter on it.)
    * Doctor's companion (You can have two commanders if the other is the Doctor.)
    * <br>
    * 1/2
    */
    private static final String nardoleResourcefulCyborg = "Nardole, Resourceful Cyborg";


    /**
    * {@link mage.cards.b.BalduvianBears Balduvian Bears}
    * <br>
    * {1}{G}
    * <br>
    * Creature — Bear
    * <br>
    * 
    * <br>
    * 2/2
    */
    private static final String balduvianBears = "Balduvian Bears";


    /**
    * {@link mage.cards.o.Opt Opt}
    * <br>
    * {U}
    * <br>
    * Instant
    * <br>
    * Scry 1. (Look at the top card of your library. You may put that card on the bottom.)
    * Draw a card.
    */
    private static final String opt = "Opt";


    @Test
    public void testNardoleResourcefulCyborg() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, nardoleResourcefulCyborg);
        addCard(Zone.HAND, playerA, opt);
        addCard(Zone.HAND, playerA, balduvianBears);
        addCard(Zone.BATTLEFIELD, playerA, "Forest");

        addCounters(1, PhaseStep.PRECOMBAT_MAIN, playerA, nardoleResourcefulCyborg, CounterType.P1P1, 1);

        checkPlayableAbility("can't cast balduvian bears", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast Balduvian Bears", false);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, opt);
        addTarget(playerA, "Mountain");

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }
}