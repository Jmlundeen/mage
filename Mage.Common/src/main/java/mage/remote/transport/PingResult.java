package mage.remote.transport;

/**
 * Result of WS/transport ping.
 */
public class PingResult {

    private final long serverTimeMillis;
    private final long clientTimeMillis;

    public PingResult(long serverTimeMillis, long clientTimeMillis) {
        this.serverTimeMillis = serverTimeMillis;
        this.clientTimeMillis = clientTimeMillis;
    }

    public long getServerTimeMillis() {
        return serverTimeMillis;
    }

    public long getClientTimeMillis() {
        return clientTimeMillis;
    }
}
