package org.mage.test.cards.single.blb;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.target.TargetPlayer;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author anonymous
 */
public class InnkeepersTalentTest extends CardTestPlayerBase {

    /*
    Innkeeper's Talent
    {1}{G}
    Enchantment - Class
    (Gain the next level as a sorcery to add its ability.)
    At the beginning of combat on your turn, put a +1/+1 counter on target creature you control.
    {G}: Level 2
    Permanents you control with counters on them have ward {1}.
    {3}{G}: Level 3
    If you would put one or more counters on a permanent or player, put twice that many of each of those kinds of counters on that permanent or player instead.
    */
    private static final String innkeepersTalent = "Innkeeper's Talent";

    /*
    Balduvian Bears
    {1}{G}
    Creature - Bear
    
    2/2
    */
    private static final String balduvianBears = "Balduvian Bears";

    @Test
    public void testInnkeepersTalent() {
        Ability ability = new SimpleActivatedAbility(new AddCountersTargetEffect(CounterType.ENERGY.createInstance()), new ManaCostsImpl<>());
        ability.addTarget(new TargetPlayer(1));
        addCustomCardWithAbility("Energy Counters",
                playerA,
                ability
        );

        addCard(Zone.BATTLEFIELD, playerA, "Forest", 7);
        addCard(Zone.BATTLEFIELD, playerA, innkeepersTalent);
        addCard(Zone.BATTLEFIELD, playerA, balduvianBears + "@bearsA");
        addCard(Zone.BATTLEFIELD, playerB, balduvianBears + "@bearsB");

        // level up to 3
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{G}");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN, 1);
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{3}{G}");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN, 1);

        // player A adding counters should be doubled for own and opponent's creature
        addCounters(1, PhaseStep.PRECOMBAT_MAIN, playerA, "@bearsA", CounterType.P1P1, 1);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN, 1);
        addCounters(1, PhaseStep.PRECOMBAT_MAIN, playerA, "@bearsB", CounterType.P1P1, 1);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN, 1);
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "target player gets an energy", playerA);
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "target player gets an energy", playerB);

        // player B adding counters should not be doubled
        addCounters(1, PhaseStep.PRECOMBAT_MAIN, playerB, "@bearsB", CounterType.P1P1, 1);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertCounterCount(playerA, balduvianBears, CounterType.P1P1, 1 * 2);
        assertCounterCount(playerB, balduvianBears, CounterType.P1P1, 1 * 2 + 1);
        assertCounterCount(playerA, CounterType.ENERGY, 2);
        assertCounterCount(playerB, CounterType.ENERGY, 2);
    }
}