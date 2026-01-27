package mage.server.ws;

import mage.interfaces.callback.ClientCallback;

/**
 * Interface for sending ClientCallback messages to WebSocket clients.
 * This replaces the JBoss AsynchInvokerCallbackHandler dependency.
 *
 * @author Jmlundeen
 */
public interface WebSocketCallbackHandler {

    /**
     * Send a callback to the client.
     *
     * @param callback the callback to send
     */
    void sendCallback(ClientCallback callback);

    /**
     * Check if the WebSocket connection is still active.
     *
     * @return true if connected, false otherwise
     */
    boolean isConnected();

    /**
     * Close the WebSocket connection and clean up resources.
     */
    void close();
}
