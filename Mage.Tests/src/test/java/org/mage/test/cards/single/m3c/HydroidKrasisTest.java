package org.mage.test.cards.single.m3c;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author anonymous
 */
public class HydroidKrasisTest extends CardTestPlayerBase {

    /*
    Hydroid Krasis
    {X}{G}{U}
    Creature - Jellyfish Hydra Beast
    When you cast this spell, you gain half X life and draw half X cards. Round down each time.
    Flying, trample
    Hydroid Krasis enters the battlefield with X +1/+1 counters on it.
    */
    private static final String hydroidKrasis = "Hydroid Krasis";

    /*
    Runadi, Behemoth Caller
    {2}{G}
    Legendary Creature - Cat Shaman
    Whenever you cast a creature spell with mana value 5 or greater, that creature enters the battlefield with X additional +1/+1 counters on it, where X is its mana value minus 4.
    Creature you control with three or more +1/+1 counters on them have haste.
    {T}: Add {G}.
    1/3
    */
    private static final String runadiBehemothCaller = "Runadi, Behemoth Caller";

    /*
    Loading Zone
    {3}{G}
    Enchantment
    If one or more counters would be put on a creature, Spacecraft, or Planet you control, twice that many of each of those kinds of counters are put on it instead.
    Warp {G}
    */
    private static final String loadingZone = "Loading Zone";

    /*
    Terrasymbiosis
    {2}{G}
    Enchantment
    Whenever you put one or more +1/+1 counters on a creature you control, you may draw that many cards. Do this only once each turn.
    */
    private static final String terrasymbiosis = "Terrasymbiosis";

    /*
    Arwen, Weaver of Hope
    {1}{G}{G}
    Legendary Creature - Elf Noble
    Each other creature you control enters the battlefield with a number of additional +1/+1 counters on it equal to Arwen, Weaver of Hope's toughness.
    2/1
    */
    private static final String arwenWeaverOfHope = "Arwen, Weaver of Hope";

    @Test
    public void testHydroidKrasis() {
        addCard(Zone.BATTLEFIELD, playerA, "Tropical Island", 32);
        addCard(Zone.BATTLEFIELD, playerA, runadiBehemothCaller);
        addCard(Zone.BATTLEFIELD, playerA, loadingZone);
        addCard(Zone.HAND, playerA, hydroidKrasis);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, hydroidKrasis);
        setChoiceAmount(playerA, 30);
        setChoice(playerA, "When you cast this spell");
        setChoice(playerA, hydroidKrasis);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertCounterCount(playerA, hydroidKrasis, CounterType.P1P1, (30 + 28) * 2);
    }
}