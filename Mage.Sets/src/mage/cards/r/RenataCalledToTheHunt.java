package mage.cards.r;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.DevotionCount;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class RenataCalledToTheHunt extends CardImpl {

    public RenataCalledToTheHunt(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT, CardType.CREATURE}, "{2}{G}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.DEMIGOD);
        this.power = new MageInt(0);
        this.toughness = new MageInt(3);

        // Renata's power is equal to your devotion to green.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new ContinuousEffectBuilder(Outcome.BoostCreature, ContinuousAffected.SOURCE)
                        .withSetPower(DevotionCount.G)
                        .setText("{this}'s power is equal to your devotion to green")
        ).addHint(DevotionCount.G.getHint()));

        // Each other creature you control enters the battlefield with an additional +1/+1 counter on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(ContinuousAffected.STATIC_OR_DYNAMIC, CounterType.P1P1.createInstance())
                .setFilter(StaticFilters.FILTER_OTHER_CONTROLLED_CREATURE)
        ));
    }

    private RenataCalledToTheHunt(final RenataCalledToTheHunt card) {
        super(card);
    }

    @Override
    public RenataCalledToTheHunt copy() {
        return new RenataCalledToTheHunt(this);
    }
}
