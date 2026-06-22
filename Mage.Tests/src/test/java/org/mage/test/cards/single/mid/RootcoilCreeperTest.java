package org.mage.test.cards.single.mid;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class RootcoilCreeperTest extends CardTestPlayerBase {

    /**
    * {@link mage.cards.r.RootcoilCreeper Rootcoil Creeper}
    * <br>
    * {G}{U}
    * <br>
    * Creature — Plant Horror
    * <br>
    * {T}: Add one mana of any color.
    * {T}: Add two mana of any one color. Spend this mana only to cast spells from your graveyard.
    * {G}{U}, {T}, Exile this creature: Return target card with flashback you own from exile to your hand.
    * <br>
    * 2/2
    */
    private static final String rootcoilCreeper = "Rootcoil Creeper";

    /**
    * {@link mage.cards.t.TheirNumberIsLegion Their Number Is Legion}
    * <br>
    * {X}{B}{B}{B}{B}
    * <br>
    * Sorcery
    * <br>
    * Create X tapped 2/2 black Necron Warrior artifact creature tokens, then you gain life equal to the number of artifacts you control. Exile Their Number Is Legion.
    * You may cast this card from your graveyard.
    */
    private static final String theirNumberIsLegion = "Their Number Is Legion";

    /**
     * {@link mage.cards.p.PastInFlames Past in Flames}
     * <br>
     * {3}{R}
     * <br>
     * Sorcery
     * <br>
     * Each instant and sorcery card in your graveyard gains flashback until end of turn. The flashback cost is equal to its mana cost.
     Flashback {4}{R} (You may cast this card from your graveyard for its flashback cost. Then exile it.)
     */
    private static final String pastInFlames = "Past in Flames";


    @Test
    public void testRootcoilCreeperFlashBack() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, rootcoilCreeper);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 3);
        addCard(Zone.GRAVEYARD, playerA, pastInFlames);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Flashback {4}{R}");
        setChoice(playerA, "Red");

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertExileCount(playerA, pastInFlames, 1);
    }

    @Test
    public void testRootcoilCreeperGraveyardSpell() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, rootcoilCreeper);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 4);
        addCard(Zone.GRAVEYARD, playerA, theirNumberIsLegion);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, theirNumberIsLegion);
        setChoice(playerA, "X=2");
        setChoice(playerA, "Black");

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertExileCount(playerA, theirNumberIsLegion, 1);
        assertPermanentCount(playerA, "Necron Warrior Token", 2);
        assertLife(playerA, 20 + 2);
    }

    @Test
    public void testRootcoilCreeperHandSpell() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, rootcoilCreeper);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 2);
        addCard(Zone.HAND, playerA, theirNumberIsLegion);

        checkPlayableAbility("Cannot cast legion from hand", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast Their Number Is Legion", false);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }
}