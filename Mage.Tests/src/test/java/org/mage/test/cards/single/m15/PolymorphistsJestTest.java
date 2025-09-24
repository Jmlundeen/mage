package org.mage.test.cards.single.m15;

import mage.constants.PhaseStep;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.permanent.Permanent;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import static org.junit.Assert.*;

/**
 *
 * @author Jmlundeen
 */
public class PolymorphistsJestTest extends CardTestPlayerBase {

    /*
    Polymorphist's Jest
    {1}{U}{U}
    Instant
    Until end of turn, each creature target player controls loses all abilities and becomes a blue Frog with base power and toughness 1/1.
    */
    private static final String polymorphistsJest = "Polymorphist's Jest";

    /*
    Bear Cub
    {1}{G}
    Creature - Bear
    
    2/2
    */
    private static final String bearCub = "Bear Cub";

    @Test
    public void testPolymorphistsJest() {
        setStrictChooseMode(true);

        addCard(Zone.BATTLEFIELD, playerB, bearCub);
        addCard(Zone.BATTLEFIELD, playerB, bearCub, 2);
        addCard(Zone.HAND, playerA, polymorphistsJest);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, polymorphistsJest, playerB);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        for (Permanent permanent : currentGame.getBattlefield().getActivePermanents(StaticFilters.FILTER_PERMANENT_CREATURES, playerB.getId(), currentGame)) {
            if (permanent.getControllerId() == playerB.getId()) {
                assertTrue("Creature should be a Frog", permanent.hasSubtype(SubType.FROG, currentGame));
                assertTrue("Creature should be blue", permanent.getColor(currentGame).isBlue());
                assertEquals("Creature should have 1 power", 1, permanent.getPower().getValue());
                assertEquals("Creature should have 1 toughness", 1, permanent.getToughness().getValue());
            } else {
                assertFalse("Creature should not be a Frog", permanent.hasSubtype(SubType.FROG, currentGame));
                assertTrue("Creature should be Green", permanent.getColor(currentGame).isGreen());
                assertEquals("Creature should have 2 power", 2, permanent.getPower().getValue());
                assertEquals("Creature should have 2 toughness", 2, permanent.getToughness().getValue());
            }
        }
    }
}