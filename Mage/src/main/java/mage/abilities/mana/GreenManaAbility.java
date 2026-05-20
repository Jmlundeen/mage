

package mage.abilities.mana;

import mage.Mana;
import mage.abilities.costs.common.TapSourceCost;

/**
 *
 * @author BetaSteward_at_googlemail.com
 */
public class GreenManaAbility extends ComposedManaAbility {

    public GreenManaAbility() {
        super(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addStatic(Mana.GreenMana(1))
                .ruleText("Add {G}")
        );
    }

    private GreenManaAbility(final GreenManaAbility ability) {
        super(ability);
    }

    @Override
    public GreenManaAbility copy() {
        return new GreenManaAbility(this);
    }

}
