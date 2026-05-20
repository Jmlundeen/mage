

package mage.abilities.mana;

import mage.Mana;
import mage.abilities.costs.common.TapSourceCost;

/**
 *
 * @author BetaSteward_at_googlemail.com
 */
public class RedManaAbility extends ComposedManaAbility {

    public RedManaAbility() {
        super(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addStatic(Mana.RedMana(1))
                .ruleText("Add {R}")
        );
    }

    private RedManaAbility(final RedManaAbility ability) {
        super(ability);
    }

    @Override
    public RedManaAbility copy() {
        return new RedManaAbility(this);
    }

}
