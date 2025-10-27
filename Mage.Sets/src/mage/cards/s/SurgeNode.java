

package mage.cards.s;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.counters.CounterType;
import mage.target.common.TargetArtifactPermanent;

import java.util.UUID;

/**
 * @author Loki
 */
public final class SurgeNode extends CardImpl {

    public SurgeNode(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{1}");

        // Surge Node enters the battlefield with six charge counters on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.CHARGE.createInstance(6))));

        // {1}, {T}, Remove a charge counter from Surge Node: Put a charge counter on target artifact.
        Ability ability = new SimpleActivatedAbility(new AddCountersTargetEffect(CounterType.CHARGE.createInstance()), new GenericManaCost(1));
        ability.addCost(new TapSourceCost());
        ability.addCost(new RemoveCountersSourceCost(CounterType.CHARGE.createInstance()));
        ability.addTarget(new TargetArtifactPermanent());
        this.addAbility(ability);
    }

    private SurgeNode(final SurgeNode card) {
        super(card);
    }

    @Override
    public SurgeNode copy() {
        return new SurgeNode(this);
    }

}
