
package mage.cards.t;

import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.mana.AnyColorManaAbility;
import mage.abilities.mana.ColorlessManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 *
 * @author Loki
 */
public final class TendoIceBridge extends CardImpl {

    public TendoIceBridge(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.LAND},"");

        // Tendo Ice Bridge enters the battlefield with a charge counter on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.CHARGE.createInstance())));

        // {tap}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {tap}, Remove a charge counter from Tendo Ice Bridge: Add one mana of any color.
        Ability ability = new AnyColorManaAbility();
        ability.addCost(new RemoveCountersSourceCost(CounterType.CHARGE.createInstance()));
        this.addAbility(ability);
    }

    private TendoIceBridge(final TendoIceBridge card) {
        super(card);
    }

    @Override
    public TendoIceBridge copy() {
        return new TendoIceBridge(this);
    }
}
