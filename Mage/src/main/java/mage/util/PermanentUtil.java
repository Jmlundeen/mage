package mage.util;

import mage.abilities.Abilities;
import mage.abilities.mana.ActivatedManaAbilityImpl;
import mage.constants.ManaType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.HashSet;
import java.util.Set;

public class PermanentUtil {

    /**
     * Gets all mana types that the given permanent can produce.
     *
     * @param permanent the permanent to check
     * @param game      the game context
     * @return a set of mana types that the permanent can produce
     */
    public static Set<ManaType> getProducibleMana(Permanent permanent, Game game) {
        Set<ManaType> allTypes = new HashSet<>(6);
        if (permanent != null) {
            Abilities<ActivatedManaAbilityImpl> manaAbilities = permanent.getAbilities().getActivatedManaAbilities(Zone.BATTLEFIELD);
            for (ActivatedManaAbilityImpl ability : manaAbilities) {
                allTypes.addAll(ability.getProducableManaTypes(game));
            }
        }
        return allTypes;
    }
}
