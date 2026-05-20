package mage.abilities.mana;

import mage.abilities.costs.Cost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.constants.Zone;

public class AnyColorManaAbility extends ComposedManaAbility {

    public AnyColorManaAbility() {
        this(new TapSourceCost());
    }

    public AnyColorManaAbility(Cost cost) {
        this(Zone.BATTLEFIELD, cost, null);
    }

    /**
     * @param cost
     * @param netAmount dynamic value used during available mana calculation to
     *                  set the max possible amount the source can produce
     */
    public AnyColorManaAbility(Cost cost, DynamicValue netAmount) {
        this(Zone.BATTLEFIELD, cost, netAmount);
    }

    public AnyColorManaAbility(Zone zone, Cost cost, DynamicValue netAmount) {
        super(new ComposedManaAbilityBuilder()
                .zone(zone)
                .cost(cost)
                .addAnyColor(1)
                .capacityOverride(netAmount)
                .ruleText("Add one mana of any color")
                .build());
    }

    protected AnyColorManaAbility(final AnyColorManaAbility ability) {
        super(ability);
    }

    @Override
    public AnyColorManaAbility copy() {
        return new AnyColorManaAbility(this);
    }
}
