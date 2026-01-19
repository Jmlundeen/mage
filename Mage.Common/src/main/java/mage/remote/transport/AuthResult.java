package mage.remote.transport;

/**
 * Result of WS/transport auth.
 */
public class AuthResult {

    private final boolean ok;
    private final String message;

    public AuthResult(boolean ok, String message) {
        this.ok = ok;
        this.message = message;
    }

    public boolean isOk() {
        return ok;
    }

    public String getMessage() {
        return message;
    }
}
