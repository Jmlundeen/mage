package org.mage.test.cards.continuous;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author JayDi85
 */
public class GainAbilityDependenciesTest extends CardTestPlayerBase {

    /**
     * I had an elephant token equipped with Amorphous Axe attacking and a Tempered Sliver in play. The token did combat
     * damage to a player but it didnt get the +1/+1 counter it hsould be getting.
     * <p>
     * More details: https://github.com/magefree/mage/issues/6147
     */
    @Test
    public void test_SliverGain() {
        // Equipped creature gets +3/+0 and is every creature type.
        // Equip {3}
        addCard(Zone.BATTLEFIELD, playerA, "Amorphous Axe");
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 3);
        //
        // Create a 3/3 green Elephant creature token.
        addCard(Zone.HAND, playerA, "Elephant Ambush"); // {2}{G}{G}
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 4);
        //
        // Sliver creatures you control have “Whenever this creature deals combat damage to a player, put a +1/+1 counter on it.”
        addCard(Zone.BATTLEFIELD, playerA, "Tempered Sliver");

        // cast token
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Elephant Ambush");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPermanentCount("token must exist", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Elephant Token", 1);

        // equip
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Equip {3}", "Elephant Token");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);

        // attack with +1 token
        attack(3, playerA, "Elephant Token", playerB);
        checkPermanentCounters("must have counter", 3, PhaseStep.POSTCOMBAT_MAIN, playerA, "Elephant Token", CounterType.P1P1, 1);

        setStrictChooseMode(true);
        setStopAt(3, PhaseStep.END_TURN);
        execute();
    }
}
