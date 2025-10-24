

package mage.cards.e;

import mage.Mana;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.dynamicvalue.common.MultikickerCount;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.MultikickerAbility;
import mage.abilities.mana.DynamicManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.counters.CounterType;

import java.util.UUID;



/**
 *
 * @author BetaSteward_at_googlemail.com, North
 */
public final class EverflowingChalice extends CardImpl {

    public EverflowingChalice(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ARTIFACT},"{0}");

        // Multikicker {2} (You may pay an additional {2} any number of times as you cast this spell.)
        this.addAbility(new MultikickerAbility("{2}"));

        // Everflowing Chalice enters the battlefield with a charge counter on it for each time it was kicked.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.CHARGE, MultikickerCount.instance)));

        // {T}: Add {C} for each charge counter on Everflowing Chalice.
        this.addAbility(new DynamicManaAbility(Mana.ColorlessMana(1), new CountersSourceCount(CounterType.CHARGE)));
    }

    private EverflowingChalice(final EverflowingChalice card) {
        super(card);
    }

    @Override
    public EverflowingChalice copy() {
        return new EverflowingChalice(this);
    }

}

