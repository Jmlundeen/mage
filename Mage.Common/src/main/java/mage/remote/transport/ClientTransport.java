package mage.remote.transport;

import mage.remote.Connection;

/**
 * Minimal transport abstraction for incremental client migration.
 *
 * Notes:
 * - Keep JBoss Remoting as default.
 * - This interface only covers the first migrated calls (hello/auth/ping).
 * - All other session RPCs stay on the existing Remoting proxy in {@code SessionImpl}.
 */
public interface ClientTransport {

    void connect(Connection connection) throws Exception;

    void disconnect();

    HelloResult hello(String clientName, String clientVersion) throws Exception;

    AuthResult auth(String sessionId, String userName, String password) throws Exception;

    PingResult ping(String sessionId, long clientTimeMillis) throws Exception;
}
