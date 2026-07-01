
package mage.cards.d;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.dynamicvalue.common.manavalue.CounteredManaValue;
import mage.abilities.effects.common.counter.AddCountersEffect;
import mage.abilities.effects.common.countered.CounterEffect;
import mage.abilities.keyword.FlashAbility;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.filter.StaticTypedFilters;
import mage.target.TargetGeneric;

import java.util.UUID;

/**
 *
 * @author emerald000
 */
public final class DrainingWhelk extends CardImpl {

    public DrainingWhelk(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{4}{U}{U}");
        this.subtype.add(SubType.ILLUSION);

        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // Flash
        this.addAbility(FlashAbility.getInstance());
        
        // Flying
        this.addAbility(FlyingAbility.getInstance());
        
        // When Draining Whelk enters the battlefield, counter target spell. Put X +1/+1 counters on Draining Whelk, where X is that spell's converted mana cost.
        Ability ability = new EntersBattlefieldTriggeredAbility(new CounterEffect()
                .setText("counter target spell")
                .setRememberManaValue(true)
        );
        ability.addEffect(new AddCountersEffect(CounterType.P1P1, CounteredManaValue.instance)
                .setText("Put X +1/+1 counters on {this}, where X is that spell's converted mana cost"));
        ability.addTarget(new TargetGeneric(StaticTypedFilters.SPELL));
        this.addAbility(ability);
    }

    private DrainingWhelk(final DrainingWhelk card) {
        super(card);
    }

    @Override
    public DrainingWhelk copy() {
        return new DrainingWhelk(this);
    }
}
