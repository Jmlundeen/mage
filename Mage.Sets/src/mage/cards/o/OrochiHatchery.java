
package mage.cards.o;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.dynamicvalue.common.GetXValue;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.counters.CounterType;
import mage.game.permanent.token.SnakeToken;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class OrochiHatchery extends CardImpl {

    public OrochiHatchery(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ARTIFACT},"{X}{X}");

        // Orochi Hatchery enters the battlefield with X charge counters on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.CHARGE, GetXValue.instance)));

        // {5}, {T}: Create a 1/1 green Snake creature token for each charge counter on Orochi Hatchery.
        Ability ability = new SimpleActivatedAbility(new CreateTokenEffect(new SnakeToken(), new CountersSourceCount(CounterType.CHARGE)), new GenericManaCost(5));
        ability.addCost(new TapSourceCost());
        this.addAbility(ability);
    }

    private OrochiHatchery(final OrochiHatchery card) {
        super(card);
    }

    @Override
    public OrochiHatchery copy() {
        return new OrochiHatchery(this);
    }

}
