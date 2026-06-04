package mage.filter.predicate.typed.Spell;

import mage.constants.Zone;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.game.Game;
import mage.game.stack.Spell;

public enum SpellCastFromZonePredicate implements ISpellPredicate {
    GRAVEYARD(Zone.GRAVEYARD),
    EXILE(Zone.EXILED),
    HAND(Zone.HAND),
    LIBRARY(Zone.LIBRARY),
    COMMAND(Zone.COMMAND),
    ALL(Zone.ALL);

    private final Zone zone;

    SpellCastFromZonePredicate(Zone zone) {
        this.zone = zone;
    }

    @Override
    public boolean apply(ObjectSourcePlayer<Spell> input, Game game) {
        return input.getObject() != null && input.getObject().getFromZone().match(zone);
    }
}
