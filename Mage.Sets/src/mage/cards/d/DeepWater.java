package mage.cards.d;

import mage.Mana;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
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
public final class DeepWater extends CardImpl {

    public DeepWater(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{U}{U}");

        // {U}: Until end of turn, if you tap a land you control for mana, it produces {U} instead of any other type.
        this.addAbility(new SimpleActivatedAbility(
                ReplaceManaEffect.produced(Duration.EndOfTurn, Outcome.Neutral, ReplaceManaEffect.replaceAllProducedMana(Mana.BlueMana(1)))
                        .setProducedMatcher(StaticTypedFilters.LAND_YOU_CONTROL)
                        .setText("Until end of turn, if you tap a land you control for mana, it produces {U} instead of any other type"),
                new ManaCostsImpl<>("{U}")
        ));
    }

    private DeepWater(final DeepWater card) {
        super(card);
    }

    @Override
    public DeepWater copy() {
        return new DeepWater(this);
    }
}
