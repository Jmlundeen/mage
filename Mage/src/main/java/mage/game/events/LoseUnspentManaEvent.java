package mage.game.events;

import mage.abilities.Ability;
import mage.players.ManaPoolItem;

import java.util.List;
import java.util.UUID;

/**
 * Event fired for each unspent mana lost from a player's mana pool. This event is fired at
 * the end of steps and phases, as well as when an effect causes a player to lose unspent mana.
 * If the event is being replaced, modify the mana items for the event to contain only the mana that should
 * be put back into the player's mana pool.
 * @author Jmlundeen
 */
public class LoseUnspentManaEvent extends GameEvent {

    private final List<ManaPoolItem> manaItems;

    public LoseUnspentManaEvent(UUID playerId, Ability source, List<ManaPoolItem> manaItems) {
        super(EventType.LOSE_UNSPENT_MANA, playerId, source, playerId, 0, false);
        this.manaItems = manaItems;
        this.amount = manaItems.stream().mapToInt(ManaPoolItem::count).sum();
    }

    public List<ManaPoolItem> getManaItems() {
        return manaItems;
    }

}
