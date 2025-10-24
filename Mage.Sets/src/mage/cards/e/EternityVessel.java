
package mage.cards.e;

import mage.abilities.common.LandfallAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.ControllerLifeCount;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.effects.common.SetPlayerLifeSourceEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Zone;
import mage.counters.CounterType;

import java.util.UUID;

/**
 *
 * @author jeffwadsworth
 */
public final class EternityVessel extends CardImpl {

    public EternityVessel(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ARTIFACT},"{6}");

        // Eternity Vessel enters the battlefield with X charge counters on it, where X is your life total.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.CHARGE, ControllerLifeCount.instance)
                .withXText()
        ));

        // Landfall - Whenever a land you control enters, you may have your life total become the number of charge counters on Eternity Vessel.
        this.addAbility(new LandfallAbility(Zone.BATTLEFIELD, new SetPlayerLifeSourceEffect(new CountersSourceCount(CounterType.CHARGE))
                .setText("have your life total become the number of charge counters on {this}"),
                true)
        );
    }

    private EternityVessel(final EternityVessel card) {
        super(card);
    }

    @Override
    public EternityVessel copy() {
        return new EternityVessel(this);
    }
}
