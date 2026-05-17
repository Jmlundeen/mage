package mage.abilities.mana;

import mage.abilities.costs.Cost;

/**
 * A consolidated activated mana ability that composes multiple mana values together.
 * 
 * <p>This class replaces multiple legacy mana ability classes:
 * <ul>
 *   <li>SimpleManaAbility</li>
 *   <li>DynamicManaAbility</li>
 *   <li>ConditionalManaAbility</li>
 *   <li>LimitedTimesPerTurnActivatedManaAbility</li>
 *   <li>AnyColorManaAbility</li>
 * </ul>
 * 
 * <p>Use {@link ComposedManaAbilityBuilder} to create instances.
 */
public class ComposedManaAbility extends ActivatedManaAbilityImpl {

    ComposedManaAbility(ComposedManaAbilityBuilder builder) {
        super(builder.getZone(), builder.getManaEffect(), builder.getCost());
        this.maxActivationsPerTurn = builder.getMaxActivations();
        this.poolDependant = builder.isPoolDependant();
        this.condition = builder.getActivationCondition();
        for (Cost additionalCost : builder.getAdditionalCosts()) {
            this.addCost(additionalCost);
        }
    }

    private ComposedManaAbility(final ComposedManaAbility ability) {
        super(ability);
    }

    @Override
    public ComposedManaAbility copy() {
        return new ComposedManaAbility(this);
    }
}
