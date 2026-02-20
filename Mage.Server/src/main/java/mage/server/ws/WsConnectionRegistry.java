package mage.server.ws;

import mage.ws.MessageProto;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
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
        void send(ByteBuffer data);
        boolean isOpen();
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

    /**
     * Broadcast a server message to all connected clients.
     *
     * @param message The protobuf message to broadcast
     */
    public void broadcast(MessageProto.ServerMessage message) {
        byte[] data = message.toByteArray();
        ByteBuffer buffer = ByteBuffer.wrap(data);

        int successCount = 0;
        int failCount = 0;

        for (CloseableConnection conn : bySessionId.values()) {
            try {
                if (conn.isOpen()) {
                    conn.send(buffer.duplicate());
                    successCount++;
                }
            } catch (Exception e) {
                failCount++;
                logger.debug("Failed to broadcast to connection", e);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Broadcasted " + message.getPayloadCase() + " to " + successCount + " clients (" + failCount + " failed)");
        }
    }

    /**
     * Get the number of active connections.
     */
    public int getConnectionCount() {
        return bySessionId.size();
    }
}
