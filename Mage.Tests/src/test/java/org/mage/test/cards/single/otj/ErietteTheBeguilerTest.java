package org.mage.test.cards.single.otj;

import mage.abilities.keyword.FlyingAbility;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class ErietteTheBeguilerTest extends CardTestPlayerBase {

    /*
    Eriette, the Beguiler
    {1}{W}{U}{B}
    Legendary Creature - Human Warlock
    Lifelink
    Whenever an Aura you control becomes attached to a nonland permanent an opponent controls with mana value less than or equal to that Aura's mana value, gain control of that permanent for as long as that Aura is attached to it.
    4/4
    */
    private static final String erietteTheBeguiler = "Eriette, the Beguiler";

    /*
    Bear Cub
    {1}{G}
    Creature - Bear
    
    2/2
    */
    private static final String bearCub = "Bear Cub";

    /*
    Arcane Flight
    {U}
    Enchantment - Aura
    Enchant creature
    Enchanted creature gets +1/+1 and has flying
    */
    private static final String arcaneFlight = "Arcane Flight";

    /*
    Aether Tunnel
    {1}{U}
    Enchantment - Aura
    Enchant creature
    Enchanted creature gets +1/+0 and can't be blocked.
    */
    private static final String aetherTunnel = "Aether Tunnel";


    @Test
    public void testErietteTheBeguilerLessThanMV() {
        addCard(Zone.BATTLEFIELD, playerA, erietteTheBeguiler);
        addCard(Zone.BATTLEFIELD, playerB, bearCub);
        addCard(Zone.HAND, playerA, arcaneFlight);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, arcaneFlight, bearCub);


        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, bearCub, 0);
        assertPermanentCount(playerB, bearCub, 1);
        assertPowerToughness(playerB, bearCub, 3, 3);
        assertAbility(playerB, bearCub, FlyingAbility.getInstance(), true);
    }

    @Test
    public void testErietteTheBeguilerEqualToMV() {
        addCard(Zone.BATTLEFIELD, playerA, erietteTheBeguiler);
        addCard(Zone.BATTLEFIELD, playerB, bearCub);
        addCard(Zone.HAND, playerA, aetherTunnel);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 1);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, aetherTunnel, bearCub);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, bearCub, 1);
        assertPermanentCount(playerB, bearCub, 0);
        assertPowerToughness(playerA, bearCub, 3, 2);
    }
}