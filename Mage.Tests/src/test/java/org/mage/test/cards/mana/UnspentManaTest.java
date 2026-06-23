package org.mage.test.cards.mana;

import mage.constants.ManaType;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class UnspentManaTest extends CardTestPlayerBase {

    /**
     * {@link mage.cards.o.OmnathLocusOfMana Omnath, Locus of Mana}
     * <br>
     * {2}{G}
     * <br>
     * Legendary Creature -- Elemental
     * <br>
     * You don't lose unspent green mana as steps and phases end.
     Omnath gets +1/+1 for each unspent green mana you have.
     * <br>
     * 1/1
     */
    private static final String omnathLocusOfMana = "Omnath, Locus of Mana";


    /**
     * {@link mage.cards.m.ManaReflection Mana Reflection}
     * <br>
     * {4}{G}{G}
     * <br>
     * Enchantment
     * <br>
     * If you tap a permanent for mana, it produces twice as much of that mana instead.
     */
    private static final String manaReflection = "Mana Reflection";


    /**
     * {@link mage.cards.g.GruulTurf Gruul Turf}
     * <br>
     *
     * <br>
     * Land
     * <br>
     * This land enters tapped.
     When this land enters, return a land you control to its owner's hand.
     {T}: Add {R}{G}.
     */
    private static final String gruulTurf = "Gruul Turf";


    @Test
    public void testUnspentMana() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, omnathLocusOfMana);
        addCard(Zone.BATTLEFIELD, playerA, manaReflection);
        addCard(Zone.BATTLEFIELD, playerA, gruulTurf);

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {R}{G}");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertManaPool(playerA, ManaType.GREEN, 2);
        assertManaPool(playerA, ManaType.RED, 0);
    }

}
