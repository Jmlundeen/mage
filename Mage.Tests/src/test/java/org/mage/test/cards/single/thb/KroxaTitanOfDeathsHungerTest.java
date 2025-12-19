package org.mage.test.cards.single.thb;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class KroxaTitanOfDeathsHungerTest extends CardTestPlayerBase {

    /*
    Kroxa, Titan of Death's Hunger
    {B}{R}
    Legendary Creature - Elder Giant
    When Kroxa enters the battlefield, sacrifice it unless it escaped.
    Whenever Kroxa enters the battlefield or attacks, each opponent discards a card, then each opponent who didn't discard a nonland card this way loses 3 life.
    Escape—{B}{B}{R}{R}, Exile five other cards from your graveyard.
    6/6
    */
    private static final String kroxaTitanOfDeathsHunger = "Kroxa, Titan of Death's Hunger";

    @Test
    public void testKroxaTitanOfDeathsHungerEscape() {
        addCard(Zone.GRAVEYARD, playerA, "Swamp", 5);
        addCard(Zone.GRAVEYARD, playerA, kroxaTitanOfDeathsHunger);
        addCard(Zone.BATTLEFIELD, playerA, "Badlands", 4);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, kroxaTitanOfDeathsHunger + " with Escape");
        setChoice(playerA, "Swamp", 5);
        setChoice(playerA, "When {this} enters");
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, kroxaTitanOfDeathsHunger, 1);
        assertLife(playerB, 17); // Should lose 3 life since they couldn't discard
    }
}