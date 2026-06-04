package mage.cards.i;

import mage.Mana;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.CompositeCost;
import mage.abilities.costs.common.PayLifeCost;
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
 * @author spjspj
 */
public final class InfernalDarkness extends CardImpl {

    public InfernalDarkness(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{B}{B}");

        // Cumulative upkeep-Pay {B} and 1 life.
        this.addAbility(new CumulativeUpkeepAbility(new CompositeCost(
                new ManaCostsImpl<>("{B}"), new PayLifeCost(1), "pay {B} and 1 life"
        )));

        // If a land is tapped for mana, it produces {B} instead of any other type.
        this.addAbility(new SimpleStaticAbility(
                ReplaceManaEffect.produced(Duration.WhileOnBattlefield, Outcome.Neutral, ReplaceManaEffect.replaceAllProducedMana(Mana.BlackMana(1)))
                        .setProducedMatcher(StaticTypedFilters.A_LAND)
                        .setText("If a land is tapped for mana, it produces {B} instead of any other type and amount")
        ));
    }

    private InfernalDarkness(final InfernalDarkness card) {
        super(card);
    }

    @Override
    public InfernalDarkness copy() {
        return new InfernalDarkness(this);
    }
}
