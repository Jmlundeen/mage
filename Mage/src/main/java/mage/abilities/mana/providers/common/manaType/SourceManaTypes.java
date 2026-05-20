package mage.abilities.mana.providers.common.manaType;

import mage.abilities.Ability;
import mage.abilities.effects.Effect;
import mage.abilities.mana.providers.ManaTypeProvider;
import mage.constants.ManaType;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum SourceManaTypes implements ManaTypeProvider {
    instance;

    @Override
    public Set<ManaType> getManaTypes(Game game, Ability source, Effect effect) {
        if (game == null) {
            return Collections.emptySet();
        }
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        return permanent == null
                ? EnumSet.noneOf(ManaType.class)
                : ManaType.getManaTypesFromObjectColor(permanent.getColor(game));
    }
}
