package mage.abilities.mana.providers.common.manaType;

import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.effects.Effect;
import mage.abilities.mana.providers.ManaTypeProvider;
import mage.constants.ManaType;
import mage.game.Game;

import java.util.Collections;
import java.util.Set;

public enum ChosenColorTypeProvider implements ManaTypeProvider {
    instance;

    @Override
    public Set<ManaType> getManaTypes(Game game, Ability source, Effect effect) {
        if (game == null) {
            return Collections.emptySet();
        }
        ObjectColor color = (ObjectColor) game.getState().getValue(source.getSourceId() + "_color");
        if (color != null) {
            return ManaType.getManaTypesFromObjectColor(color);
        }
        return Set.of();
    }
}
