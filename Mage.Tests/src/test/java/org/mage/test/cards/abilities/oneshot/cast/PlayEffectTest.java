package org.mage.test.cards.abilities.oneshot.cast;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author Jmlundeen
 */
public class PlayEffectTest extends CardTestPlayerBase {

    /**
    * {@link mage.cards.c.Counterpoint Counterpoint}
    * <br>
    * {3}{U}{B}
    * <br>
    * Instant
    * <br>
    * Counter target spell. You may cast a creature, instant, sorcery, or planeswalker spell from your graveyard with mana value less than or equal to that spell's mana value without paying its mana cost.
    */
    private static final String counterpoint = "Counterpoint";


    /**
    * {@link mage.cards.m.MortalCombat Mortal Combat}
    * <br>
    * {2}{B}{B}
    * <br>
    * Enchantment
    * <br>
    * At the beginning of your upkeep, if twenty or more creature cards are in your graveyard, you win the game.
    */
    private static final String mortalCombat = "Mortal Combat";


    /**
    * {@link mage.cards.w.WrennAndSix Wrenn and Six}
    * <br>
    * {R}{G}
    * <br>
    * Legendary Planeswalker — Wrenn
    * <br>
    * +1: Return up to one target land card from your graveyard to your hand.
    * −1: Wrenn and Six deals 1 damage to any target.
    * −7: You get an emblem with "Instant and sorcery cards in your graveyard have retrace." (You may cast instant and sorcery cards from your graveyard by discarding a land card in addition to paying their other costs.)
    * <br>
    * Starting Loyalty: 3
    */
    private static final String wrennAndSix = "Wrenn and Six";


    /**
    * {@link mage.cards.a.AncestralRecall Ancestral Recall}
    * <br>
    * {U}
    * <br>
    * Instant
    * <br>
    * Target player draws three cards.
    */
    private static final String ancestralRecall = "Ancestral Recall";

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

    @Test
    public void testCounterpointCreature() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, "Underground Sea", 5);
        addCard(Zone.BATTLEFIELD, playerB, "Swamp", 4);
        addCard(Zone.GRAVEYARD, playerA, balduvianBears);
        addCard(Zone.GRAVEYARD, playerA, wrennAndSix);
        addCard(Zone.GRAVEYARD, playerA, ancestralRecall);
        addCard(Zone.GRAVEYARD, playerA, mortalCombat);
        addCard(Zone.HAND, playerA, counterpoint);
        addCard(Zone.HAND, playerB, mortalCombat);

        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, mortalCombat);
        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerA, counterpoint, mortalCombat, mortalCombat);
        setChoice(playerA, balduvianBears);
        setChoice(playerA, true);

        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, balduvianBears, 1);
        assertPermanentCount(playerB, mortalCombat, 0);
    }

    @Test
    public void testCounterpointPlaneswalker() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, "Underground Sea", 5);
        addCard(Zone.BATTLEFIELD, playerB, "Swamp", 4);
        addCard(Zone.GRAVEYARD, playerA, balduvianBears);
        addCard(Zone.GRAVEYARD, playerA, wrennAndSix);
        addCard(Zone.GRAVEYARD, playerA, ancestralRecall);
        addCard(Zone.GRAVEYARD, playerA, mortalCombat);
        addCard(Zone.HAND, playerA, counterpoint);
        addCard(Zone.HAND, playerB, mortalCombat);

        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, mortalCombat);
        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerA, counterpoint, mortalCombat, mortalCombat);
        setChoice(playerA, wrennAndSix);
        setChoice(playerA, true);

        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, wrennAndSix, 1);
        assertPermanentCount(playerB, mortalCombat, 0);
    }

    @Test
    public void testCounterpointInstant() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, "Underground Sea", 5);
        addCard(Zone.BATTLEFIELD, playerB, "Swamp", 4);
        addCard(Zone.GRAVEYARD, playerA, balduvianBears);
        addCard(Zone.GRAVEYARD, playerA, wrennAndSix);
        addCard(Zone.GRAVEYARD, playerA, ancestralRecall);
        addCard(Zone.GRAVEYARD, playerA, mortalCombat);
        addCard(Zone.HAND, playerA, counterpoint);
        addCard(Zone.HAND, playerB, mortalCombat);

        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, mortalCombat);
        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerA, counterpoint, mortalCombat, mortalCombat);
        setChoice(playerA, ancestralRecall);
        setChoice(playerA, true);
        addTarget(playerA, playerA);

        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertHandCount(playerA, 3);
        assertPermanentCount(playerB, mortalCombat, 0);
    }

    @Test
    public void testCounterpointEnchantment() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, "Underground Sea", 5);
        addCard(Zone.BATTLEFIELD, playerB, "Swamp", 4);
        addCard(Zone.GRAVEYARD, playerA, balduvianBears);
        addCard(Zone.GRAVEYARD, playerA, wrennAndSix);
        addCard(Zone.GRAVEYARD, playerA, ancestralRecall);
        addCard(Zone.GRAVEYARD, playerA, mortalCombat);
        addCard(Zone.HAND, playerA, counterpoint);
        addCard(Zone.HAND, playerB, mortalCombat);

        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, mortalCombat);
        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerA, counterpoint, mortalCombat, mortalCombat);
        setChoice(playerA, mortalCombat);
        addTarget(playerA, playerA);

        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        try {
            execute();
        } catch (AssertionError e) {
            assertTrue(e.getMessage().startsWith("Found wrong choice command (invalid target or miss skip command):\n" +
                    "Mortal Combat"));
        }
    }

    @Test
    public void testCounterpointReplay() {
        setStrictChooseMode(true);
        addCustomEffect_ReturnFromAnyToHand(playerA);
        addCard(Zone.BATTLEFIELD, playerA, "Underground Sea", 10);
        addCard(Zone.BATTLEFIELD, playerB, "Swamp", 8);
        addCard(Zone.GRAVEYARD, playerA, balduvianBears);
        addCard(Zone.GRAVEYARD, playerA, wrennAndSix);
        addCard(Zone.GRAVEYARD, playerA, ancestralRecall);
        addCard(Zone.GRAVEYARD, playerA, mortalCombat);
        addCard(Zone.HAND, playerA, counterpoint);
        addCard(Zone.HAND, playerB, mortalCombat, 2);

        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, mortalCombat);
        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerA, counterpoint, mortalCombat, mortalCombat);
        setChoice(playerA, balduvianBears);
        setChoice(playerA, true);
        waitStackResolved(2, PhaseStep.PRECOMBAT_MAIN, playerB, true);
        waitStackResolved(2, PhaseStep.PRECOMBAT_MAIN, playerB, true);
        waitStackResolved(2, PhaseStep.PRECOMBAT_MAIN, playerB, true);
        waitStackResolved(2, PhaseStep.PRECOMBAT_MAIN, playerA, true);
        waitStackResolved(2, PhaseStep.PRECOMBAT_MAIN, playerA, true);
        activateAbility(2, PhaseStep.PRECOMBAT_MAIN, playerA, "return from graveyard", counterpoint);
        waitStackResolved(2, PhaseStep.PRECOMBAT_MAIN, playerA, true);
        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, mortalCombat);
        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerA, counterpoint, mortalCombat, mortalCombat);
        setChoice(playerA, wrennAndSix);
        setChoice(playerA, true);

        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, balduvianBears, 1);
        assertPermanentCount(playerA, wrennAndSix, 1);
        assertPermanentCount(playerB, mortalCombat, 0);
        assertGraveyardCount(playerA, counterpoint, 1);
        assertGraveyardCount(playerB, mortalCombat, 2);
    }
}