package mage.ws;

/**
 * Strict protocol version used by the new WebSocket + Protobuf transport.
 *
 * IMPORTANT:
 * - Increment it when making a wire-level breaking change.
 */
public final class ProtocolVersion {

    // Increment when updated (breaking wire protocol change).
    private static final String VERSION = "1";

    private ProtocolVersion() {
    }

    public static String getVersion() {
        return VERSION;
    }

    public static boolean equalsStrict(String other) {
        if (other == null) {
            return false;
        }
        return VERSION.equals(normalize(other));
    }

    public static String normalize(String version) {
        // Keep it conservative for now: trim only.
        return version == null ? "" : version.trim();
    }
}
