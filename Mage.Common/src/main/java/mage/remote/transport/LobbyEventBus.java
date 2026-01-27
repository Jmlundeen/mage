package mage.remote.transport;

import mage.remote.WsSessionImpl;
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
    private final CopyOnWriteArrayList<LobbyEvent> eventQueue = new CopyOnWriteArrayList<>();
    WsSessionImpl wsSession;


    public LobbyEventBus(WsSessionImpl wsSession) {
        this.wsSession = wsSession;
    }

    /**
     * Subscribe a listener to receive lobby events.
     *
     * @param listener The listener to add
     */
    public void subscribe(LobbyEventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            logger.debug("Lobby event listener subscribed: " + listener.getClass().getSimpleName());
            // Dispatch queued events to the newly subscribed listener
            List<LobbyEvent> queuedEvents = List.copyOf(eventQueue);
            for (LobbyEvent event : eventQueue) {
                sendEvent(event, listener);
            }
            eventQueue.removeAll(queuedEvents);
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
        if (listeners.isEmpty()) {
            eventQueue.add(event);
            return;
        }

        logger.debug("Publishing lobby event to " + listeners.size() + " listener(s)");

        for (LobbyEventListener listener : listeners) {
            sendEvent(event, listener);
        }
    }

    private static void sendEvent(LobbyEvent event, LobbyEventListener listener) {
        try {
            listener.onLobbyEvent(event);
        } catch (Exception e) {
            logger.error("Error notifying lobby event listener: " + listener.getClass().getSimpleName(), e);
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
