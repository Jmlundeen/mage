package org.mage.test.cards.single.uds;

import mage.abilities.keyword.ProtectionAbility;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class MaskOfLawAndGraceTest extends CardTestPlayerBase {

    /*
    Mask of Law and Grace
    {W}
    Enchantment - Aura
    Enchant creature
    Enchanted creature has protection from black and from red.
    */
    private static final String maskOfLawAndGrace = "Mask of Law and Grace";

    /*
    Jarad, Golgari Lich Lord
    {B}{B}{G}{G}
    Legendary Creature - Zombie Elf
    Jarad, Golgari Lich Lord gets +1/+1 for each creature card in your graveyard.
    {1}{B}{G}, Sacrifice another creature: Each opponent loses life equal to the sacrificed creature's power.
    Sacrifice a Swamp and a Forest: Return Jarad from your graveyard to your hand.
    2/2
    */
    private static final String jaradGolgariLichLord = "Jarad, Golgari Lich Lord";

    /*
    Balduvian Bears
    {1}{G}
    Creature - Bear
    
    2/2
    */
    private static final String balduvianBears = "Balduvian Bears";

    @Test
    public void testMaskOfLawAndGrace() {
        addCard(Zone.BATTLEFIELD, playerA, balduvianBears + "@bearA");
        addCard(Zone.BATTLEFIELD, playerB, jaradGolgariLichLord);
        addCard(Zone.BATTLEFIELD, playerA, "Plains");
        addCard(Zone.HAND, playerA, maskOfLawAndGrace);
        addCard(Zone.BATTLEFIELD, playerB, balduvianBears);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, maskOfLawAndGrace, "@bearA");

        checkAbility("bear has protection", 1, PhaseStep.BEGIN_COMBAT, playerA, balduvianBears, ProtectionAbility.class, true);
        attack(1, playerA, balduvianBears);
        block(1, playerB, jaradGolgariLichLord, balduvianBears);
        block(1, playerB, balduvianBears, balduvianBears);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_COMBAT);
        execute();

        assertPermanentCount(playerB, jaradGolgariLichLord, 1);
        assertGraveyardCount(playerA, balduvianBears, 1);
        assertGraveyardCount(playerB, balduvianBears, 1);
    }
}