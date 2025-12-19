package mage.cards.a;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.SourcePermanentToughnessValue;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ContinuousAffected;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.CounterType;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 * @author Susucr
 */
public final class ArwenWeaverOfHope extends CardImpl {

    public ArwenWeaverOfHope(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ELF);
        this.subtype.add(SubType.NOBLE);
        this.power = new MageInt(2);
        this.toughness = new MageInt(1);

        // Each other creature you control enters the battlefield with a number of additional +1/+1 counters on it equal to Arwen, Weaver of Hope's toughness.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(
                ContinuousAffected.STATIC_OR_DYNAMIC, CounterType.P1P1, SourcePermanentToughnessValue.instance)
                .withNumberOfText()
                .setFilter(StaticFilters.FILTER_OTHER_CONTROLLED_CREATURE)
        ));
    }

    private ArwenWeaverOfHope(final ArwenWeaverOfHope card) {
        super(card);
    }

    @Override
    public ArwenWeaverOfHope copy() {
        return new ArwenWeaverOfHope(this);
    }
}
