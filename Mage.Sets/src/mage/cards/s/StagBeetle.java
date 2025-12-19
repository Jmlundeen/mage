
package mage.cards.s;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.PermanentsOnBattlefieldCount;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.AnotherPredicate;

import java.util.UUID;

/**
 *
 * @author LoneFox
 */
public final class StagBeetle extends CardImpl {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent("other creatures on the battlefield");

    static {
        filter.add(AnotherPredicate.instance);
    }

    public StagBeetle(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{3}{G}{G}");
        this.subtype.add(SubType.INSECT);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Stag Beetle enters the battlefield with X +1/+1 counters on it, where X is the number of other creatures on the battlefield.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.P1P1, new PermanentsOnBattlefieldCount(filter))
                .setText("{this} enters with X +1/+1 counters on it, where X is the number of other creatures on the battlefield")
        ));
    }

    private StagBeetle(final StagBeetle card) {
        super(card);
    }

    @Override
    public StagBeetle copy() {
        return new StagBeetle(this);
    }
}
