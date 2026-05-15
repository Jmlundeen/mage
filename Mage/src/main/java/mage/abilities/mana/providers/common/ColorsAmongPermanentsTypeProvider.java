package mage.abilities.mana.providers.common;

import mage.abilities.Ability;
import mage.abilities.effects.Effect;
import mage.abilities.mana.providers.ManaTypeProvider;
import mage.constants.ManaType;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.EnumSet;
import java.util.Set;

public enum ColorsAmongPermanentsTypeProvider implements ManaTypeProvider {
    YOU_CONTROL;

    @Override
    public Set<ManaType> getManaTypes(Game game, Ability source, Effect effect) {
        if (game == null) {
            return EnumSet.noneOf(ManaType.class);
        }
        Set<ManaType> manaTypes = EnumSet.noneOf(ManaType.class);
        if (YOU_CONTROL.equals(this)) {
            for (Permanent permanent : game.getBattlefield().getAllActivePermanents(source.getControllerId())) {
                manaTypes.addAll(ManaType.getManaTypesFromObjectColor(permanent.getColor(game)));
                if (manaTypes.size() == 5) {
                    break;
                }
            }
        }

        return manaTypes;
    }
}
