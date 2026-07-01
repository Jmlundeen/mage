package org.mage.test.cards.conditional;

import mage.abilities.keyword.FirstStrikeAbility;
import mage.abilities.keyword.HasteAbility;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author LevelX2
 */

public class ManaWasSpentToCastTest extends CardTestPlayerBase {

    /**
     * {@link mage.cards.t.TinStreetHooligan Tin Street Hooligan}
     * <br>
     * {1}{R}
     * <br>
     * Creature -- Goblin Rogue
     * <br>
     * When this creature enters, if {G} was spent to cast it, destroy target artifact.
     * <br>
     * 2/1
     */
    private static final String tinStreetHooligan = "Tin Street Hooligan";


    /**
     * {@link mage.cards.a.AbzanBanner Abzan Banner}
     * <br>
     * {3}
     * <br>
     * Artifact
     * <br>
     * {T}: Add {W}, {B}, or {G}.
     * {W}{B}{G}, {T}, Sacrifice this artifact: Draw a card.
     */
    private static final String abzanBanner = "Abzan Banner";


    /**
     * {@link mage.cards.l.LightningBolt Lightning Bolt}
     * <br>
     * {R}
     * <br>
     * Instant
     * <br>
     * Lightning Bolt deals 3 damage to any target.
     */
    private static final String lightningBolt = "Lightning Bolt";


    /**
     * {@link mage.cards.b.BorealDruid Boreal Druid}
     * <br>
     * {G}
     * <br>
     * Snow Creature -- Elf Druid
     * <br>
     * {T}: Add {C}.
     * <br>
     * 1/1
     */
    private static final String borealDruid = "Boreal Druid";


    /**
     * {@link mage.cards.s.SearchForGlory Search for Glory}
     * <br>
     * {2}{W}
     * <br>
     * Snow Sorcery
     * <br>
     * Search your library for a snow permanent card, a legendary card, or a Saga card, reveal it, put it into your hand, then shuffle. You gain 1 life for each {S} spent to cast this spell. ({S} is mana from a snow source.)
     */
    private static final String searchForGlory = "Search for Glory";


    /**
     * {@link mage.cards.v.VodalianArcanist Vodalian Arcanist}
     * <br>
     * {1}{U}
     * <br>
     * Creature -- Merfolk Wizard
     * <br>
     * {T}: Add {C}. Spend this mana only to cast an instant or sorcery spell.
     * <br>
     * 1/3
     */
    private static final String vodalianArcanist = "Vodalian Arcanist";


    /**
     * {@link mage.cards.r.RimefeatherOwl Rimefeather Owl}
     * <br>
     * {5}{U}{U}
     * <br>
     * Snow Creature -- Bird
     * <br>
     * Flying
     * Rimefeather Owl's power and toughness are each equal to the number of snow permanents on the battlefield.
     * {1}{S}: Put an ice counter on target permanent.
     * Permanents with ice counters on them are snow.
     * <br>
     *
     */
    private static final String rimefeatherOwl = "Rimefeather Owl";


    /**
     * {@link mage.cards.s.SilvercoatLion Silvercoat Lion}
     * <br>
     * {1}{W}
     * <br>
     * Creature -- Cat
     * <br>
     *
     * <br>
     * 2/2
     */
    private static final String silvercoatLion = "Silvercoat Lion";


    /**
     * {@link mage.cards.b.BergStrider Berg Strider}
     * <br>
     * {4}{U}
     * <br>
     * Snow Creature -- Giant Wizard
     * <br>
     * When this creature enters, tap target artifact or creature an opponent controls. If {S} was spent to cast this spell, that permanent doesn't untap during its controller's next untap step. ({S} is mana from a snow source.)
     * <br>
     * 4/4
     */
    private static final String bergStrider = "Berg Strider";


    /**
     * {@link mage.cards.j.JadedSellSword Jaded Sell-Sword}
     * <br>
     * {3}{R}
     * <br>
     * Creature -- Dragon Warrior
     * <br>
     * When this creature enters, if mana from a Treasure was spent to cast it, it gains first strike and haste until end of turn.
     * <br>
     * 4/3
     */
    private static final String jadedSellSword = "Jaded Sell-Sword";


    /**
     * {@link mage.cards.s.StrikeItRich Strike It Rich}
     * <br>
     * {R}
     * <br>
     * Sorcery
     * <br>
     * Create a Treasure token. (It's an artifact with "{T}, Sacrifice this token: Add one mana of any color.")
     * Flashback {2}{R} (You may cast this card from your graveyard for its flashback cost. Then exile it.)
     */
    private static final String strikeItRich = "Strike It Rich";


