
package mage.cards.d;

import mage.abilities.common.AttacksCreatureYouControlTriggeredAbility;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 *
 * @author noxx

 */
public final class DruidsRepository extends CardImpl {

    public DruidsRepository(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ENCHANTMENT},"{1}{G}{G}");


        // Whenever a creature you control attacks, put a charge counter on Druids' Repository.
        this.addAbility(new AttacksCreatureYouControlTriggeredAbility(new AddCountersSourceEffect(CounterType.CHARGE.createInstance())));

        // Remove a charge counter from Druids' Repository: Add one mana of any color.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new RemoveCountersSourceCost(CounterType.CHARGE.createInstance()))
                .capacityOverride(new CountersSourceCount(CounterType.CHARGE))
                .addAnyColor(1)
                .ruleText("Add one mana of any color")
                .build()
        );
    }

    private DruidsRepository(final DruidsRepository card) {
        super(card);
    }

    @Override
    public DruidsRepository copy() {
        return new DruidsRepository(this);
    }
}
