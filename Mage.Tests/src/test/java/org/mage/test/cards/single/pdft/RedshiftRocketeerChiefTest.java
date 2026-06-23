package org.mage.test.cards.single.pdft;

import mage.abilities.keyword.FirstStrikeAbility;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class RedshiftRocketeerChiefTest extends CardTestPlayerBase {

    /**
    * {@link mage.cards.r.RedshiftRocketeerChief Redshift, Rocketeer Chief}
    * <br>
    * {R}{G}
    * <br>
    * Legendary Creature — Goblin Pilot
    * <br>
    * Vigilance
    * {T}: Add X mana of any one color, where X is Redshift's power. Spend this mana only to activate abilities.
    * Exhaust -- {10}{R}{G}: Put any number of permanent cards from your hand onto the battlefield. (Activate each exhaust ability only once.)
    * <br>
    * 2/3
    */
    private static final String redshiftRocketeerChief = "Redshift, Rocketeer Chief";


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
     * {@link mage.cards.e.EmeraldDragonfly Emerald Dragonfly}
     * <br>
     * {1}{G}
     * <br>
     * Creature -- Insect
     * <br>
     * Flying
     {G}{G}: This creature gains first strike until end of turn.
     * <br>
     * 1/1
     */
    private static final String emeraldDragonfly = "Emerald Dragonfly";

    @Test
    public void testRedshiftRocketeerChief() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, redshiftRocketeerChief);
        addCard(Zone.BATTLEFIELD, playerA, emeraldDragonfly);
        addCard(Zone.HAND, playerA, balduvianBears);

        checkPlayableAbility("can't cast Balduvian Bears", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast " + balduvianBears, false);
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{G}{G}: {this}");
        setChoice(playerA, "Green");

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, redshiftRocketeerChief, 1);
        assertPermanentCount(playerA, emeraldDragonfly, 1);
        assertAbility(playerA, emeraldDragonfly, FirstStrikeAbility.getInstance(), true);
    }

    @Test
    public void testRedshiftRocketeerChiefExhaust() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, redshiftRocketeerChief);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain");
        addCard(Zone.HAND, playerA, balduvianBears, 2);
        addCard(Zone.HAND, playerA, emeraldDragonfly);

        addCounters(1, PhaseStep.PRECOMBAT_MAIN, playerA, redshiftRocketeerChief, CounterType.P1P1, 9);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Exhaust");
        setChoice(playerA, "Green");
        setChoice(playerA, balduvianBears, 2);
        setChoice(playerA, emeraldDragonfly);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, redshiftRocketeerChief, 1);
        assertPermanentCount(playerA, balduvianBears, 2);
        assertPermanentCount(playerA, emeraldDragonfly, 1);
        assertTapped(redshiftRocketeerChief, true);
    }
}