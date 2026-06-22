
package mage.cards.p;

import mage.abilities.Ability;
import mage.abilities.ActivatedAbilityImpl;
import mage.abilities.condition.common.SourceHasCounterCondition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.mana.AnyColorManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 *
 * @author spjspj
 */
public final class PyramidOfThePantheon extends CardImpl {

    public PyramidOfThePantheon(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{1}");

        // {2}, {T}: Add one mana of any color. Put a brick counter on Pyramid of the Pantheon.
        Ability ability = new AnyColorManaAbility(new GenericManaCost(2));
        ability.addCost(new TapSourceCost());
        ability.addEffect(new AddCountersSourceEffect(CounterType.BRICK.createInstance()));
        this.addAbility(ability);

        // {T}: Add three mana of any one color. Activate this ability only of there are three or more brick counters on Pyramid of the Pantheon.
        ActivatedAbilityImpl manaAbility = new AnyColorManaAbility(3)
                .setCondition(new SourceHasCounterCondition(CounterType.BRICK, 3));
        manaAbility.appendToRule(" Activate this ability only of there are three or more brick counters on {this}.");
        this.addAbility(manaAbility);
    }

    private PyramidOfThePantheon(final PyramidOfThePantheon card) {
        super(card);
    }

    @Override
    public PyramidOfThePantheon copy() {
        return new PyramidOfThePantheon(this);
    }
}
