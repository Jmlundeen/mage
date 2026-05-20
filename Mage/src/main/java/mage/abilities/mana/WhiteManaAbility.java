

package mage.abilities.mana;

import mage.Mana;
import mage.abilities.costs.common.TapSourceCost;

/**
 *
 * @author BetaSteward_at_googlemail.com
 */
public class WhiteManaAbility extends ComposedManaAbility {

    public WhiteManaAbility() {
        super(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addStatic(Mana.WhiteMana(1))
                .ruleText("Add {W}")
        );
    }

    private WhiteManaAbility(final WhiteManaAbility ability) {
        super(ability);
    }

    @Override
    public WhiteManaAbility copy() {
        return new WhiteManaAbility(this);
    }

}
