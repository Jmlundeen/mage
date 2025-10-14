package mage.cards.c;

import mage.MageInt;
import mage.abilities.common.DiesSourceTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.SourcePermanentPowerValue;
import mage.abilities.effects.common.GainLifeEffect;
import mage.abilities.effects.common.replacement.ReplaceCounterEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class ConclaveMentor extends CardImpl {

    public ConclaveMentor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{G}{W}");

        this.subtype.add(SubType.CENTAUR);
        this.subtype.add(SubType.CLERIC);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // If one or more +1/+1 counters would be put on a creature you control, that many plus one +1/+1 counters are put on that creature instead.
        this.addAbility(new SimpleStaticAbility(new ReplaceCounterEffect(ReplaceCounterEffect.ModificationType.ADD, 1)
                .setPermanentFilter(StaticFilters.FILTER_CONTROLLED_CREATURE)
                .addValidCounterTypes(CounterType.P1P1)
                .setText("If one or more +1/+1 counters would be put on a creature you control, " +
                        "that many plus one +1/+1 counters are put on that creature instead.")
        ));

        // When Conclave Mentor dies, you gain life equal to its power.
        this.addAbility(new DiesSourceTriggeredAbility(new GainLifeEffect(SourcePermanentPowerValue.NOT_NEGATIVE, "you gain life equal to its power")));
    }

    private ConclaveMentor(final ConclaveMentor card) {
        super(card);
    }

    @Override
    public ConclaveMentor copy() {
        return new ConclaveMentor(this);
    }
}
