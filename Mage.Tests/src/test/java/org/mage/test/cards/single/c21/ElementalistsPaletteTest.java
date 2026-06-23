package org.mage.test.cards.single.c21;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class ElementalistsPaletteTest extends CardTestPlayerBase {

    /**
    * {@link mage.cards.e.ElementalistsPalette Elementalist's Palette}
    * <br>
    * {3}
    * <br>
    * Artifact
    * <br>
    * Whenever you cast a spell with {X} in its mana cost, put two charge counters on this artifact.
    * {T}: Add one mana of any color.
    * {T}: Add {C} for each charge counter on this artifact. Spend this mana only on costs that contain {X}.
    */
    private static final String elementalistsPalette = "Elementalist's Palette";


    /**
    * {@link mage.cards.c.ChaliceOfTheVoid Chalice of the Void}
    * <br>
    * {X}{X}
    * <br>
    * Artifact
    * <br>
    * This artifact enters with X charge counters on it.
    * Whenever a player casts a spell with mana value equal to the number of charge counters on this artifact, counter that spell.
    */
    private static final String chaliceOfTheVoid = "Chalice of the Void";

    /**
     * {@link mage.cards.u.UrzasChalice Urza's Chalice}
     * <br>
     * {1}
     * <br>
     * Artifact
     * <br>
     * Whenever a player casts an artifact spell, you may pay {1}. If you do, you gain 1 life.
     */
    private static final String urzasChalice = "Urza's Chalice";


    @Test
    public void testElementalistsPalette() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, elementalistsPalette);
        addCard(Zone.HAND, playerA, chaliceOfTheVoid);
        addCard(Zone.HAND, playerA, urzasChalice);

        addCounters(1, PhaseStep.PRECOMBAT_MAIN, playerA, elementalistsPalette, CounterType.CHARGE, 2);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {C}");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);

        checkManaPool("Two colorless in pool", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "C", 2);
        checkPlayableAbility("Can't cast Urza's Chalice", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast " + urzasChalice, false);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, chaliceOfTheVoid);
        setChoice(playerA, "X=1");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertCounterCount(playerA, elementalistsPalette, CounterType.CHARGE, 2 + 2); // two additional from casting chalice
        assertCounterCount(playerA, chaliceOfTheVoid, CounterType.CHARGE, 1);
    }
}