package mage.cards.u;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.continuous.UntapAllDuringEachOtherPlayersUntapStepEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 * @author BetaSteward
 */
public final class UnwindingClock extends CardImpl {

    public UnwindingClock(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{4}");

        // Untap all artifacts you control during each other player's untap step.
        this.addAbility(new SimpleStaticAbility(new UntapAllDuringEachOtherPlayersUntapStepEffect(StaticFilters.FILTER_CONTROLLED_PERMANENT_ARTIFACTS)));
    }

    private UnwindingClock(final UnwindingClock card) {
        super(card);
    }

    @Override
    public UnwindingClock copy() {
        return new UnwindingClock(this);
    }
}
