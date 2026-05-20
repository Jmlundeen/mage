

package mage.abilities.mana;

import mage.Mana;
import mage.abilities.costs.common.TapSourceCost;

/**
 *
 * @author BetaSteward_at_googlemail.com
 */
public class BlackManaAbility extends ComposedManaAbility {

    public BlackManaAbility() {
        super(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addStatic(Mana.BlackMana(1))
                .ruleText("Add {B}")
        );
    }

    private BlackManaAbility(final BlackManaAbility ability) {
        super(ability);
    }

    @Override
    public BlackManaAbility copy() {
        return new BlackManaAbility(this);
    }

}
