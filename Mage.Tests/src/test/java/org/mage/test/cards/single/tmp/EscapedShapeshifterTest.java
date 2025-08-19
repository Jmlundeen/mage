package org.mage.test.cards.single.tmp;

import mage.ObjectColor;
import mage.abilities.keyword.FirstStrikeAbility;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.ProtectionAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;


public class EscapedShapeshifterTest extends CardTestPlayerBase {


    /*
    Escaped Shapeshifter
    {3}{U}{U}
    Creature — Shapeshifter

    As long as an opponent controls a creature with flying not named Escaped Shapeshifter, this creature has flying.
    The same is true for first strike, trample, and protection from any color.

    3/4
     */
    private static final String escapedShapeshifter = "Escaped Shapeshifter";
    /*
    kroma, Angel of Wrath
    {5}{W}{W}{W}
    Legendary Creature — Angel

    Flying, first strike, vigilance, trample, haste, protection from black and from red
     */
    private static final String akromaAngelOfWrath = "Akroma, Angel of Wrath";

    @Test
    public void testEscapedShapeshifter() {
        setStrictChooseMode(true);

        addCard(Zone.BATTLEFIELD, playerA, escapedShapeshifter);
        addCard(Zone.BATTLEFIELD, playerB, akromaAngelOfWrath);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertAbilities(playerA, escapedShapeshifter, Arrays.asList(
                FlyingAbility.getInstance(),
                FirstStrikeAbility.getInstance(),
                TrampleAbility.getInstance()
        ));
        assertTrue("Escaped Shapeshifter should have protection ability", getPermanent(escapedShapeshifter)
                .getAbilities(currentGame)
                .stream()
                .anyMatch(ability -> ability instanceof ProtectionAbility &&
                        ((ProtectionAbility) ability).getFromColor().equals(new ObjectColor("BR"))));
    }
}
