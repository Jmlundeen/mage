package org.mage.test.cards.single.clb;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class VhalCandlekeepResearcherTest extends CardTestPlayerBase {

    /**
    * {@link mage.cards.v.VhalCandlekeepResearcher Vhal, Candlekeep Researcher}
    * <br>
    * {3}{U}
    * <br>
    * Legendary Creature — Human Wizard
    * <br>
    * Vigilance
    * {T}: Add an amount of {C} equal to Vhal's toughness. This mana can't be spent to cast spells from your hand.
    * Choose a Background (You can have a Background as a second commander.)
    * <br>
    * 2/3
    */
    private static final String vhalCandlekeepResearcher = "Vhal, Candlekeep Researcher";


    /**
    * {@link mage.cards.m.MyrQuadropod Myr Quadropod}
    * <br>
    * {4}
    * <br>
    * Artifact Creature — Myr
    * <br>
    * {3}: Switch this creature's power and toughness until end of turn.
    * <br>
    * 1/4
    */
    private static final String myrQuadropod = "Myr Quadropod";


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


    @Test
    public void testVhalCandlekeepResearcher() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, vhalCandlekeepResearcher);
        addCard(Zone.BATTLEFIELD, playerA, myrQuadropod);
        addCard(Zone.BATTLEFIELD, playerA, "Forest");
        addCard(Zone.HAND, playerA, balduvianBears);

        checkPlayableAbility("Can't cast from hand", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast " + balduvianBears, false);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{3}: Switch");

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, myrQuadropod, 1);
        assertPowerToughness(playerA, myrQuadropod, 4, 1);
    }
}