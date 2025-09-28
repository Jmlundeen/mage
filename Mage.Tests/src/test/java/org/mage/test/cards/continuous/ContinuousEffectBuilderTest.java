package org.mage.test.cards.continuous;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class ContinuousEffectBuilderTest extends CardTestPlayerBase {

    /*
    A Realm Reborn
    {4}{G}{G}
    Enchantment
    Other permanents you control have "{T}: Add one mana of any color."
    */
    private static final String aRealmReborn = "A Realm Reborn";

    /*
    Balduvian Bears
    {1}{G}
    Creature - Bear

    2/2
    */
    private static final String balduvianBears = "Balduvian Bears";

    @Test
    public void testGainOneAbility() {
        addCard(Zone.BATTLEFIELD, playerA, aRealmReborn);
        addCard(Zone.BATTLEFIELD, playerA, balduvianBears);

        checkPlayableAbility("Bear has mana ability", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                "{T}: Add one mana of any color.", true);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }
}
