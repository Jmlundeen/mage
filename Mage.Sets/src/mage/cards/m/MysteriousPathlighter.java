package mage.cards.m;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ContinuousAffected;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.mageobject.AdventurePredicate;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class MysteriousPathlighter extends CardImpl {

    private static final FilterPermanent filter = new FilterControlledCreaturePermanent("creature you control that has an Adventure");

    static {
        filter.add(AdventurePredicate.instance);
    }

    public MysteriousPathlighter(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{W}");

        this.subtype.add(SubType.FAERIE);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Each creature you control that has an Adventure enters the battlefield with an additional +1/+1 counter on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(ContinuousAffected.STATIC_OR_DYNAMIC, CounterType.P1P1.createInstance())
                .setFilter(filter)
        ));
    }

    private MysteriousPathlighter(final MysteriousPathlighter card) {
        super(card);
    }

    @Override
    public MysteriousPathlighter copy() {
        return new MysteriousPathlighter(this);
    }
}
