package org.mage.test.cards.single.spg;

import mage.constants.ManaType;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class BloomTenderTest extends CardTestPlayerBase {

    /**
    * {@link mage.cards.b.BloomTender Bloom Tender}
    * <br>
    * {1}{G}
    * <br>
    * Creature — Elf Druid
    * <br>
    * {T}: For each color among permanents you control, add one mana of that color.
    * <br>
    * 1/1
    */
    private static final String bloomTender = "Bloom Tender";


    /**
    * {@link mage.cards.s.SteadfastGuard Steadfast Guard}
    * <br>
    * {W}{W}
    * <br>
    * Creature — Human Rebel
    * <br>
    * Vigilance (Attacking doesn't cause this creature to tap.)
    * <br>
    * 2/2
    */
    private static final String steadfastGuard = "Steadfast Guard";


    /**
    * {@link mage.cards.s.SpinelessThug Spineless Thug}
    * <br>
    * {1}{B}
    * <br>
    * Creature — Phyrexian Zombie Mercenary
    * <br>
    * This creature can't block.
    * <br>
    * 2/2
    */
    private static final String spinelessThug = "Spineless Thug";


    /**
    * {@link mage.cards.g.GoblinEliteInfantry Goblin Elite Infantry}
    * <br>
    * {1}{R}
    * <br>
    * Creature — Goblin Warrior
    * <br>
    * Whenever this creature blocks or becomes blocked, it gets -1/-1 until end of turn.
    * <br>
    * 2/2
    */
    private static final String goblinEliteInfantry = "Goblin Elite Infantry";


    /**
    * {@link mage.cards.c.CoastalPiracy Coastal Piracy}
    * <br>
    * {2}{U}{U}
    * <br>
    * Enchantment
    * <br>
    * Whenever a creature you control deals combat damage to an opponent, you may draw a card.
    */
    private static final String coastalPiracy = "Coastal Piracy";


    /**
    * {@link mage.cards.s.SundialOfTheInfinite Sundial of the Infinite}
    * <br>
    * {2}
    * <br>
    * Artifact
    * <br>
    * {1}, {T}: End the turn. Activate only during your turn. (Exile all spells and abilities from the stack. Discard down to your maximum hand size. Damage wears off, and "this turn" and "until end of turn" effects end.)
    */
    private static final String sundialOfTheInfinite = "Sundial of the Infinite";

    /**
     * {@link mage.cards.u.Upwelling Upwelling}
     * <br>
     * {3}{G}
     * <br>
     * Enchantment
     * <br>
     * Players don't lose unspent mana as steps and phases end.
     */
    private static final String upwelling = "Upwelling";

    /**
     * {@link mage.cards.j.JodahTheUnifier Jodah, the Unifier}
     * <br>
     * {W}{U}{B}{R}{G}
     * <br>
     * Legendary Creature -- Human Wizard
     * <br>
     * Legendary creatures you control get +X/+X, where X is the number of legendary creatures you control.
     Whenever you cast a legendary spell from your hand, exile cards from the top of your library until you exile a legendary nonland card with lesser mana value. You may cast that card without paying its mana cost. Put the rest on the bottom of your library in a random order.
     * <br>
     * 5/5
     */
    private static final String jodahTheUnifier = "Jodah, the Unifier";


    @Test
    public void testBloomTender() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, bloomTender);
        addCard(Zone.BATTLEFIELD, playerA, steadfastGuard);
        addCard(Zone.BATTLEFIELD, playerA, spinelessThug, 2);
        addCard(Zone.BATTLEFIELD, playerA, goblinEliteInfantry);
        addCard(Zone.BATTLEFIELD, playerA, coastalPiracy, 2);
        addCard(Zone.BATTLEFIELD, playerA, sundialOfTheInfinite);
        addCard(Zone.BATTLEFIELD, playerA, upwelling);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: For each color among permanents you control, add one mana of that color.");
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertTapped(bloomTender, true);
        assertManaPool(playerA, ManaType.WHITE, 1);
        assertManaPool(playerA, ManaType.BLUE, 1);
        assertManaPool(playerA, ManaType.BLACK, 1);
        assertManaPool(playerA, ManaType.RED, 1);
        assertManaPool(playerA, ManaType.GREEN, 1);
    }

    @Test
    public void testBloomTenderAllColorsPermanent() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, bloomTender);
        addCard(Zone.BATTLEFIELD, playerA, steadfastGuard);
        addCard(Zone.BATTLEFIELD, playerA, jodahTheUnifier);
        addCard(Zone.BATTLEFIELD, playerA, upwelling);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: For each color among permanents you control, add one mana of that color.");
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertTapped(bloomTender, true);
        assertManaPool(playerA, ManaType.WHITE, 1);
        assertManaPool(playerA, ManaType.BLUE, 1);
        assertManaPool(playerA, ManaType.BLACK, 1);
        assertManaPool(playerA, ManaType.RED, 1);
        assertManaPool(playerA, ManaType.GREEN, 1);
    }
}