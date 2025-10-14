package mage.cards.p;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.replacement.ReplaceCounterEffect;
import mage.abilities.effects.common.replacement.ReplaceTokenEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.TargetController;
import mage.counters.CounterType;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class PrimalVigor extends CardImpl {

    public PrimalVigor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{4}{G}");

        // If one or more tokens would be created, twice that many of those tokens are created instead.
        this.addAbility(new SimpleStaticAbility(new ReplaceTokenEffect(ReplaceTokenEffect.ModificationType.MULTIPLY, 2)
                .setControllingPlayer(TargetController.ANY)
                .setText("If one or more tokens would be created, twice that many of those tokens are created instead")
        ));

        // If one or more +1/+1 counters would be put on a creature, twice that many +1/+1 counters are put on that creature instead.
        this.addAbility(new SimpleStaticAbility(new ReplaceCounterEffect(ReplaceCounterEffect.ModificationType.MULTIPLY, 2)
                .addValidCounterTypes(CounterType.P1P1)
                .setPermanentFilter(StaticFilters.FILTER_PERMANENT_A_CREATURE)
                .setText("If one or more +1/+1 counters would be put on a creature, twice that many +1/+1 counters are put on that creature instead")
        ));
    }

    private PrimalVigor(final PrimalVigor card) {
        super(card);
    }

    @Override
    public PrimalVigor copy() {
        return new PrimalVigor(this);
    }
}
