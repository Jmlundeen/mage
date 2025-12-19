package mage.cards.n;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.dynamicvalue.common.GetXValue;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.BestowAbility;
import mage.abilities.keyword.ReachAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ContinuousAffected;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 * @author Susucr
 */
public final class NyxbornHydra extends CardImpl {

    private static final DynamicValue xValue = new CountersSourceCount(CounterType.P1P1);

    public NyxbornHydra(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT, CardType.CREATURE}, "{X}{G}");

        this.subtype.add(SubType.HYDRA);
        this.power = new MageInt(0);
        this.toughness = new MageInt(1);

        // Bestow {X}{G}{G}
        this.addAbility(new BestowAbility(this, "{X}{G}{G}"));

        // Reach
        this.addAbility(ReachAbility.getInstance());

        // Trample
        this.addAbility(TrampleAbility.getInstance());

        // Nyxborn Hydra enters the battlefield with X +1/+1 counters on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.P1P1, GetXValue.instance)));

        // Enchanted creature gets +1/+1 for each +1/+1 counter on Nyxborn Hydra and has reach and trample.
        Ability ability = new SimpleStaticAbility(new ContinuousEffectBuilder(Outcome.BoostCreature, ContinuousAffected.ATTACHED_TO)
                .withAddPower(xValue)
                .withAddToughness(xValue)
                .withGainedAbilities(ReachAbility.getInstance(), TrampleAbility.getInstance())
                .setText("Enchanted creature gets +1/+1 for each +1/+1 counter on {this} and has reach and trample")
        );
        this.addAbility(ability);
    }

    private NyxbornHydra(final NyxbornHydra card) {
        super(card);
    }

    @Override
    public NyxbornHydra copy() {
        return new NyxbornHydra(this);
    }
}
