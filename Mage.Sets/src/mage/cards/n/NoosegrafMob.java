
package mage.cards.n;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.common.SpellCastAllTriggeredAbility;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.DoIfCostPaid;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.game.permanent.token.ZombieToken;

import java.util.UUID;

/**
 *
 * @author fireshoes
 */
public final class NoosegrafMob extends CardImpl {

    public NoosegrafMob(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{4}{B}{B}");
        this.subtype.add(SubType.ZOMBIE);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Noosegraf Mob enters the battlefield with five +1/+1 counters on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.P1P1.createInstance(5))));

        // Whenever a player casts a spell, remove a +1/+1 counter from Noosegraf Mob. If you do, create a 2/2 black Zombie creature token.
        this.addAbility(new SpellCastAllTriggeredAbility(new DoIfCostPaid(
                new CreateTokenEffect(new ZombieToken()),
                null,
                new RemoveCountersSourceCost(CounterType.P1P1.createInstance()),
                false
        ), false));
    }

    private NoosegrafMob(final NoosegrafMob card) {
        super(card);
    }

    @Override
    public NoosegrafMob copy() {
        return new NoosegrafMob(this);
    }
}
