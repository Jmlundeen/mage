package mage.cards.r;

import mage.Mana;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.mana.ReplaceManaEffect;
import mage.abilities.keyword.CumulativeUpkeepAbility;
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
public final class RitualOfSubdual extends CardImpl {

    public RitualOfSubdual(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{4}{G}{G}");

        // Cumulative upkeep-Pay {2}.
        this.addAbility(new CumulativeUpkeepAbility(new ManaCostsImpl<>("{2}")));

        // If a land is tapped for mana, it produces colorless mana instead of any other type.
        this.addAbility(new SimpleStaticAbility(ReplaceManaEffect.produced(Duration.WhileOnBattlefield, Outcome.Neutral, ReplaceManaEffect.replaceAllProducedMana(Mana.ColorlessMana(1)))
                .setProducedMatcher(StaticTypedFilters.A_LAND)
                .setText("If a land is tapped for mana, it produces colorless mana instead of any other type")
        ));
    }

    private RitualOfSubdual(final RitualOfSubdual card) {
        super(card);
    }

    @Override
    public RitualOfSubdual copy() {
        return new RitualOfSubdual(this);
    }
}
