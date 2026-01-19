package mage.server.ws;

import org.apache.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Enforces "one active WS connection per sessionId".
 *
 * Policy:
 * - When a second connection claims the same sessionId, the old connection is closed.
 */
public class WsConnectionRegistry {

    private static final Logger logger = Logger.getLogger(WsConnectionRegistry.class);

    public interface CloseableConnection {
        void close();
    }

    private final ConcurrentHashMap<String, CloseableConnection> bySessionId = new ConcurrentHashMap<>();

    public void onSessionIdentified(String sessionId, CloseableConnection connection) {
        if (sessionId == null || sessionId.trim().isEmpty() || connection == null) {
            return;
        }

        CloseableConnection prev = bySessionId.put(sessionId, connection);
        if (prev != null && prev != connection) {
            try {
                logger.info("WS replaced active connection for sessionId=" + sessionId);
                prev.close();
            } catch (Exception e) {
                logger.debug("Failed to close previous WS session for sessionId=" + sessionId, e);
            }
        }
    }

    public void onDisconnect(CloseableConnection connection) {
        if (connection == null) {
            return;
        }

        bySessionId.entrySet().removeIf(e -> e.getValue() == connection);
    }
}
