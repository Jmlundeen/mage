package mage.cards.m;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.replacement.ReplaceCounterEffect;
import mage.abilities.keyword.TrampleAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.CounterType;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class MowuLoyalCompanion extends CardImpl {

    public MowuLoyalCompanion(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.DOG);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Trample
        this.addAbility(TrampleAbility.getInstance());

        // Vigilance
        this.addAbility(VigilanceAbility.getInstance());

        // If one or more +1/+1 counters would be put on Mowu, Loyal Companion, that many plus one +1/+1 counters are put on it instead.
        this.addAbility(new SimpleStaticAbility(new ReplaceCounterEffect(ReplaceCounterEffect.ModificationType.ADD, 1, true)
                .setPermanentFilter(StaticFilters.FILTER_SOURCE_PERMANENT)
                .addValidCounterTypes(CounterType.P1P1)
                .setText("If one or more +1/+1 counters would be put on {this}, " +
                        "that many plus one +1/+1 counters are put on it instead")
        ));
    }

    private MowuLoyalCompanion(final MowuLoyalCompanion card) {
        super(card);
    }

    @Override
    public MowuLoyalCompanion copy() {
        return new MowuLoyalCompanion(this);
    }
}
