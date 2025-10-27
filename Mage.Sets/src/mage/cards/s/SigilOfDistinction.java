
package mage.cards.s;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.dynamicvalue.common.GetXValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.EquipAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ContinuousAffected;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 * @author Loki
 */
public final class SigilOfDistinction extends CardImpl {

    private static final DynamicValue xValue = new CountersSourceCount(CounterType.CHARGE);

    public SigilOfDistinction(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ARTIFACT},"{X}");
        this.subtype.add(SubType.EQUIPMENT);

        // Sigil of Distinction enters the battlefield with X charge counters on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.CHARGE, GetXValue.instance)));

        // Equipped creature gets +1/+1 for each charge counter on Sigil of Distinction.
        Effect effect = new ContinuousEffectBuilder(Outcome.BoostCreature, ContinuousAffected.ATTACHED_TO)
                .withAddPower(xValue)
                .withAddToughness(xValue)
                .setText("Equipped creature gets +1/+1 for each charge counter on {this}.");
        this.addAbility(new SimpleStaticAbility(effect));

        // Equip—Remove a charge counter from Sigil of Distinction.
        this.addAbility(new EquipAbility(Outcome.AddAbility, new RemoveCountersSourceCost(CounterType.CHARGE.createInstance()), false));

    }

    private SigilOfDistinction(final SigilOfDistinction card) {
        super(card);
    }

    @Override
    public SigilOfDistinction copy() {
        return new SigilOfDistinction(this);
    }
}
