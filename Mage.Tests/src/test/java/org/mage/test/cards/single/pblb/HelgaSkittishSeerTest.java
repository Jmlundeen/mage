package org.mage.test.cards.single.pblb;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class HelgaSkittishSeerTest extends CardTestPlayerBase {

    /**
    * {@link mage.cards.h.HelgaSkittishSeer Helga, Skittish Seer}
    * <br>
    * {G}{W}{U}
    * <br>
    * Legendary Creature — Frog Druid
    * <br>
    * Whenever you cast a creature spell with mana value 4 or greater, you draw a card, gain 1 life, and put a +1/+1 counter on Helga.
    * {T}: Add X mana of any one color, where X is Helga's power. Spend this mana only to cast creature spells with mana value 4 or greater or creature spells with {X} in their mana costs.
    * <br>
    * 1/3
    */
    private static final String helgaSkittishSeer = "Helga, Skittish Seer";

    /**
     * {@link mage.cards.b.Broodlord Broodlord}
     * <br>
     * {X}{3}{G}
     * <br>
     * Creature -- Tyranid
     * <br>
     * Ravenous (This creature enters with X +1/+1 counters on it. If X is 5 or more, draw a card when it enters.)
     Brood Telepathy -- When this creature enters, distribute X +1/+1 counters among any number of other target creatures you control.
     * <br>
     * 3/3
     */
    private static final String broodlord = "Broodlord";


    /**
    * {@link mage.cards.s.ScavengerRegent}
    * <br>
    * Scavenger Regent
    * <br>
    * {3}{B}
    * <br>
    * Creature — Dragon
    * <br>
    * Flying
    * Ward--Discard a card.
    * <br>
    * 4/4
    */
    private static final String scavengerRegent = "Scavenger Regent";
    /**
    * {@link mage.cards.s.ScavengerRegent}
    * <br>
    * Exude Toxin
    * <br>
    * {X}{B}{B}
    * <br>
    * Sorcery — Omen
    * <br>
    * Each non-Dragon creature gets -X/-X until end of turn. (Then shuffle this card into its owner's library.)
    */
    private static final String exudeToxin = "Exude Toxin";

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
    public void testHelgaSkittishSeer() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, helgaSkittishSeer);
        addCard(Zone.HAND, playerA, broodlord);
        addCard(Zone.HAND, playerA, scavengerRegent);
        addCard(Zone.HAND, playerA, balduvianBears);

        addCounters(1, PhaseStep.PRECOMBAT_MAIN, playerA, helgaSkittishSeer, CounterType.P1P1, 4);

        checkPlayableAbility("Can cast Broodlord", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast " + broodlord, true);
        checkPlayableAbility("Can cast Scavenger Regent", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast " + scavengerRegent, true);
        checkPlayableAbility("Can't cast Balduvian Bears", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast " + balduvianBears, false);
        checkPlayableAbility("Can't cast Exude Toxin", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast " + exudeToxin, false);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, broodlord);
        setChoice(playerA, "X=1");
        setChoice(playerA, "Green");
        addTargetAmount(playerA, helgaSkittishSeer, 1);

        setChoice(playerA, "Black");
        castSpell(3, PhaseStep.PRECOMBAT_MAIN, playerA, scavengerRegent);

        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, helgaSkittishSeer, 1);
        assertCounterCount(playerA, helgaSkittishSeer, CounterType.P1P1, 4 + 3);
        assertPermanentCount(playerA, scavengerRegent, 1);
    }
}