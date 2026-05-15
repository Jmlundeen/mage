package mage.abilities.mana.value.common;

import mage.abilities.Ability;
import mage.abilities.effects.Effect;
import mage.abilities.mana.providers.ManaTypeProvider;
import mage.constants.ManaType;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.EnumSet;
import java.util.Set;

public enum SourceManaTypes implements ManaTypeProvider {
    instance;

    @Override
    public Set<ManaType> getManaTypes(Game game, Ability source, Effect effect) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        return permanent == null
                ? EnumSet.noneOf(ManaType.class)
                : ManaType.getManaTypesFromObjectColor(permanent.getColor(game));
    }
}
