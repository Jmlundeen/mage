

package mage.abilities.mana;

import mage.Mana;
import mage.abilities.costs.common.TapSourceCost;

/**
 *
 * @author BetaSteward_at_googlemail.com
 */
public class BlueManaAbility extends ComposedManaAbility {

    public BlueManaAbility() {
        super(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addStatic(Mana.BlueMana(1))
                .ruleText("Add {U}")
        );
    }

    private BlueManaAbility(final BlueManaAbility ability) {
        super(ability);
    }

    @Override
    public BlueManaAbility copy() {
        return new BlueManaAbility(this);
    }

}
