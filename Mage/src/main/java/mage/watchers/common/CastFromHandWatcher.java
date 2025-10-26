package mage.watchers.common;

import mage.constants.WatcherScope;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.stack.Spell;
import mage.game.turn.Step;
import mage.watchers.Watcher;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class CastFromHandWatcher extends Watcher {

    private final Set<UUID> spellsCastFromHand = new HashSet<>();
    private Step step;

    public CastFromHandWatcher() {
        super(WatcherScope.GAME);
    }


    @Override
    public void watch(GameEvent event, Game game) {
        if (step != null && !Objects.equals(game.getTurn().getStep(), step)) {
            spellsCastFromHand.clear();
            step = null;
        }

        if (step != null && event.getType() == GameEvent.EventType.ZONE_CHANGE) {
            ZoneChangeEvent zEvent = (ZoneChangeEvent) event;
            if (zEvent.getFromZone() != Zone.STACK && spellsCastFromHand.contains(zEvent.getTargetId())) {
                spellsCastFromHand.remove(zEvent.getTargetId());
            }
        }

        if (event.getType() == GameEvent.EventType.SPELL_CAST && event.getZone() == Zone.HAND) {
            if (step == null) {
                step = game.getTurn().getStep();
            }
            Spell spell = (Spell) game.getObject(event.getTargetId());
            if (spell != null) {
                spellsCastFromHand.add(spell.getSourceId());
            }
        }
    }

    public boolean spellWasCastFromHand(UUID sourceId) {
        return spellsCastFromHand.contains(sourceId);
    }

    @Override
    public void reset() {
        super.reset();
        spellsCastFromHand.clear();
    }

}
