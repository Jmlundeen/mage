package org.mage.test.cards.flip;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class FlipCardsTest extends CardTestPlayerBase {

    /**
     * {@link mage.cards.j.JushiApprentice}
     * <br>
     * Jushi Apprentice
     * <br>
     * {1}{U}
     * <br>
     * Creature -- Human Wizard
     * <br>
     * {2}{U}, {T}: Draw a card. If you have nine or more cards in hand, flip this creature.
     * <br>
     * 1/2
     */
    private static final String jushiApprentice = "Jushi Apprentice";
    /**
     * {@link mage.cards.j.JushiApprentice}
     * <br>
     * Tomoya the Revealer
     * <br>
     * Legendary Creature -- Human Wizard
     * <br>
     * {3}{U}{U}, {T}: Target player draws X cards, where X is the number of cards in your hand.
     * <br>
     * 2/3
     */
    private static final String tomoyaTheRevealer = "Tomoya the Revealer";

    /**
     * {@link mage.cards.d.DimirDoppelganger}
     * <br>
     * Dimir Doppelganger
     * <br>
     * {1}{U}{B}
     * <br>
     * Creature -- Shapeshifter
     * <br>
     * {1}{U}{B}: Exile target creature card from a graveyard. This creature becomes a copy of that card, except it has this ability.
     * <br>
     * 0/2
     */
    private static final String dimirDoppelganger = "Dimir Doppelganger";
    /**
     * {@link mage.cards.n.NezumiShortfang}
     * <br>
     * Nezumi Shortfang
     * <br>
     * {1}{B}
     * <br>
     * Creature -- Rat Rogue
     * <br>
     * {1}{B}, {T}: Target opponent discards a card. Then if that player has no cards in hand, flip this creature.
     * <br>
     * 1/1
     */
    private static final String nezumiShortfang = "Nezumi Shortfang";
    /**
     * {@link mage.cards.n.NezumiShortfang}
     * <br>
     * Stabwhisker the Odious
     * <br>
     * Legendary Creature -- Rat Shaman
     * <br>
     * At the beginning of each opponent's upkeep, that player loses 1 life for each card fewer than three in their hand.
     * <br>
     * 3/3
     */
    private static final String stabwhiskerTheOdious = "Stabwhisker the Odious";
    /**
     * {@link mage.cards.h.HomuraHumanAscendant}
     * <br>
     * Homura, Human Ascendant
     * <br>
     * {4}{R}{R}
     * <br>
     * Legendary Creature -- Human Monk
     * <br>
     * Homura can't block.
     When Homura dies, return it to the battlefield flipped.
     * <br>
     * 4/4
     */
    private static final String homuraHumanAscendant = "Homura, Human Ascendant";
    /**
     * {@link mage.cards.h.HomuraHumanAscendant}
     * <br>
     * Homura's Essence
     * <br>
     * Legendary Enchantment
     * <br>
     * Creatures you control get +2/+2 and have flying and "{R}: This creature gets +1/+0 until end of turn."
     * <br>
     * /
     */
    private static final String homurasEssence = "Homura's Essence";

    /**
     * {@link mage.cards.s.SparkDouble}
     * <br>
     * Spark Double
     * <br>
     * {3}{U}
     * <br>
     * Creature -- Illusion
     * <br>
     * You may have this creature enter as a copy of a creature or planeswalker you control, except it enters with an additional +1/+1 counter on it if it's a creature, it enters with an additional loyalty counter on it if it's a planeswalker, and it isn't legendary.
     * <br>
     * 0/0
     */
    private static final String sparkDouble = "Spark Double";



    /**
     * Test that copying a flip card keeps its flip status. Copying a jushi apprentice with doppleganger
     * and then flipping should make the doppleganger become tomoya the revealer. Then copying the nezumi shortfang
     * should make the doppleganger become stabwhisker the odious.
     */
    @Test
    public void test_copy_keeps_flip_status() {
        addCard(Zone.GRAVEYARD, playerA, nezumiShortfang);
        addCard(Zone.GRAVEYARD, playerA, jushiApprentice);
        addCard(Zone.BATTLEFIELD, playerA, dimirDoppelganger);
        addCard(Zone.BATTLEFIELD, playerA, "Underground Sea", 20);
        addCard(Zone.HAND, playerA, "Mountain", 8);

        // Copy Jushi Apprentice
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{1}{U}{B}");
        addTarget(playerA, jushiApprentice);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPermanentCount("Doppelganger should be Jushi Apprentice", 1, PhaseStep.PRECOMBAT_MAIN, playerA, jushiApprentice, 1);

        // Flip Jushi Apprentice copy (has 9 cards in hand now)
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{2}{U}");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPermanentCount("Doppelganger should be Tomoya the Revealer", 1, PhaseStep.PRECOMBAT_MAIN, playerA, tomoyaTheRevealer, 1);

        // Copy Nezumi Shortfang
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{1}{U}{B}");
        addTarget(playerA, nezumiShortfang);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPermanentCount("Doppelganger should be stabwhisker", 1, PhaseStep.PRECOMBAT_MAIN, playerA, stabwhiskerTheOdious, 1);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }

    /**
     * Test that when a homura is copied, then the copy is destroyed, it will return flipped and
     * if the copy returns as a flipped card, it will be the flipped side (Homura's Essence) that returns.
     */
    @Test
    public void test_flip_copy_on_death() {
        addCard(Zone.BATTLEFIELD, playerA, homuraHumanAscendant);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 5);
        addCard(Zone.HAND, playerA, sparkDouble);

        addCustomEffect_TargetDestroy(playerA);

        // Copy Homura, Human Ascendant
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, sparkDouble);
        setChoice(playerA, true); // choose to copy
        setChoice(playerA, homuraHumanAscendant);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPermanentCount("There should be 2 Homura on battlefield", 1, PhaseStep.PRECOMBAT_MAIN, playerA, homuraHumanAscendant, 2);

        // Destroy copy of Homura
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "target destroy");
        addTarget(playerA, homuraHumanAscendant + "[only copy]");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        setChoice(playerA, true); // choose to copy
        setChoice(playerA, homuraHumanAscendant); // choose homura on return
        checkPermanentCount("There should be 1 Homura's Essence on battlefield", 1, PhaseStep.PRECOMBAT_MAIN, playerA, homurasEssence, 1);
        checkPermanentCount("There should be 1 Homura on battlefield", 1, PhaseStep.PRECOMBAT_MAIN, playerA, homuraHumanAscendant, 1);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPowerToughness(playerA, homuraHumanAscendant, 4 + 2, 4 + 2); // Homura's Essence gives +2/+2
    }
}
