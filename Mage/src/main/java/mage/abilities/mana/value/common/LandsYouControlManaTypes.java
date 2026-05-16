package mage.abilities.mana.value.common;

import mage.abilities.Abilities;
import mage.abilities.Ability;
import mage.abilities.effects.Effect;
import mage.abilities.mana.ActivatedManaAbilityImpl;
import mage.abilities.mana.providers.ManaTypeProvider;
import mage.constants.ManaType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mana types that lands you control could produce.
 */
public enum LandsYouControlManaTypes implements ManaTypeProvider {
    instance;

    @Override
    public Set<ManaType> getManaTypes(Game game, Ability source, Effect effect) {
        Set<ManaType> types = EnumSet.noneOf(ManaType.class);
        if (game == null || source == null || game.getPhase() == null) {
            return types;
        }

        Set<ManaType> allTypes = new HashSet<>(6);
        List<Permanent> lands = game.getBattlefield().getActivePermanents(StaticFilters.FILTER_CONTROLLED_PERMANENT_LANDS, source.getControllerId(), source, game);
        for (Permanent land : lands) {
            Abilities<ActivatedManaAbilityImpl> manaAbilities = land.getAbilities().getActivatedManaAbilities(Zone.BATTLEFIELD);
            for (ActivatedManaAbilityImpl ability : manaAbilities) {
                allTypes.addAll(ability.getProducableManaTypes(game));
            }
        }
        return allTypes;
    }
}

