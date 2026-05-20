
package mage.abilities.mana;

import mage.Mana;
import mage.abilities.costs.common.TapSourceCost;

/**
 *
 * @author BetaSteward_at_googlemail.com
 */
public class ColorlessManaAbility extends ComposedManaAbility {

    public ColorlessManaAbility() {
        super(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addStatic(Mana.ColorlessMana(1))
                .ruleText("Add {C}")
        );
    }

    private ColorlessManaAbility(final ColorlessManaAbility ability) {
        super(ability);
    }

    @Override
    public ColorlessManaAbility copy() {
        return new ColorlessManaAbility(this);
    }
}
