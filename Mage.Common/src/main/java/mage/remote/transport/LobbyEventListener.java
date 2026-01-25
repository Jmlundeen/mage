package mage.remote.transport;

/**
 * Listener interface for lobby events from the server.
 * UI components can implement this to receive push updates.
 */
@FunctionalInterface
public interface LobbyEventListener {

    /**
     * Called when a lobby update event is received from the server.
     * This will be called on the WebSocket client thread, so implementations
     * should handle threading appropriately (e.g., use SwingUtilities.invokeLater).
     *
     * @param event The lobby event containing updated tables, users, and matches
     */
    void onLobbyEvent(LobbyEvent event);
}
