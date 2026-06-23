package org.mage.test.cards.single.fin;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class FreyaCrescentTest extends CardTestPlayerBase {

    /**
    * {@link mage.cards.f.FreyaCrescent Freya Crescent}
    * <br>
    * {R}
    * <br>
    * Legendary Creature — Rat Knight
    * <br>
    * Jump -- During your turn, Freya Crescent has flying.
    * {T}: Add {R}. Spend this mana only to cast an Equipment spell or activate an equip ability.
    * <br>
    * 1/1
    */
    private static final String freyaCrescent = "Freya Crescent";


    /**
    * {@link mage.cards.s.Sunforger Sunforger}
    * <br>
    * {3}
    * <br>
    * Artifact — Equipment
    * <br>
    * Equipped creature gets +4/+0.
    * {R}{W}, Unattach this Equipment: Search your library for a red or white instant card with mana value 4 or less and cast that card without paying its mana cost. Then shuffle.
    * Equip {3}
    */
    private static final String sunforger = "Sunforger";

    /**
     * {@link mage.cards.f.Fiendlash Fiendlash}
     * <br>
     * {1}{R}
     * <br>
     * Artifact -- Equipment
     * <br>
     * Equipped creature gets +2/+0 and has reach.
     Whenever equipped creature is dealt damage, it deals damage equal to its power to target player or planeswalker.
     Equip {2}{R}
     */
    private static final String fiendlash = "Fiendlash";

    @Test
    public void testFreyaCrescent() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, freyaCrescent);
        addCard(Zone.BATTLEFIELD, playerA, sunforger);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 5);
        addCard(Zone.BATTLEFIELD, playerA, fiendlash);

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {W}", 3);
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Equip {3}", freyaCrescent);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);

        checkPlayableAbility("Should not be able to use sunforger ability", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "{R}{W}, Unattach", false);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Equip {2}{R}", freyaCrescent);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, freyaCrescent, 1);
        assertPermanentCount(playerA, sunforger, 1);
        assertPermanentCount(playerA, fiendlash, 1);
        assertAttachedTo(playerA, sunforger, freyaCrescent, true);
        assertAttachedTo(playerA, fiendlash, freyaCrescent, true);
    }
}