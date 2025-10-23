package org.mage.test.cards.continuous;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.Card;
import mage.constants.PhaseStep;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.target.TargetPermanent;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    /*
    A Tale for the Ages
    {1}{W}
    Enchantment
    Enchanted creatures you control get +2/+2.
    */
    private static final String aTaleForTheAges = "A Tale for the Ages";

    /*
    Spirit Link
    {W}
    Enchantment - Aura
    Enchant creature
    Whenever enchanted creature deals damage, you gain that much life.
    */
    private static final String spiritLink = "Spirit Link";

    /*
    Archon of the Wild Rose
    {2}{W}{W}
    Creature - Archon
    Flying
    Other creatures you control that are enchanted by Auras you control have base power and toughness 4/4 and have flying.
    4/4
    */
    private static final String archonOfTheWildRose = "Archon of the Wild Rose";

    /*
    Avalanche of Sector 7
    {2}{R}
    Legendary Creature - Human Rebel
    Menace
    Avalanche of Sector 7's power is equal to the number of artifacts your opponents control.
    Whenever an opponent activates an ability of an artifact they control, Avalanche of Sector 7 deals 1 damage to that player.
    0/3
    */
    private static final String avalancheOfSector7 = "Avalanche of Sector 7";

    /*
    Tormod's Crypt
    {0}
    Artifact
    {tap}, Sacrifice Tormod's Crypt: Exile all cards from target player's graveyard.
    */
    private static final String tormodsCrypt = "Tormod's Crypt";

    /*
     Ashes of the Fallen
     {2}
     Artifact
     As Ashes of the Fallen enters the battlefield, choose a creature type.
     Each creature card in your graveyard has the chosen creature type in addition to its other types.
     */
    private static final String ashesOfTheFallen = "Ashes of the Fallen";

    /*
    Shade's Breath
    {1}{B}
    Instant
    Until end of turn, each creature you control becomes a black Shade and gains "{B}: This creature gets +1/+1 until end of turn."
    */
    private static final String shadesBreath = "Shade's Breath";

    /*
    Clamavus
    {4}{G}
    Creature - Human Tyranid Artificer
    Proclamator Hailer -- Each creature you control gets +1/+1 for each +1/+1 counter on it.
    3/3
    */
    private static final String clamavus = "Clamavus";

    @Test
    public void testGainOneAbility() {
        addCard(Zone.BATTLEFIELD, playerA, aRealmReborn);
        addCard(Zone.BATTLEFIELD, playerA, balduvianBears);

        checkPlayableAbility("Bear has mana ability", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                "{T}: Add one mana of any color.", true);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }

    @Test
    public void testBoostPowerToughness() {
        addCard(Zone.BATTLEFIELD, playerA, aTaleForTheAges);
        addCard(Zone.BATTLEFIELD, playerA, balduvianBears + "@bearA");
        addCard(Zone.BATTLEFIELD, playerB, balduvianBears);
        addCard(Zone.BATTLEFIELD, playerA, "Plains");
        addCard(Zone.HAND, playerA, spiritLink);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, spiritLink, "@bearA");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPowerToughness(playerA, balduvianBears, 4, 4); // 2/2 + 2/2 = 4/4
        assertPowerToughness(playerB, balduvianBears, 2, 2); // 2/2
    }

    @Test
    public void testSetPowerToughness() {
        addCard(Zone.BATTLEFIELD, playerA, archonOfTheWildRose);
        addCard(Zone.BATTLEFIELD, playerA, balduvianBears + "@bearA");
        addCard(Zone.BATTLEFIELD, playerB, balduvianBears);
        addCard(Zone.BATTLEFIELD, playerA, "Plains");
        addCard(Zone.HAND, playerA, spiritLink);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, spiritLink, "@bearA");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPowerToughness(playerA, balduvianBears, 4, 4); // set to 4/4 by Archon
        assertAbility(playerA, balduvianBears, FlyingAbility.getInstance(), true); // gains flying by Archon
        assertPowerToughness(playerB, balduvianBears, 2, 2); // 2/2
        assertAbility(playerB, balduvianBears, FlyingAbility.getInstance(), false); // doesn't gain flying
    }

    @Test
    public void testSetPowerCDA() {
        addCard(Zone.HAND, playerA, avalancheOfSector7);
        addCard(Zone.BATTLEFIELD, playerA, avalancheOfSector7);
        addCard(Zone.BATTLEFIELD, playerB, tormodsCrypt, 6);
        addCard(Zone.BATTLEFIELD, playerA, tormodsCrypt); // should not count

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPowerToughness(playerA, avalancheOfSector7, 6, 3); // 6 artifacts controlled by opponent
        Card handCard = playerA.getHand().getCards(currentGame).stream().findFirst().orElse(null);
        assertTrue("Avalanche in hand should have power 6", handCard != null && handCard.getPower().getValue() == 6);
    }

    @Test
    public void testChosenCreatureType() {
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 2);
        addCard(Zone.HAND, playerA, ashesOfTheFallen);
        addCard(Zone.GRAVEYARD, playerA, balduvianBears);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, ashesOfTheFallen);
        setChoice(playerA, "Sliver");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertGraveyardCount(playerA, balduvianBears, 1);
        Card card = playerA.getGraveyard().getCards(currentGame).stream().findFirst().orElse(null);
        assertTrue("Balduvian Bears should have subtype Sliver", card != null && card.hasSubtype(SubType.SLIVER, currentGame));
    }

    @Test
    public void testAddedColorAndAbility() {
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 3);
        addCard(Zone.HAND, playerA, shadesBreath);
        addCard(Zone.BATTLEFIELD, playerA, balduvianBears + "@bearA");
        addCard(Zone.BATTLEFIELD, playerB, balduvianBears + "@bearB");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, shadesBreath, true);
        checkPlayableAbility("Player B should not have activated ability", 1, PhaseStep.PRECOMBAT_MAIN, playerB, "{B}: {this}", false);
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{B}: {this}");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPowerToughness(playerA, balduvianBears, 3, 3); // 2/2 +1/+1
        assertPowerToughness(playerB, balduvianBears, 2, 2);
        assertColor(playerA, balduvianBears, "B", true);
        assertColor(playerB, balduvianBears, "B", false);
        assertColor(playerA, balduvianBears, "G", false);
        assertColor(playerB, balduvianBears, "G", true);
        assertTrue("bearA should be subtype Shade", getPermanent(balduvianBears, playerA).hasSubtype(SubType.SHADE, currentGame));
        assertFalse("bearB should not be subtype Shade", getPermanent(balduvianBears, playerB).hasSubtype(SubType.SHADE, currentGame));
    }

    @Test
    public void testDynamicPowerToughnessReferencingObject() {
        Ability ability  = new SimpleActivatedAbility(
                new AddCountersTargetEffect(CounterType.P1P1.createInstance(3)),
                new ManaCostsImpl<>("")
        );
        ability.addTarget(new TargetPermanent(2, StaticFilters.FILTER_PERMANENT));
        addCustomCardWithAbility("add counter", playerA, ability);

        addCard(Zone.BATTLEFIELD, playerA, clamavus);
        addCard(Zone.BATTLEFIELD, playerA, balduvianBears + "@bearA");
        addCard(Zone.BATTLEFIELD, playerB, balduvianBears + "@bearB");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "put");
        addTarget(playerA, "@bearA^@bearB");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPowerToughness(playerA, balduvianBears, 2 + 3 * 2, 2 + 3 * 2); // 2/2 +1/+1 * 3 * 2
        assertPowerToughness(playerB, balduvianBears, 2 + 3, 2 + 3); // no double from Clamavus
    }
}
