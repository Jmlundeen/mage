package mage.cards.s;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.GetXValue;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.ProtectionAbility;
import mage.abilities.keyword.ReachAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.filter.FilterObject;
import mage.filter.predicate.mageobject.MulticoloredPredicate;

import java.util.UUID;

/**
 *
 * @author Tsirides
 */
public final class StonecoilSerpent extends CardImpl {

    private static final FilterObject filter = new FilterObject("multicolored");

    static {
        filter.add(MulticoloredPredicate.instance);
    }

    public StonecoilSerpent(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ARTIFACT,CardType.CREATURE},"{X}");
        this.subtype.add(SubType.SNAKE);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        //Trample, Reach, Protection from Multicolored
        this.addAbility(new ProtectionAbility(filter));
        this.addAbility(ReachAbility.getInstance());
        this.addAbility(TrampleAbility.getInstance());


        // Endless One enters the battlefield with X +1/+1 counters on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.P1P1, GetXValue.instance)));
    }

    private StonecoilSerpent(final StonecoilSerpent card) {
        super(card);
    }

    @Override
    public StonecoilSerpent copy() {
        return new StonecoilSerpent(this);
    }
}