    /**
     * {@link mage.cards.v.VerazolTheSplitCurrent Verazol, the Split Current}
     * <br>
     * {X}{G}{U}
     * <br>
     * Legendary Creature -- Serpent
     * <br>
     * Verazol enters with a +1/+1 counter on it for each mana spent to cast it.
     * Whenever you cast a kicked spell, you may remove two +1/+1 counters from Verazol. If you do, copy that spell. You may choose new targets for the copy. (A copy of a permanent spell becomes a token.)
     * <br>
     * 0/0
     */
    private static final String verazolTheSplitCurrent = "Verazol, the Split Current";


    /**
     * {@link mage.cards.s.SphereOfResistance Sphere of Resistance}
     * <br>
     * {2}
     * <br>
     * Artifact
     * <br>
     * Spells cost {1} more to cast.
     */
    private static final String sphereOfResistance = "Sphere of Resistance";


    /**
     * {@link mage.cards.p.ProsshSkyraiderOfKher Prossh, Skyraider of Kher}
     * <br>
     * {3}{B}{R}{G}
     * <br>
     * Legendary Creature -- Dragon
     * <br>
     * When you cast this spell, create X 0/1 red Kobold creature tokens named Kobolds of Kher Keep, where X is the amount of mana spent to cast it.
     * Flying
     * Sacrifice another creature: Prossh gets +1/+0 until end of turn.
     * <br>
     * 5/5
     */
    private static final String prosshSkyraiderOfKher = "Prossh, Skyraider of Kher";


    /**
     * {@link mage.cards.k.KoboldsOfKherKeep Kobolds of Kher Keep}
     * <br>
     * {0}
     * <br>
     * Creature -- Kobold
     * <br>
     *
     * <br>
     * 0/1
     */
    private static final String koboldsOfKherKeep = "Kobolds of Kher Keep";


    /**
     * {@link mage.cards.p.PyreticRitual Pyretic Ritual}
     * <br>
     * {1}{R}
     * <br>
     * Instant
     * <br>
     * Add {R}{R}{R}.
     */
    private static final String pyreticRitual = "Pyretic Ritual";


    /**
     * {@link mage.cards.g.GrayOgre Gray Ogre}
     * <br>
     * {2}{R}
     * <br>
     * Creature -- Ogre
     * <br>
     *
     * <br>
     * 2/2
     */
    private static final String grayOgre = "Gray Ogre";


    /**
     * {@link mage.cards.i.IsochronScepter Isochron Scepter}
     * <br>
     * {2}
     * <br>
     * Artifact
     * <br>
     * Imprint -- When this artifact enters, you may exile an instant card with mana value 2 or less from your hand.
     * {2}, {T}: You may copy the exiled card. If you do, you may cast the copy without paying its mana cost.
     */
    private static final String isochronScepter = "Isochron Scepter";


    /**
     * {@link mage.cards.m.ManaDrain Mana Drain}
     * <br>
     * {U}{U}
     * <br>
     * Instant
     * <br>
     * Counter target spell. At the beginning of your next main phase, add an amount of {C} equal to that spell's mana value.
     */
    private static final String manaDrain = "Mana Drain";


    /**
     * {@link mage.cards.s.SliverConstruct Sliver Construct}
     * <br>
     * {3}
     * <br>
     * Artifact Creature -- Sliver Construct
     * <br>
     *
     * <br>
     * 2/2
     */
    private static final String sliverConstruct = "Sliver Construct";

    /**
     * {@link mage.cards.i.InduceParanoia Induce Paranoia}
     * <br>
     * {2}{U}{U}
     * <br>
     * Instant
     * <br>
     * Counter target spell. If {B} was spent to cast this spell, that spell's controller mills X cards, where X is the spell's mana value.
     */
    private static final String induceParanoia = "Induce Paranoia";


    /**
     * {@link mage.cards.b.BalduvianBears Balduvian Bears}
     * <br>
     * {1}{G}
     * <br>
     * Creature — Bear
     * <br>
     *
     * <br>
     * 2/2
     */
    private static final String balduvianBears = "Balduvian Bears";

    @Test
    public void testArtifactWillBeDestroyed() {
        // Tin Street Hooligan - Creature 2/1   {1}{R}
        // When Tin Street Hooligan enters the battlefield, if {G} was spent to cast Tin Street Hooligan, destroy target artifact.
        addCard(Zone.HAND, playerA, tinStreetHooligan);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 1);

