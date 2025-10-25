package mage.cards.h;

import mage.MageInt;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.dynamicvalue.common.SourceXCostValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.VigilanceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.mageobject.AnotherPredicate;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class HaldirLorienLieutenant extends CardImpl {

    private static final FilterPermanent filter = new FilterControlledCreaturePermanent(SubType.ELF, "other Elves you control");
    private static final DynamicValue xValue = new CountersSourceCount(CounterType.P1P1);

    static {
        filter.add(AnotherPredicate.instance);
    }

    public HaldirLorienLieutenant(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{X}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ELF);
        this.subtype.add(SubType.SOLDIER);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Haldir, Lorien Lieutenant enters the battlefield with X +1/+1 counters on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.P1P1, SourceXCostValue.instance)));

        // Vigilance
        this.addAbility(VigilanceAbility.getInstance());

        // {5}{G}: Until end of turn, other Elves you control gain vigilance and get +1/+1 for each +1/+1 counter on Haldir.
        Effect effect = new ContinuousEffectBuilder(Duration.EndOfTurn, Outcome.BoostCreature)
                .setPermanentFilter(filter)
                .withGainedAbilities(VigilanceAbility.getInstance())
                .withAddPower(xValue)
                .withAddToughness(xValue)
                .setText("until end of turn, other Elves you control gain vigilance and get +1/+1 for each +1/+1 counter on {this}");
        this.addAbility(new SimpleActivatedAbility(effect,  new ManaCostsImpl<>("{5}{G}")));
    }

    private HaldirLorienLieutenant(final HaldirLorienLieutenant card) {
        super(card);
    }

    @Override
    public HaldirLorienLieutenant copy() {
        return new HaldirLorienLieutenant(this);
    }
}
