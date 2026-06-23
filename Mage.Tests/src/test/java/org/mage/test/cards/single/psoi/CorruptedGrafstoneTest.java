package org.mage.test.cards.single.psoi;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class CorruptedGrafstoneTest extends CardTestPlayerBase {

    /**
    * {@link mage.cards.c.CorruptedGrafstone Corrupted Grafstone}
    * <br>
    * {2}
    * <br>
    * Artifact
    * <br>
    * This artifact enters tapped.
    * {T}: Choose a color of a card in your graveyard. Add one mana of that color.
    */
    private static final String corruptedGrafstone = "Corrupted Grafstone";


    /**
    * {@link mage.cards.b.BalduvianBears Balduvian Bears}
    * <br>
    * {1}{G}
    * <br>
    * Creature -- Bear
    * <br>
    * 
    * <br>
    * 2/2
    */
    private static final String balduvianBears = "Balduvian Bears";

    /**
     * {@link mage.cards.s.SilvercoatLion Silvercoat Lion}
     * <br>
     * {1}{W}
     * <br>
     * Creature -- Cat
     * <br>
     *
     * <br>
     * 2/2
     */
    private static final String silvercoatLion = "Silvercoat Lion";


    @Test
    public void testCorruptedGrafstone() {
        setStrictChooseMode(true);

        addCard(Zone.BATTLEFIELD, playerA, corruptedGrafstone);
        addCard(Zone.GRAVEYARD, playerA, balduvianBears);
        addCard(Zone.HAND, playerA, balduvianBears);
        addCard(Zone.BATTLEFIELD, playerA, "Island");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, balduvianBears);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, balduvianBears, 1);
        assertGraveyardCount(playerA, balduvianBears, 1);
        assertTapped(corruptedGrafstone, true);
    }

    @Test
    public void testCorruptedGrafstoneMultipleColors() {
        setStrictChooseMode(true);

        addCard(Zone.BATTLEFIELD, playerA, corruptedGrafstone);
        addCard(Zone.GRAVEYARD, playerA, balduvianBears);
        addCard(Zone.GRAVEYARD, playerA, silvercoatLion);
        addCard(Zone.HAND, playerA, balduvianBears);
        addCard(Zone.BATTLEFIELD, playerA, "Island");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, balduvianBears);
        setChoice(playerA, "Green");

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, balduvianBears, 1);
        assertGraveyardCount(playerA, balduvianBears, 1);
        assertGraveyardCount(playerA, silvercoatLion, 1);
        assertTapped(corruptedGrafstone, true);
    }
}