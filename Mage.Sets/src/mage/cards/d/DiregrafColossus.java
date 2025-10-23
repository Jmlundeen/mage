
package mage.cards.d;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.CardsInControllerGraveyardCount;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.filter.FilterCard;
import mage.filter.FilterSpell;
import mage.game.permanent.token.ZombieToken;

import java.util.UUID;

/**
 *
 * @author fireshoes
 */
public final class DiregrafColossus extends CardImpl {

    private static final FilterSpell filter = new FilterSpell("a Zombie spell");
    private static final FilterCard cardFilter = new FilterCard();
    private static final DynamicValue xValue = new CardsInControllerGraveyardCount(cardFilter);
    static {
        filter.add(SubType.ZOMBIE.getPredicate());
        cardFilter.add(SubType.ZOMBIE.getPredicate());
    }

    public DiregrafColossus(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{2}{B}");
        this.subtype.add(SubType.ZOMBIE);
        this.subtype.add(SubType.GIANT);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Diregraf Colossus enters the battlefield with a +1/+1 counter on it for each Zombie card in your graveyard.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.P1P1, xValue)
                .setText("{this} enters with a +1/+1 counter on it for each Zombie card in your graveyard"))
        );

        // Whenever you cast a Zombie spell, create a tapped 2/2 black Zombie creature token.
        this.addAbility(new SpellCastControllerTriggeredAbility(new CreateTokenEffect(new ZombieToken(), 1, true, false), filter, false));

    }

    private DiregrafColossus(final DiregrafColossus card) {
        super(card);
    }

    @Override
    public DiregrafColossus copy() {
        return new DiregrafColossus(this);
    }
}
