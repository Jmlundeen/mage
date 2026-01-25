package mage.remote.transport;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Event bus for lobby events. Manages subscriptions and event dispatching.
 * Thread-safe for concurrent subscription/unsubscription and event publishing.
 */
public class LobbyEventBus {

    private static final Logger logger = Logger.getLogger(LobbyEventBus.class);

    private final List<LobbyEventListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Subscribe a listener to receive lobby events.
     *
     * @param listener The listener to add
     */
    public void subscribe(LobbyEventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            logger.debug("Lobby event listener subscribed: " + listener.getClass().getSimpleName());
        }
    }

    /**
     * Unsubscribe a listener from receiving lobby events.
     *
     * @param listener The listener to remove
     */
    public void unsubscribe(LobbyEventListener listener) {
        if (listener != null) {
            listeners.remove(listener);
            logger.debug("Lobby event listener unsubscribed: " + listener.getClass().getSimpleName());
        }
    }

    /**
     * Publish a lobby event to all subscribed listeners.
     *
     * @param event The event to publish
     */
    public void publish(LobbyEvent event) {
        if (event == null) {
            return;
        }

        logger.debug("Publishing lobby event to " + listeners.size() + " listener(s)");

        for (LobbyEventListener listener : listeners) {
            try {
                listener.onLobbyEvent(event);
            } catch (Exception e) {
                logger.error("Error notifying lobby event listener: " + listener.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * Get the number of currently subscribed listeners.
     *
     * @return The listener count
     */
    public int getListenerCount() {
        return listeners.size();
    }

    /**
     * Clear all subscribed listeners.
     */
    public void clear() {
        listeners.clear();
        logger.debug("All lobby event listeners cleared");
    }
}
