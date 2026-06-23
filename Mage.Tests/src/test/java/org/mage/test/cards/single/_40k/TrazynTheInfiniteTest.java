package org.mage.test.cards.single._40k;

import mage.abilities.ActivatedAbility;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class TrazynTheInfiniteTest extends CardTestPlayerBase {

    /**
    * {@link mage.cards.t.TrazynTheInfinite Trazyn the Infinite}
    * <br>
    * {4}{B}{B}
    * <br>
    * Legendary Artifact Creature — Necron
    * <br>
    * Deathtouch
    * Prismatic Gallery -- As long as Trazyn is on the battlefield, it has all activated abilities of all artifact cards in your graveyard.
    * <br>
    * 4/6
    */
    private static final String trazynTheInfinite = "Trazyn the Infinite";


    /**
    * {@link mage.cards.t.TormodsCrypt Tormod's Crypt}
    * <br>
    * {0}
    * <br>
    * Artifact
    * <br>
    * {T}, Sacrifice this artifact: Exile target player's graveyard.
    */
    private static final String tormodsCrypt = "Tormod's Crypt";


    @Test
    public void testTrazynTheInfinite() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, trazynTheInfinite);
        addCard(Zone.GRAVEYARD, playerA, tormodsCrypt);
        addCard(Zone.BATTLEFIELD, playerB, trazynTheInfinite);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertAbilityCount(playerA, trazynTheInfinite, ActivatedAbility.class, 2); // spell ability + Tormod's Crypt ability
        assertAbilityCount(playerB, trazynTheInfinite, ActivatedAbility.class, 1); // only spell ability
    }
}