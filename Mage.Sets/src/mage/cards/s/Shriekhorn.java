package mage.cards.s;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.MillCardsTargetEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.counters.CounterType;
import mage.target.TargetPlayer;

import java.util.UUID;

/**
 * @author Loki
 */
public final class Shriekhorn extends CardImpl {

    public Shriekhorn(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{1}");

        // Shriekhorn enters the battlefield with three charge counters on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.CHARGE.createInstance(3))));

        // {T}, Remove a charge counter from Shriekhorn: Target player puts the top two cards of their library into their graveyard.
        Ability ability = new SimpleActivatedAbility(new MillCardsTargetEffect(2), new TapSourceCost());
        ability.addCost(new RemoveCountersSourceCost(CounterType.CHARGE.createInstance()));
        ability.addTarget(new TargetPlayer());
        this.addAbility(ability);
    }

    private Shriekhorn(final Shriekhorn card) {
        super(card);
    }

    @Override
    public Shriekhorn copy() {
        return new Shriekhorn(this);
    }

}
