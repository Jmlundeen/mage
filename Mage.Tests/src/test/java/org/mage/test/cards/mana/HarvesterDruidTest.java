package org.mage.test.cards.mana;

import mage.abilities.mana.ManaOptions;
import mage.constants.ManaType;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author LevelX2, JayDi85
 */
public class HarvesterDruidTest extends CardTestPlayerBase {

    @Test
    public void testOneInstance() {
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 1);

        // {T}: Add one mana of any color that a land you control could produce.
        addCard(Zone.BATTLEFIELD, playerA, "Harvester Druid", 1);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        ManaOptions options = playerA.getAvailableManaTest(currentGame);
        Assert.assertEquals(3, options.size());
        options.canProduce(ManaType.RED, 2);
        options.canProduce(ManaType.BLUE, 2);
    }

    @Test
    public void testTwoInstances() {
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 1);

        // {T}: Add one mana of any color that a land you control could produce.
        addCard(Zone.BATTLEFIELD, playerA, "Harvester Druid", 2);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        ManaOptions options = playerA.getAvailableManaTest(currentGame);
        Assert.assertEquals(4, options.size());
        options.canProduce(ManaType.RED, 3);
        options.canProduce(ManaType.BLUE, 3);
    }
}
