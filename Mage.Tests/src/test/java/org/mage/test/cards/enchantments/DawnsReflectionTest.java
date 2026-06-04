package org.mage.test.cards.enchantments;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class DawnsReflectionTest extends CardTestPlayerBase {

    /**
     * {@link mage.cards.d.DawnsReflection Dawn's Reflection}
     * <br>
     * {3}{G}
     * <br>
     * Enchantment -- Aura
     * <br>
     * Enchant land
     Whenever enchanted land is tapped for mana, its controller adds an additional two mana in any combination of colors.
     */
    private static final String dawnsReflection = "Dawn's Reflection";

    @Test
    public void testEnchantedLandControllerGetsManaAndChoosesColors() {
        setStrictChooseMode(true);

        addCard(Zone.HAND, playerA, dawnsReflection);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 4);

        addCard(Zone.BATTLEFIELD, playerB, "Mountain");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, dawnsReflection, "Mountain");

        // W U B R G order for multi-amount mana choice.
        setChoiceAmount(playerB, 0, 0, 0, 0, 2);
        activateAbility(2, PhaseStep.PRECOMBAT_MAIN, playerB, "{T}: Add {R}");

        checkManaPool("player B gets extra green mana", 2, PhaseStep.PRECOMBAT_MAIN, playerB, "G", 2);
        checkManaPool("player B keeps land mana", 2, PhaseStep.PRECOMBAT_MAIN, playerB, "R", 1);
        checkManaPool("player A gets nothing", 2, PhaseStep.PRECOMBAT_MAIN, playerA, "G", 0);

        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }
}

