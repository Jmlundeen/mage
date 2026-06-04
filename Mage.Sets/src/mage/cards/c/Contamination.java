package mage.cards.c;

import mage.Mana;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.SacrificeTargetCost;
import mage.abilities.effects.common.SacrificeSourceUnlessPaysEffect;
import mage.abilities.effects.mana.ReplaceManaEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.filter.StaticFilters;
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 * @author emerald000
 */
public final class Contamination extends CardImpl {

    public Contamination(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{B}");

        // At the beginning of your upkeep, sacrifice Contamination unless you sacrifice a creature.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(
                new SacrificeSourceUnlessPaysEffect(new SacrificeTargetCost(StaticFilters.FILTER_PERMANENT_CREATURE)))
        );

        // If a land is tapped for mana, it produces {B} instead of any other type and amount.
        this.addAbility(new SimpleStaticAbility(
                ReplaceManaEffect.produced(Duration.WhileOnBattlefield, Outcome.Neutral, ReplaceManaEffect.replaceAllProducedMana(Mana.BlackMana(1)))
                        .setProducedMatcher(StaticTypedFilters.A_LAND)
                        .setText("If a land is tapped for mana, it produces {B} instead of any other type and amount")
        ));
    }

    private Contamination(final Contamination card) {
        super(card);
    }

    @Override
    public Contamination copy() {
        return new Contamination(this);
    }
}
