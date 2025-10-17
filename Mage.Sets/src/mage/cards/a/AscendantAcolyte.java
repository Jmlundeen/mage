package mage.cards.a;

import mage.MageInt;
import mage.abilities.common.EntersBattlefieldAbility;
import mage.abilities.dynamicvalue.common.CountersCount;
import mage.abilities.effects.common.DoubleCountersSourceEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
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
public final class AscendantAcolyte extends CardImpl {

    private static final CountersCount xValue = new CountersCount(CounterType.P1P1, StaticFilters.FILTER_OTHER_CONTROLLED_CREATURES)
            .setUseAmong(true);

    public AscendantAcolyte(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{G}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.MONK);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // Ascendant Acolyte enters the battlefield with a +1/+1 counter on it for each +1/+1 counter among other creatures you control.
        this.addAbility(new EntersBattlefieldAbility(new EntersWithCountersEffect(CounterType.P1P1, xValue))
                .addHint(xValue.getHint())
        );

        // At the beginning of your upkeep, double the number of +1/+1 counters on Ascendant Acolyte.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(
                new DoubleCountersSourceEffect(CounterType.P1P1)
        ));
    }

    private AscendantAcolyte(final AscendantAcolyte card) {
        super(card);
    }

    @Override
    public AscendantAcolyte copy() {
        return new AscendantAcolyte(this);
    }
}
