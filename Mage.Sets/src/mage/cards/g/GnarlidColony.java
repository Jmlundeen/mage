package mage.cards.g;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.KickedCondition;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.continuous.GainAbilityAllEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.KickerAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class GnarlidColony extends CardImpl {

    public GnarlidColony(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}");

        this.subtype.add(SubType.BEAST);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Kicker {2}{G}
        this.addAbility(new KickerAbility("{2}{G}"));

        // If Gnarlid Colony was kicked, it enters with two +1/+1 counters on it.
        this.addAbility(new SimpleStaticAbility(Zone.ALL, new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance(2))
                        .setText("if {this} was kicked, it enters with two +1/+1 counters on it"),
                KickedCondition.ONCE
        )));

        // Each creature you control with a +1/+1 counter on it has trample.
        this.addAbility(new SimpleStaticAbility(new GainAbilityAllEffect(
                TrampleAbility.getInstance(),
                Duration.WhileOnBattlefield,
                StaticFilters.FILTER_EACH_CONTROLLED_CREATURE_P1P1))
        );
    }

    private GnarlidColony(final GnarlidColony card) {
        super(card);
    }

    @Override
    public GnarlidColony copy() {
        return new GnarlidColony(this);
    }
}
