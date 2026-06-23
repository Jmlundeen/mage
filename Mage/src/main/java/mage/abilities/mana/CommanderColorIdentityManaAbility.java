package mage.abilities.mana;

import mage.abilities.costs.Cost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.providers.common.manaType.CommanderColorIdentityManaTypes;

/**
 * @author LevelX2
 */
public class CommanderColorIdentityManaAbility extends ComposedManaAbility {

    public CommanderColorIdentityManaAbility() {
        this(new TapSourceCost());
    }

    public CommanderColorIdentityManaAbility(Cost cost) {
        super(ComposedManaAbilityBuilder.builder()
                .cost(cost)
                .addChoice(CommanderColorIdentityManaTypes.instance, 1)
                .ruleText("Add one mana of any color in your commander's color identity")
        );
    }

    protected CommanderColorIdentityManaAbility(final CommanderColorIdentityManaAbility ability) {
        super(ability);
    }

    @Override
    public CommanderColorIdentityManaAbility copy() {
        return new CommanderColorIdentityManaAbility(this);
    }
}
