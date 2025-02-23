package mage.cards.c;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.common.counter.DistributeCountersEffect;
import mage.constants.SubType;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.CrewAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.target.common.TargetPermanentAmount;

/**
 *
 * @author Jmlundeen
 */
public final class CloudspireSkycycle extends CardImpl {
    private static final FilterPermanent filter = new FilterPermanent("other target Vehicles and/or creatures you control");

    static {
        filter.add(Predicates.or(
                SubType.VEHICLE.getPredicate(),
                CardType.CREATURE.getPredicate()
        ));
        filter.add(AnotherPredicate.instance);
    }
    public CloudspireSkycycle(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{2}{R}{W}");
        
        this.subtype.add(SubType.VEHICLE);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // When this Vehicle enters, distribute two +1/+1 counters among one or two other target Vehicles and/or creatures you control.
        Ability ability = new EntersBattlefieldTriggeredAbility(
                new DistributeCountersEffect(CounterType.P1P1)
        );
        ability.addTarget(new TargetPermanentAmount(2, 1, filter));
        this.addAbility(ability);

        // Crew 1
        this.addAbility(new CrewAbility(1));

    }

    private CloudspireSkycycle(final CloudspireSkycycle card) {
        super(card);
    }

    @Override
    public CloudspireSkycycle copy() {
        return new CloudspireSkycycle(this);
    }
}
