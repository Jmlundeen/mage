
package mage.cards.a;

import mage.MageInt;
import mage.abilities.common.EntersBattlefieldAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.PermanentsOnBattlefieldCount;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.ReachAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 *
 * @author jeffwadsworth
 */
public final class AvatarOfTheResolute extends CardImpl {

    public AvatarOfTheResolute(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{G}{G}");
        this.subtype.add(SubType.AVATAR);
        this.power = new MageInt(3);
        this.toughness = new MageInt(2);

        // Reach
        this.addAbility(ReachAbility.getInstance());
        
        // Trample
        this.addAbility(TrampleAbility.getInstance());
        
        // Avatar of the Resolute enters the battlefield with a +1/+1 counter on it for each other creature you control with a +1/+1 counter on it.
        DynamicValue numberCounters = new PermanentsOnBattlefieldCount(StaticFilters.FILTER_OTHER_CONTROLLED_CREATURE_P1P1);
        this.addAbility(new EntersBattlefieldAbility(new EntersWithCountersEffect(CounterType.P1P1, numberCounters))
        );
        
    }

    private AvatarOfTheResolute(final AvatarOfTheResolute card) {
        super(card);
    }

    @Override
    public AvatarOfTheResolute copy() {
        return new AvatarOfTheResolute(this);
    }
}
