
package mage.cards.m;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.mana.AnyColorManaAbility;
import mage.abilities.mana.ColorlessManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 *
 * @author Styxo
 */
public final class MoistureFarm extends CardImpl {

    public MoistureFarm(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.LAND},"");

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Put a storage counter on Moisture Farm.
        this.addAbility(new SimpleActivatedAbility(new AddCountersSourceEffect(CounterType.STORAGE.createInstance()), new TapSourceCost()));

        // {T}, Remove a storage counter from Moisture Farm: Add one mana of any color.
        Ability ability = new AnyColorManaAbility();
        ability.addCost(new RemoveCountersSourceCost(CounterType.STORAGE.createInstance()));
        this.addAbility(ability);
    }

    private MoistureFarm(final MoistureFarm card) {
        super(card);
    }

    @Override
    public MoistureFarm copy() {
        return new MoistureFarm(this);
    }
}
