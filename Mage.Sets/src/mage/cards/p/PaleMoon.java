package mage.cards.p;

import mage.Mana;
import mage.abilities.effects.mana.ReplaceManaEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 * @author L_J
 */
public final class PaleMoon extends CardImpl {

    public PaleMoon(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{U}");

        // Until end of turn, if a player taps a nonbasic land for mana, it produces colorless mana instead of any other type.
        this.getSpellAbility().addEffect(
                ReplaceManaEffect.produced(Duration.EndOfTurn, Outcome.Neutral, ReplaceManaEffect.replaceAllProducedMana(Mana.ColorlessMana(1)))
                        .setProducedMatcher(StaticTypedFilters.A_NONBASIC_LAND)
                        .setText("Until end of turn, if a player taps a nonbasic land for mana, it produces colorless mana instead of any other type")
        );
    }

    private PaleMoon(final PaleMoon card) {
        super(card);
    }

    @Override
    public PaleMoon copy() {
        return new PaleMoon(this);
    }
}
