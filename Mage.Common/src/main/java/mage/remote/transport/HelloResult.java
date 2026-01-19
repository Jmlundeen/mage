package mage.remote.transport;

/**
 * Result of WS/transport hello.
 */
public class HelloResult {

    private final String serverName;
    private final String serverVersion;

    public HelloResult(String serverName, String serverVersion) {
        this.serverName = serverName;
        this.serverVersion = serverVersion;
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerVersion() {
        return serverVersion;
    }
}
