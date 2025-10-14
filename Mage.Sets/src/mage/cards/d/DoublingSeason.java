package mage.cards.d;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.replacement.ReplaceCounterEffect;
import mage.abilities.effects.common.replacement.ReplaceTokenEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class DoublingSeason extends CardImpl {

    public DoublingSeason(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{4}{G}");

        // If an effect would create one or more tokens under your control, it creates twice that many of those tokens instead.
        this.addAbility(new SimpleStaticAbility(new ReplaceTokenEffect(ReplaceTokenEffect.ModificationType.MULTIPLY, 2)
                .setText("If an effect would create one or more tokens under your control, " +
                        "it creates twice that many of those tokens instead")));

        // If an effect would put one or more counters on a permanent you control, it puts twice that many of those counters on that permanent instead.
        this.addAbility(new SimpleStaticAbility(new ReplaceCounterEffect(ReplaceCounterEffect.ModificationType.MULTIPLY, 2)
                .setPermanentFilter(StaticFilters.FILTER_CONTROLLED_PERMANENT)
                .onlyFromEffects()
                .setText("If an effect would put one or more counters on a permanent you control, " +
                        "it puts twice that many of those counters on that permanent instead")
        ));
    }

    private DoublingSeason(final DoublingSeason card) {
        super(card);
    }

    @Override
    public DoublingSeason copy() {
        return new DoublingSeason(this);
    }
}
