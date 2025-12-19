package mage.cards.b;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.filter.StaticFilters;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.AnotherPredicate;

import java.util.UUID;

/**
 * @author emerald000
 */
public final class BramblewoodParagon extends CardImpl {

    private static final FilterPermanent filter = new FilterCreaturePermanent(SubType.WARRIOR, "other Warrior creature you control");

    static {
        filter.add(TargetController.YOU.getControllerPredicate());
        filter.add(AnotherPredicate.instance);
    }

    public BramblewoodParagon(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}");
        this.subtype.add(SubType.ELF, SubType.WARRIOR);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Each other Warrior creature you control enters the battlefield with an additional +1/+1 counter on it.
        this.addAbility(new SimpleStaticAbility(
                new EntersWithCountersEffect(ContinuousAffected.STATIC_OR_DYNAMIC, CounterType.P1P1.createInstance())
                        .setFilter(filter)
        ));

        // Each creature you control with a +1/+1 counter on it has trample.
        this.addAbility(new SimpleStaticAbility(new ContinuousEffectBuilder(
                Outcome.AddAbility,
                StaticFilters.FILTER_EACH_CONTROLLED_CREATURE_P1P1)
                .withGainedAbilities(TrampleAbility.getInstance())
                .setText("Each creature you control with a +1/+1 counter on it has trample")
        ));

    }

    private BramblewoodParagon(final BramblewoodParagon card) {
        super(card);
    }

    @Override
    public BramblewoodParagon copy() {
        return new BramblewoodParagon(this);
    }
}
