package org.mage.test.cards.single.cmm;

import mage.abilities.common.TurnFaceUpAbility;
import mage.constants.*;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author Jmlundeen
 */
public class YedoraGraveGardenerTest extends CardTestPlayerBase {

    /*
    Yedora, Grave Gardener
    {4}{G}
    Legendary Creature - Treefolk Druid
    Whenever another nontoken creature you control dies, you may return it to the battlefield face down under its owner's control. It's a Forest land.
    5/5
    */
    private static final String yedoraGraveGardener = "Yedora, Grave Gardener";

    /*
    Den Protector
    {1}{G}
    Creature - Human Warrior
    Creatures with power less than Den Protector's power can't block it.
    Megamorph {1}{G} <i>(You may cast this card face down as a 2/2 creature for {3}. Turn it face up any time for its megamorph cost and put a +1/+1 counter on it.)</i>
    When Den Protector is turned face up, return target card from your graveyard to your hand.
    2/1
    */
    private static final String denProtector = "Den Protector";

    /*
    Lightning Bolt
    {R}
    Instant
    Lightning Bolt deals 3 damage to any target.
    */
    private static final String lightningBolt = "Lightning Bolt";

    /*
    Boltbender
    {3}{R}
    Creature - Goblin Wizard
    Disguise {1}{R}
    When Boltbender is turned face up, you may choose new targets for any number of other spells and/or abilities.
    4/2
    */
    private static final String boltbender = "Boltbender";

    @Test
    public void testYedoraGraveGardenerMorph() {
        addCard(Zone.BATTLEFIELD, playerA, yedoraGraveGardener);
        addCard(Zone.BATTLEFIELD, playerA, denProtector);
        addCard(Zone.HAND, playerA, lightningBolt);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, lightningBolt, denProtector);
        setChoice(playerA, true);
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertType(EmptyNames.FACE_DOWN_CREATURE.getTestCommand(), CardType.LAND, SubType.FOREST);
        assertNotType(EmptyNames.FACE_DOWN_CREATURE.getTestCommand(), CardType.CREATURE);
        assertAbilityCount(playerA, EmptyNames.FACE_DOWN_CREATURE.getTestCommand(), TurnFaceUpAbility.class, 1);
    }

    @Test
    public void testYedoraDisguise() {
        addCard(Zone.BATTLEFIELD, playerA, yedoraGraveGardener);
        addCard(Zone.BATTLEFIELD, playerA, boltbender);
        addCard(Zone.HAND, playerA, lightningBolt);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, lightningBolt, boltbender);
        setChoice(playerA, true);
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertType(EmptyNames.FACE_DOWN_CREATURE.getTestCommand(), CardType.LAND, SubType.FOREST);
        assertNotType(EmptyNames.FACE_DOWN_CREATURE.getTestCommand(), CardType.CREATURE);
        assertAbilityCount(playerA, EmptyNames.FACE_DOWN_CREATURE.getTestCommand(), TurnFaceUpAbility.class, 1);
    }
}