        addCard(Zone.BATTLEFIELD, playerB, abzanBanner);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, tinStreetHooligan);
        addTarget(playerA, abzanBanner);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, tinStreetHooligan, 1);
        assertPermanentCount(playerB, abzanBanner, 0);
    }

    @Test
    public void testArtifactWontBeDestroyed() {
        // Tin Street Hooligan - Creature 2/1   {1}{R}
        // When Tin Street Hooligan enters the battlefield, if {G} was spent to cast Tin Street Hooligan, destroy target artifact.
        addCard(Zone.HAND, playerA, tinStreetHooligan);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 2);

        addCard(Zone.BATTLEFIELD, playerB, abzanBanner);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, tinStreetHooligan);
        // {G} was not spent, so no target is chosen

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, tinStreetHooligan, 1);
    }

    //ManaWasSpentCondition gives false negative after permanent leaves battlefield
    //Fixed by using MageObjectReference instead of UUID
    @Test
    public void testArtifactWillBeDestroyedAfterDeath() {
        // Tin Street Hooligan - Creature 2/1   {1}{R}
        // When Tin Street Hooligan enters the battlefield, if {G} was spent to cast Tin Street Hooligan, destroy target artifact.
        addCard(Zone.HAND, playerA, tinStreetHooligan);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 1);

        addCard(Zone.HAND, playerB, lightningBolt);
        addCard(Zone.BATTLEFIELD, playerB, abzanBanner);
        addCard(Zone.BATTLEFIELD, playerB, "Mountain", 1);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, tinStreetHooligan);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN, true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerB, lightningBolt, tinStreetHooligan);
        addTarget(playerA, abzanBanner);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, tinStreetHooligan, 0);
        assertPermanentCount(playerB, abzanBanner, 0);
        assertGraveyardCount(playerA, 1);
        assertGraveyardCount(playerB, 2);
    }

    @Test
    public void testSnowMana() {
        addCard(Zone.BATTLEFIELD, playerA, "Snow-Covered Plains");
        addCard(Zone.BATTLEFIELD, playerA, borealDruid);
        addCard(Zone.BATTLEFIELD, playerA, "Plains");
        addCard(Zone.HAND, playerA, searchForGlory);
        addCard(Zone.LIBRARY, playerA, "Snow-Covered Plains");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, searchForGlory);
        addTarget(playerA, "Snow-Covered Plains");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertGraveyardCount(playerA, searchForGlory, 1);
        assertLife(playerA, 22);
    }

    @Test
    public void testSnowMana2() {
        addCard(Zone.BATTLEFIELD, playerA, "Plains");
        addCard(Zone.BATTLEFIELD, playerA, "Island");
        addCard(Zone.BATTLEFIELD, playerA, "Swamp");
        addCard(Zone.BATTLEFIELD, playerA, "Snow-Covered Mountain");
        addCard(Zone.BATTLEFIELD, playerA, vodalianArcanist);
        addCard(Zone.BATTLEFIELD, playerA, rimefeatherOwl);
        addCard(Zone.HAND, playerA, searchForGlory);
        addCard(Zone.LIBRARY, playerA, "Snow-Covered Plains");

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {U}");
        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {R}");
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{1}{S}", vodalianArcanist);
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, searchForGlory);
        addTarget(playerA, "Snow-Covered Plains");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertGraveyardCount(playerA, searchForGlory, 1);
        assertLife(playerA, 21);
    }

    @Test
    public void testSnowMana3() {
        addCard(Zone.BATTLEFIELD, playerA, "Snow-Covered Island");
        addCard(Zone.BATTLEFIELD, playerA, "Island", 4);
        addCard(Zone.BATTLEFIELD, playerB, silvercoatLion);
        addCard(Zone.HAND, playerA, bergStrider);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, bergStrider);
        addTarget(playerA, silvercoatLion);

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertTapped(silvercoatLion, true);
    }

    @Test
    public void testTreasureMana() {
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 4);
        addCard(Zone.HAND, playerA, jadedSellSword);
        addCard(Zone.HAND, playerA, strikeItRich, 1);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, strikeItRich, true);
        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}, Sacrifice");
        setChoice(playerA, "Red");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, jadedSellSword);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertAbility(playerA, jadedSellSword, FirstStrikeAbility.getInstance(), true);
        assertAbility(playerA, jadedSellSword, HasteAbility.getInstance(), true);
    }

    @Test
    public void testTreasureMana2() {
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 4);
        addCard(Zone.HAND, playerA, jadedSellSword);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, jadedSellSword);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertAbility(playerA, jadedSellSword, FirstStrikeAbility.getInstance(), false);
        assertAbility(playerA, jadedSellSword, HasteAbility.getInstance(), false);
    }

    @Test
    public void testVerazol() {
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);
        addCard(Zone.HAND, playerA, verazolTheSplitCurrent);

        setChoice(playerA, "X=2");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, verazolTheSplitCurrent);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertCounterCount(playerA, verazolTheSplitCurrent, CounterType.P1P1, 4);
        assertPowerToughness(playerA, verazolTheSplitCurrent, 4, 4);
    }

    @Test
    public void testProssh() {
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Wastes");
        addCard(Zone.BATTLEFIELD, playerA, sphereOfResistance);
        addCard(Zone.HAND, playerA, prosshSkyraiderOfKher);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, prosshSkyraiderOfKher);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerA, koboldsOfKherKeep, 7);
    }

    @Test
    public void testRitualManaNormal() {
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 2);
        addCard(Zone.HAND, playerA, pyreticRitual);
        addCard(Zone.HAND, playerA, grayOgre);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, pyreticRitual, true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, grayOgre);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertGraveyardCount(playerA, pyreticRitual, 1);
        assertPermanentCount(playerA, grayOgre, 1);
    }

    @Test
    public void testRitualManaCopied() {
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 6);
        addCard(Zone.HAND, playerA, isochronScepter);
        addCard(Zone.HAND, playerA, pyreticRitual);
        addCard(Zone.HAND, playerA, grayOgre);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, isochronScepter);
        setChoice(playerA, true);
        setChoice(playerA, pyreticRitual);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{2}, {T}:");
        setChoice(playerA, true);
        setChoice(playerA, true);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, grayOgre);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertTapped(isochronScepter, true);
        assertExileCount(playerA, pyreticRitual, 1);
        assertPermanentCount(playerA, grayOgre, 1);
    }

    @Test
    public void testManaDrainNormal() {
        addCard(Zone.BATTLEFIELD, playerA, "Island", 4);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 1);
        addCard(Zone.HAND, playerA, manaDrain);
        addCard(Zone.HAND, playerA, grayOgre);
        addCard(Zone.HAND, playerA, sliverConstruct);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, grayOgre);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, manaDrain, grayOgre);

        waitStackResolved(1, PhaseStep.POSTCOMBAT_MAIN);  // Let the Mana Drain delayed triggered ability resolve
        checkManaPool("Mana Drain added 3 Colorless", 1, PhaseStep.POSTCOMBAT_MAIN, playerA, "C", 3);
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, sliverConstruct);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertGraveyardCount(playerA, manaDrain, 1);
        assertGraveyardCount(playerA, grayOgre, 1);
        assertPermanentCount(playerA, sliverConstruct, 1);
    }

    @Test
    public void testManaDrainCopied() {
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 11);
        addCard(Zone.HAND, playerA, isochronScepter);
        addCard(Zone.HAND, playerA, manaDrain);
        addCard(Zone.HAND, playerA, grayOgre);
        addCard(Zone.HAND, playerA, sliverConstruct);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, isochronScepter);
        setChoice(playerA, true);
        setChoice(playerA, manaDrain);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, grayOgre);
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{2}, {T}:");
        setChoice(playerA, true);
        setChoice(playerA, true);
        addTarget(playerA, grayOgre);

        waitStackResolved(1, PhaseStep.POSTCOMBAT_MAIN);  // Let the Mana Drain delayed triggered ability resolve
        checkManaPool("Mana Drain added 3 Colorless", 1, PhaseStep.POSTCOMBAT_MAIN, playerA, "C", 3);
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, sliverConstruct);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertTapped(isochronScepter, true);
        assertExileCount(playerA, manaDrain, 1);
        assertGraveyardCount(playerA, grayOgre, 1);
        assertPermanentCount(playerA, sliverConstruct, 1);
    }

    @Test
    public void testInduceParanoia() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);
        addCard(Zone.HAND, playerA, induceParanoia);
        addCard(Zone.HAND, playerB, balduvianBears);
        addCard(Zone.BATTLEFIELD, playerB, "Forest", 2);

        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, balduvianBears);
        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerA, induceParanoia, balduvianBears);

        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerA, induceParanoia, 1);
        assertGraveyardCount(playerB, balduvianBears, 1);
        assertGraveyardCount(playerB, 1 + 2);
    }

    @Test
    public void testInduceParanoiaNoBlackMana() {
        setStrictChooseMode(true);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 4);
        addCard(Zone.HAND, playerA, induceParanoia);
        addCard(Zone.HAND, playerB, balduvianBears);
        addCard(Zone.BATTLEFIELD, playerB, "Forest", 2);


        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, balduvianBears);
        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerA, induceParanoia, balduvianBears);

        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerA, induceParanoia, 1);
        assertGraveyardCount(playerB, balduvianBears, 1);
        assertGraveyardCount(playerB, 1);
    }
}
