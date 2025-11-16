package org.mage.test.cards.single.khc;

import mage.abilities.keyword.ForetellAbility;
import mage.cards.Card;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link mage.cards.e.EtherealValkyrie Ethereal Valkyrie}
 * <p>
 * Whenever Ethereal Valkyrie enters the battlefield or attacks, draw a card, then exile a card from your hand face down.
 * It becomes foretold.
 * Its foretell cost is its mana cost reduced by {2}.
 * (On a later turn, you may cast it for its foretell cost, even if this creature has left the battlefield.)
 *
 * @author Alex-Vasile
 */
public class EtherealValkyrieTest extends CardTestPlayerBase {

    // {4}{W}{U}
    private static final String etherealValkyrie = "Ethereal Valkyrie";
    // Suspend 4—{U}
    // Target player draws three cards.
    private static final String ancestralVision = "Ancestral Vision";
    // {5}{R} Creature-Land MDFC
    private static final String akoumWarrior = "Akoum Warrior";
    private static final String akoumTeeth = "Akoum Teeth";
    // {3}{U}{U}-{1}{U} Creature-Creature MDFC
    private static final String alrund = "Alrund, God of the Cosmos";
    private static final String hakka = "Hakka, Whispering Raven";
    // MDFC with where both sides are lands
    private static final String blightclimbPathway = "Brightclimb Pathway";
    private static final String grimclimbPathway = "Grimclimb Pathway";
    // {3}
    // {T}: Add one mana of any color
    private static final String alloyMyr = "Alloy Myr";
    // Land
    private static final String exoticOrchard = "Exotic Orchard";
    // Creature with foretell {4}{R}
    private static final String doomskarTitan = "Doomskar Titan";

    /**
     * Test that a regular card is playable.
     */
    @Test
    public void testRegularCard() {
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 4);
        addCard(Zone.HAND, playerA, etherealValkyrie);
        addCard(Zone.HAND, playerA, alloyMyr); // The one to exile with ETB ability

        setStrictChooseMode(true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, etherealValkyrie);
        addTarget(playerA, alloyMyr);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
        assertExileCount(playerA, 1);

        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        activateAbility(3, PhaseStep.PRECOMBAT_MAIN, playerA, "Foretell");
        execute();
        assertExileCount(playerA, alloyMyr, 0);
        assertPermanentCount(playerA, alloyMyr, 1);
    }

    /**
     * Reported Bug: When you only have lands in hand the game enters a permanent rollback state.
     * https://github.com/magefree/mage/issues/9361
     */
    @Test
    public void testLand() {
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 4);
        addCard(Zone.HAND, playerA, etherealValkyrie);
        addCard(Zone.HAND, playerA, exoticOrchard);

        setStrictChooseMode(true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, etherealValkyrie);
        addTarget(playerA, exoticOrchard);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
        assertExileCount(playerA, 1);

        checkPlayableAbility("Can't fortell land", 1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Foretell", false);

        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }

    /**
     * MDFC cards where both sides are lands should be exiled, but not fortell-able.
     */
    @Test
    public void testMDFCDualLand() {
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 4);
        addCard(Zone.HAND, playerA, etherealValkyrie);

        addCard(Zone.HAND, playerA, blightclimbPathway);


        setStrictChooseMode(true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, etherealValkyrie);
        addTarget(playerA, blightclimbPathway);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
        assertExileCount(playerA, 1);
        checkPlayableAbility("Can't fortell land", 1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Foretell", false);
    }

    /**
     * MDFC cards where only one side is a land should let you fortell its non-land side.
     */
    @Test
    public void testMDFCNonLandLand() {
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 4);
        addCard(Zone.HAND, playerA, etherealValkyrie, 1);

        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 4);
        addCard(Zone.HAND, playerA, akoumWarrior);

        setStrictChooseMode(true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, etherealValkyrie);
        addTarget(playerA, akoumWarrior);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
        assertExileCount(playerA, 1);

        // TODO: Add functionality to test for this programmatically by changing assertAbilityCount
        showAvailableAbilities("Should only be 1 Foretell ability", 3, PhaseStep.PRECOMBAT_MAIN, playerA);
        activateAbility(3, PhaseStep.PRECOMBAT_MAIN, playerA, "Foretell {3}{R}");

        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();
        assertExileCount(playerA, 0);
        assertPermanentCount(playerA, akoumWarrior, 1);
    }

    /**
     * MDFC cards where only one side is a land should let you fortell its non-land side.
     */
    @Test
    public void testMDFCCreatureCreature() {
        addCard(Zone.BATTLEFIELD, playerA, "Tundra", 12);
        addCard(Zone.HAND, playerA, etherealValkyrie, 2);

        addCard(Zone.BATTLEFIELD, playerA, "Island", 5);
        addCard(Zone.HAND, playerA, alrund);

        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);
        addCard(Zone.HAND, playerA, alrund);

        setStrictChooseMode(true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, etherealValkyrie);
        addTarget(playerA, alrund);
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, etherealValkyrie);
        addTarget(playerA, alrund);

        setStopAt(1, PhaseStep.END_TURN);
        execute();
        assertExileCount(playerA, 2);

        activateAbility(3, PhaseStep.PRECOMBAT_MAIN, playerA, "Foretell {1}{U}{U}");
        waitStackResolved(3, PhaseStep.PRECOMBAT_MAIN);
        activateAbility(3, PhaseStep.PRECOMBAT_MAIN, playerA, "Foretell {U}");

        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();
        assertExileCount(playerA, 0);
        assertPermanentCount(playerA, alrund, 1);
        assertPermanentCount(playerA, hakka, 1);
    }

    /**
     * Test a Suspend card, which should not be playable from exile with foretell.
     */
    @Test
    public void testSuspendCard() {
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 4);
        addCard(Zone.HAND, playerA, etherealValkyrie);
        addCard(Zone.HAND, playerA, ancestralVision);

        setStrictChooseMode(true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, etherealValkyrie);
        addTarget(playerA, ancestralVision);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
        assertExileCount(playerA, 1);

        checkPlayableAbility("Can't fortell suspend-only card", 1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Foretell", false);

        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }

    @Test
    public void testCardWithForetell() {
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 4);
        addCard(Zone.HAND, playerA, etherealValkyrie);
        addCard(Zone.HAND, playerA, doomskarTitan);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, etherealValkyrie);
        addTarget(playerA, doomskarTitan);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertExileCount(playerA, 1);
        for (Card card : currentGame.getExile().getCardsOwned(currentGame, playerA.getId())) {
            if (card.getAbilities(currentGame).containsClass(ForetellAbility.class)) {
                assertEquals(2, card.getAbilities(currentGame).stream()
                        .filter(ability -> ability instanceof ForetellAbility)
                        .count(),
                        "The exiled Doomskar Titan should have 2 Foretell abilities"
                );
            }
        }
    }
}
