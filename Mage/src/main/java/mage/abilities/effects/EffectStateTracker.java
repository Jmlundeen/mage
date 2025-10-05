package mage.abilities.effects;

import mage.MageItem;
import mage.MageObject;
import mage.game.CardState;
import mage.game.Game;
import mage.game.MageObjectAttribute;
import mage.game.permanent.Permanent;

import java.util.*;

/**
 * Tracks changes made during continuous effect simulation to quickly revert changes
 *
 * @author Jmlundeen
 */
public class EffectStateTracker {

    // Track original values of modified permanents
    private final Map<UUID, Permanent> permanentSnapshots = new HashMap<>();
    private final Map<UUID, CardState> cardStateSnapshots = new HashMap<>();
    private final Map<UUID, MageObjectAttribute> mageObjectAttributeSnapshots = new HashMap<>();


    /**
     * Records state for any MageItem that might be affected
     */
    public void recordMageItemState(MageItem item, Game game) {
        // only store the first change to avoid overwriting original state
        if (permanentSnapshots.containsKey(item.getId()) ||
            cardStateSnapshots.containsKey(item.getId()) ||
            mageObjectAttributeSnapshots.containsKey(item.getId())) {
            return;
        }
        if (item instanceof MageObject) {
            if (item instanceof Permanent) {
                permanentSnapshots.put(item.getId(), ((Permanent) item).copy());
            } else {
                cardStateSnapshots.put(item.getId(), game.getState().getCardState(item.getId()).copy());
            }
            MageObjectAttribute attr = game.getState().getMageObjectAttribute(item.getId());
            if (attr != null) {
                mageObjectAttributeSnapshots.put(item.getId(), new MageObjectAttribute((MageObject) item, game));
            } else {
                mageObjectAttributeSnapshots.put(item.getId(), null);
            }
        }
    }

    /**
     * Reverts all tracked changes back to their original state
     */
    public void revertChanges(Game game) {
        for (Permanent original : permanentSnapshots.values()) {
            game.getBattlefield().addPermanent(original.copy());
        }
        for (Map.Entry<UUID, CardState> entry : cardStateSnapshots.entrySet()) {
            CardState current = game.getState().getCardState(entry.getKey());
            current.copyFrom(entry.getValue());
        }
        for (Map.Entry<UUID, MageObjectAttribute> entry : mageObjectAttributeSnapshots.entrySet()) {
            MageObjectAttribute current = game.getState().getMageObjectAttribute(entry.getKey());
            if (current != null && entry.getValue() != null) {
                current.copyFrom(entry.getValue());
            } else {
                game.getState().removeMageObjectAttribute(entry.getKey());
            }
        }
    }

    /**
     * Clears all tracked state
     */
    public void clear() {
        permanentSnapshots.clear();
        cardStateSnapshots.clear();
        mageObjectAttributeSnapshots.clear();
    }

}
