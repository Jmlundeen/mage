package org.mage.test.cards.single.acr;

import mage.abilities.keyword.MenaceAbility;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class BrotherhoodHeadquartersTest extends CardTestPlayerBase {

    /**
    * {@link mage.cards.b.BrotherhoodHeadquarters Brotherhood Headquarters}
    * <br>
    * 
    * <br>
    * Land
    * <br>
    * {T}: Add {C}.
    * {T}: Add one mana of any color. Spend this mana only to cast an Assassin spell or a spell that has freerunning, or to activate an ability of an Assassin source.
    */
    private static final String brotherhoodHeadquarters = "Brotherhood Headquarters";


    /**
    * {@link mage.cards.e.EvieFrye Evie Frye}
    * <br>
    * {1}{U}
    * <br>
    * Legendary Creature — Human Assassin
    * <br>
    * Partner with Jacob Frye (When this creature enters, target player may put Jacob into their hand from their library, then shuffle.)
    * {1}, {T}: Draw a card, then discard a card. When you discard a creature card this way, target creature you control can't be blocked this turn.
    * <br>
    * 2/1
    */
    private static final String evieFrye = "Evie Frye";


    /**
    * {@link mage.cards.e.EagleVision Eagle Vision}
    * <br>
    * {4}{U}
    * <br>
    * Sorcery
    * <br>
    * Freerunning {1}{U} (You may cast this spell for its freerunning cost if you dealt combat damage to a player this turn with an Assassin or commander.)
    * Draw three cards.
    */
    private static final String eagleVision = "Eagle Vision";


    /**
     * {@link mage.cards.c.CinderingCutthroat Cindering Cutthroat}
     * <br>
     * {2}{B/R}
     * <br>
     * Creature -- Lizard Assassin
     * <br>
     * This creature enters with a +1/+1 counter on it if an opponent lost life this turn.
     {1}{B/R}: This creature gains menace until end of turn. (It can't be blocked except by two or more creatures.)
     * <br>
     * 3/2
     */
    private static final String cinderingCutthroat = "Cindering Cutthroat";


    /**
    * {@link mage.cards.g.GreenbeltGuardian Greenbelt Guardian}
    * <br>
    * {1}{G}
    * <br>
    * Creature — Elf Ranger
    * <br>
    * {G}: Target creature gains trample until end of turn.
    * Exhaust -- {3}{G}: Put three +1/+1 counters on this creature. (Activate each exhaust ability only once.)
    * <br>
    * 2/2
    */
    private static final String greenbeltGuardian = "Greenbelt Guardian";


    @Test
    public void testBrotherhoodHeadquartersAssassinSpell() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, brotherhoodHeadquarters);
        addCard(Zone.HAND, playerA, evieFrye);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, evieFrye);
        setChoice(playerA, "Blue");
        addTarget(playerA, playerA);
        setChoice(playerA, false);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, evieFrye, 1);
    }

    @Test
    public void testBrotherhoodHeadquartersFreerunningSpell() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, brotherhoodHeadquarters);
        addCard(Zone.BATTLEFIELD, playerA, evieFrye);
        addCard(Zone.BATTLEFIELD, playerA, "Forest");
        addCard(Zone.HAND, playerA, eagleVision);

        attack(1, playerA, evieFrye);

        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, eagleVision);
        setChoice(playerA, "Cast with Freerunning");
        setChoice(playerA, "Blue");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, 3);
        assertGraveyardCount(playerA, eagleVision, 1);
        assertLife(playerB, 20 - 2);
    }

    @Test
    public void testBrotherhoodHeadquartersNonAssassinSpell() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, brotherhoodHeadquarters);
        addCard(Zone.BATTLEFIELD, playerA, "Island");
        addCard(Zone.HAND, playerA, greenbeltGuardian);

        checkPlayableAbility("Can't cast non-Assassin spell", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast " + greenbeltGuardian, false);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }

    @Test
    public void testBrotherhoodHeadquartersActivateAssassinAbility() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, brotherhoodHeadquarters);
        addCard(Zone.BATTLEFIELD, playerA, "Forest");
        addCard(Zone.BATTLEFIELD, playerA, cinderingCutthroat);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{1}{B/R}: {this}");
        setChoice(playerA, "Black");

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertAbilityCount(playerA, cinderingCutthroat, MenaceAbility.class, 1);
    }
}