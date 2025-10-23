
package mage.cards.b;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.dynamicvalue.common.SourceXCostValue;
import mage.abilities.effects.common.combat.CantAttackBlockTargetEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.counters.CounterType;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class BribersPurse extends CardImpl {

    public BribersPurse(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{X}");

        // Briber's Purse enters the battlefield with X gem counters on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.GEM, SourceXCostValue.instance)));

        // {1}, {T}, Remove a gem counter from Briber's Purse: Target creature can't attack or block this turn.
        Ability ability = new SimpleActivatedAbility(new CantAttackBlockTargetEffect(Duration.EndOfTurn), new GenericManaCost(1));
        ability.addCost(new TapSourceCost());
        ability.addCost(new RemoveCountersSourceCost(CounterType.GEM.createInstance()));
        ability.addTarget(new TargetCreaturePermanent());
        this.addAbility(ability);
    }

    private BribersPurse(final BribersPurse card) {
        super(card);
    }

    @Override
    public BribersPurse copy() {
        return new BribersPurse(this);
    }
}
