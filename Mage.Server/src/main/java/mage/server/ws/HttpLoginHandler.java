package mage.server.ws;

import com.google.gson.Gson;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import mage.server.DisconnectReason;
import mage.server.MainManagerFactory;
import mage.server.Session;
import mage.server.SessionManagerImpl;
import org.apache.log4j.Logger;
import org.jboss.remoting.callback.AsynchInvokerCallbackHandler;
import org.jboss.remoting.callback.Callback;
import org.jboss.remoting.callback.HandleCallbackException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * HTTP endpoint handler for Basic Auth login.
 * Returns a session ID that can be used for WebSocket connections.
 * @author jmlundeen
 */
public class HttpLoginHandler {

    private static final Logger logger = Logger.getLogger(HttpLoginHandler.class);

    private final MainManagerFactory managerFactory;
    private final SessionManagerImpl sessionManager;
    private final boolean detailsMode;

    // TODO: remove after jboss is removed
    private final AsynchInvokerCallbackHandler noopCallbackHandler = new AsynchInvokerCallbackHandler() {
        @Override
        public void handleCallback(Callback callback) throws HandleCallbackException {

        }

        @Override
        public void handleCallbackOneway(Callback callback) throws HandleCallbackException {

        }

        @Override
        public void handleCallbackOneway(Callback callback, boolean b) throws HandleCallbackException {

        }

        @Override
        public void handleCallback(Callback callback, boolean b, boolean b1) throws HandleCallbackException {

        }
    };

    public HttpLoginHandler(MainManagerFactory managerFactory, boolean detailsMode) {
        this.managerFactory = managerFactory;
        this.sessionManager = (SessionManagerImpl) managerFactory.sessionManager();
        this.detailsMode = detailsMode;
    }

    /**
     * Handle HTTP login request with Basic Auth.
     * Returns JSON with sessionId on success.
     */
    public void handleLogin(Context ctx) {
        try {
            // Extract Basic Auth credentials
            String authHeader = ctx.header("Authorization");
            if (authHeader == null || !authHeader.startsWith("Basic ")) {
                logger.warn("HTTP Login: missing or invalid Authorization header");
                ctx.status(HttpStatus.UNAUTHORIZED) // Unauthorized
                   .json(new LoginResponse(false, "Missing authorization", null));
                return;
            }

            // Decode Basic Auth
            String base64Credentials = authHeader.substring(6);
            byte[] decoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(decoded, StandardCharsets.UTF_8);

            String[] parts = credentials.split(":", 2);
            if (parts.length != 2) {
                logger.warn("HTTP Login: invalid credentials format");
                ctx.status(HttpStatus.UNAUTHORIZED) // Unauthorized
                   .json(new LoginResponse(false, "Invalid credentials format", null));
                return;
            }

            String username = parts[0];
            String password = parts[1];

            // Optional restore session ID from query parameter
            String restoreSessionId = ctx.queryParam("restoreSessionId");
            if (restoreSessionId == null) {
                restoreSessionId = "";
            }

            logger.info("HTTP Login: attempting login for user=" + username);

            // Generate new session ID
            String sessionId = UUID.randomUUID().toString();

            // Create session in SessionManager with noop callback handler
            // The WebSocket callback handler will be assigned when the WebSocket connection is established
            sessionManager.createSession(sessionId, noopCallbackHandler);

            // Set host information
            String clientHost = ctx.ip();
            Optional<Session> sessionOpt = sessionManager.getSession(sessionId);
            sessionOpt.ifPresent(session -> session.setHost(clientHost));

            // Authenticate user
            boolean authenticated = sessionManager.connectUser(sessionId, restoreSessionId, username, password, "ws-http", detailsMode);

            if (!authenticated) {
                // Authentication failed - clean up session
                logger.warn("HTTP Login: authentication failed for user=" + username);
                try {
                    sessionManager.disconnect(sessionId, DisconnectReason.LostConnection, false);
                } catch (Exception e) {
                    logger.debug("Error cleaning up failed session", e);
                }

                ctx.status(HttpStatus.UNAUTHORIZED) // Unauthorized
                        .contentType(ContentType.APPLICATION_JSON)
                        .result(new Gson().toJson(new LoginResponse(false, "Authentication failed", null)));
                return;
            }

            logger.info("HTTP Login: success for user=" + username + ", sessionId=" + sessionId);

            // Return session ID
            ctx.sessionAttribute("sessionId", sessionId);
            ctx.status(HttpStatus.OK) // OK
               .contentType(ContentType.APPLICATION_JSON)
                    .result(new Gson().toJson(new LoginResponse(true, "Login successful", sessionId)));

        } catch (Exception e) {
            logger.error("HTTP Login: error processing login", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR) // Internal Server Error
               .json(new LoginResponse(false, "Server error: " + e.getMessage(), null));
        }
    }

    /**
         * Response object for login endpoint.
         */
        public record LoginResponse(boolean success, String message, String sessionId) {
    }
}
