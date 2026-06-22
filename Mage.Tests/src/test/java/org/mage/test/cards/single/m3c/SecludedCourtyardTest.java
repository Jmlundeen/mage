package org.mage.test.cards.single.m3c;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class SecludedCourtyardTest extends CardTestPlayerBase {

    /**
    * {@link mage.cards.s.SecludedCourtyard Secluded Courtyard}
    * <br>
    * 
    * <br>
    * Land
    * <br>
    * As this land enters, choose a creature type.
    * {T}: Add {C}.
    * {T}: Add one mana of any color. Spend this mana only to cast a creature spell of the chosen type or activate an ability of a creature source of the chosen type.
    */
    private static final String secludedCourtyard = "Secluded Courtyard";


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


    /**
    * {@link mage.cards.w.WallOfFire Wall of Fire}
    * <br>
    * {1}{R}{R}
    * <br>
    * Creature — Wall
    * <br>
    * Defender (This creature can't attack.)
    * {R}: This creature gets +1/+0 until end of turn.
    * <br>
    * 0/5
    */
    private static final String wallOfFire = "Wall of Fire";

    /**
     * {@link mage.cards.p.PlagueBoiler Plague Boiler}
     * <br>
     * {3}
     * <br>
     * Artifact
     * <br>
     * At the beginning of your upkeep, put a plague counter on this artifact.
     {1}{B}{G}: Put a plague counter on this artifact or remove a plague counter from it.
     When this artifact has three or more plague counters on it, sacrifice it. If you do, destroy all nonland permanents.
     */
    private static final String plagueBoiler = "Plague Boiler";

    /**
     * {@link mage.cards.j.JeditOjanenMercenary Jedit Ojanen, Mercenary}
     * <br>
     * {1}{W}{U}
     * <br>
     * Legendary Creature -- Cat Mercenary
     * <br>
     * Whenever Jedit Ojanen or another legendary creature you control enters, you may pay {G}. If you do, create a 2/2 green Cat Warrior creature token with forestwalk. (It can't be blocked as long as defending player controls a Forest.)
     * <br>
     * 3/3
     */
    private static final String jeditOjanenMercenary = "Jedit Ojanen, Mercenary";

    @Test
    public void testSecludedCourtyard() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, secludedCourtyard, 2);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 2);
        addCard(Zone.BATTLEFIELD, playerA, wallOfFire);
        addCard(Zone.BATTLEFIELD, playerA, plagueBoiler);
        addCard(Zone.HAND, playerA, opt);
        addCard(Zone.HAND, playerA, balduvianBears);

        setChoice(playerA, "Bear"); // Secluded Courtyard
        setChoice(playerA, "Wall"); // Secluded Courtyard

        checkPlayableAbility("Can't cast Opt", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast " + opt, false);
        checkPlayableAbility("Can cast Balduvian Bears", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast " + balduvianBears, true);
        checkPlayableAbility("Can activate Wall of Fire", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "{R}: {this} gets", true);
        checkPlayableAbility("Can't activate Plague Boiler", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "{1}{B}{G}: Put a plague counter", false);

         setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
         execute();
    }

    @Test
    public void testSecludedCourtyardTrigger() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Plains");
        addCard(Zone.HAND, playerA, jeditOjanenMercenary);
        addCard(Zone.BATTLEFIELD, playerA, secludedCourtyard);

        setChoice(playerA, "Cat"); // Secluded Courtyard

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {W}");
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {U}");
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {U}");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, jeditOjanenMercenary);
        setChoice(playerA, true);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, jeditOjanenMercenary, 1);
        assertPermanentCount(playerA, "Cat Warrior", 0); // should not be able to pay for trigger with secluded courtyard mana
    }
}