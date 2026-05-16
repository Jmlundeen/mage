package mage.abilities.mana.providers.common;

import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.effects.Effect;
import mage.abilities.mana.providers.ManaTypeProvider;
import mage.constants.ManaType;
import mage.game.Game;

import java.util.Set;

public enum ChosenColorTypeProvider implements ManaTypeProvider {
    instance;

    @Override
    public Set<ManaType> getManaTypes(Game game, Ability source, Effect effect) {
        ObjectColor color = (ObjectColor) game.getState().getValue(source.getSourceId() + "_color");
        if (color != null) {
            return ManaType.getManaTypesFromObjectColor(color);
        }
        return Set.of();
    }
}
