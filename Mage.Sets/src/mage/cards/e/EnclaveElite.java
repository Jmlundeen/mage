
package mage.cards.e;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.MultikickerCount;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.IslandwalkAbility;
import mage.abilities.keyword.MultikickerAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 *
 * @author North
 */
public final class EnclaveElite extends CardImpl {

    public EnclaveElite(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{2}{U}");
        this.subtype.add(SubType.MERFOLK);
        this.subtype.add(SubType.SOLDIER);

        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Multikicker (You may pay an additional any number of times as you cast this spell.)
        this.addAbility(new MultikickerAbility("{1}{U}"));

        // Islandwalk
        this.addAbility(new IslandwalkAbility());

        // Enclave Elite enters the battlefield with a +1/+1 counter on it for each time it was kicked.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.P1P1, MultikickerCount.instance)));
    }

    private EnclaveElite(final EnclaveElite card) {
        super(card);
    }

    @Override
    public EnclaveElite copy() {
        return new EnclaveElite(this);
    }
}
