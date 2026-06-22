package mage.abilities.mana;

import mage.abilities.costs.Cost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.constants.Zone;
import mage.util.CardUtil;

public class AnyColorManaAbility extends ComposedManaAbility {

    public AnyColorManaAbility() {
        this(new TapSourceCost());
    }

    public AnyColorManaAbility(int amount) {
        this(Zone.BATTLEFIELD, new TapSourceCost(), null, amount);
    }

    public AnyColorManaAbility(Cost cost) {
        this(Zone.BATTLEFIELD, cost, null, 1);
    }

    public AnyColorManaAbility(int amount, Cost cost) {
        this(Zone.BATTLEFIELD, cost, null, amount);
    }

    public AnyColorManaAbility(Zone zone, Cost cost) {
        this(zone, cost, null, 1);
    }

    /**
     * @param cost
     * @param maxPossible dynamic value used during available mana calculation to
     *                  set the max possible amount the source can produce
     */
    public AnyColorManaAbility(Cost cost, DynamicValue maxPossible) {
        this(Zone.BATTLEFIELD, cost, maxPossible, 1);
    }

    public AnyColorManaAbility(Zone zone, Cost cost, DynamicValue maxPossible, int amount) {
        super(new ComposedManaAbilityBuilder()
                .zone(zone)
                .cost(cost)
                .addAnyColor(amount)
                .capacityOverride(maxPossible)
                .ruleText(String.format("Add %s mana of any %s", CardUtil.numberToText(amount), amount == 1 ? "color" : "one color"))
        );
    }

    protected AnyColorManaAbility(final AnyColorManaAbility ability) {
        super(ability);
    }

    @Override
    public AnyColorManaAbility copy() {
        return new AnyColorManaAbility(this);
    }
}
