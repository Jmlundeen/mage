package mage.cards.t;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ContinuousAffected;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class TrawlerDrake extends CardImpl {

    private static final DynamicValue xValue = new CountersSourceCount(CounterType.OIL);

    public TrawlerDrake(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}");

        this.subtype.add(SubType.PHYREXIAN);
        this.subtype.add(SubType.DRAKE);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Trawler Drake enters the battlefield with an oil counter on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.OIL.createInstance())));

        // Trawler Drake gets +1/+1 for each oil counter on it.
        this.addAbility(new SimpleStaticAbility(new ContinuousEffectBuilder(Outcome.BoostCreature, ContinuousAffected.SOURCE)
                .withAddPower(xValue)
                .withAddToughness(xValue)
                .setText("{this} gets +1/+1 for each oil counter on it")));

        // Whenever you cast a noncreature spell, put an oil counter on Trawler Drake.
        this.addAbility(new SpellCastControllerTriggeredAbility(
                new AddCountersSourceEffect(CounterType.OIL.createInstance()),
                StaticFilters.FILTER_SPELL_A_NON_CREATURE, false
        ));
    }

    private TrawlerDrake(final TrawlerDrake card) {
        super(card);
    }

    @Override
    public TrawlerDrake copy() {
        return new TrawlerDrake(this);
    }
}
