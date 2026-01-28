package mage.server.ws;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import mage.server.AuthorizedUserRepository;
import mage.server.MainManagerFactory;
import mage.server.Session;
import org.apache.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTP endpoint handler for user registration.
 * Two-step registration process:
 * 1. User submits registration with username, email, password -> server sends auth token to email
 * 2. User submits auth token -> server completes registration
 * Includes spam protection by rejecting duplicate requests for pending registrations.
 *
 * @author Jmlundeen
 */
public class HttpRegisterHandler {

    private static final Logger logger = Logger.getLogger(HttpRegisterHandler.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_PENDING_REGISTRATIONS = 1000;

    private final MainManagerFactory managerFactory;
    private final boolean testMode;

    // Spam protection: track pending registrations by both email and IP address
    // A registration is "pending" from when auth token is sent until user confirms with token
    private final Map<String, PendingRegistration> pendingRegistrationsByEmail = new ConcurrentHashMap<>();
    private final Map<String, Long> pendingRegistrationsByIp = new ConcurrentHashMap<>();

    // Auto-evict old entries to prevent memory leak
    // This map is used for its side-effect of automatically removing old entries
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final LinkedHashMap<String, Long> registrationHistory = new LinkedHashMap<>(MAX_PENDING_REGISTRATIONS + 1, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Long> eldest) {
            return size() > MAX_PENDING_REGISTRATIONS;
        }
    };

    /**
     * Holds data for a pending registration waiting for auth token confirmation.
     */
    private static class PendingRegistration {
        String username;
        String password;
        String email;
        String authToken;
        long timestamp;
        String ipAddress;

        PendingRegistration(String username, String password, String email, String authToken, String ipAddress) {
            this.username = username;
            this.password = password;
            this.email = email;
            this.authToken = authToken;
            this.timestamp = System.currentTimeMillis();
            this.ipAddress = ipAddress;
        }
    }

    public HttpRegisterHandler(MainManagerFactory managerFactory, boolean testMode) {
        this.managerFactory = managerFactory;
        this.testMode = testMode;
    }

    /**
     * Handle HTTP registration request with Basic Auth.
     * Two-step process:
     * 1. If no authToken provided: validate user data, generate auth token, send to email
     * 2. If authToken provided: verify token and complete registration
     */
    public void handleRegister(Context ctx) {
        try {
            // Extract Basic Auth credentials
            String authHeader = ctx.header("Authorization");
            if (authHeader == null || !authHeader.startsWith("Basic ")) {
                logger.warn("HTTP Register: missing or invalid Authorization header");
                ctx.status(HttpStatus.UNAUTHORIZED)
                   .json(new RegisterResponse(false, "Missing authorization"));
                return;
            }

            // Decode Basic Auth
            String base64Credentials = authHeader.substring(6);
            byte[] decoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(decoded, StandardCharsets.UTF_8);

            String[] parts = credentials.split(":", 2);
            if (parts.length != 2) {
                logger.warn("HTTP Register: invalid credentials format");
                ctx.status(HttpStatus.UNAUTHORIZED)
                   .json(new RegisterResponse(false, "Invalid credentials format"));
                return;
            }

            String username = parts[0];
            String password = parts[1]; // Extract password from Authorization header

            // Get client IP address for spam protection
            String clientIp = ctx.ip();
            if (clientIp.isEmpty()) {
                clientIp = "unknown";
            }

            // Extract request body
            RegisterRequest request;
            try {
                request = ctx.bodyAsClass(RegisterRequest.class);
            } catch (Exception e) {
                logger.warn("HTTP Register: invalid request body", e);
                ctx.status(HttpStatus.BAD_REQUEST)
                   .json(new RegisterResponse(false, "Invalid request body - expected JSON with 'email' and optionally 'authToken'"));
                return;
            }

            // Check if this is step 2 (confirming with auth token)
            if (request.authToken != null && !request.authToken.isEmpty()) {
                handleConfirmRegistration(ctx, username, request.authToken);
                return;
            }

            // Step 1: Initial registration request - send auth token
            handleInitialRegistration(ctx, username, request.email, password, clientIp);

        } catch (Exception e) {
            logger.error("HTTP Register: unexpected error", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
               .json(new RegisterResponse(false, "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * Step 1: Handle initial registration request - validate and send auth token to email.
     */
    private void handleInitialRegistration(Context ctx, String username, String email, String password, String clientIp) {
        if (email == null || email.trim().isEmpty()) {
            logger.warn("HTTP Register: missing email");
            ctx.status(HttpStatus.BAD_REQUEST)
               .json(new RegisterResponse(false, "Email is required"));
            return;
        }

        if (password == null || password.isEmpty()) {
            logger.warn("HTTP Register: missing password");
            ctx.status(HttpStatus.BAD_REQUEST)
               .json(new RegisterResponse(false, "Password is required"));
            return;
        }

        logger.info("HTTP Register Step 1: attempting registration for user=" + username + ", email=" + email + ", ip=" + clientIp);

        // Spam protection: check if registration is pending for this email or IP
        synchronized (pendingRegistrationsByEmail) {
            if (pendingRegistrationsByEmail.containsKey(email)) {
                PendingRegistration pending = pendingRegistrationsByEmail.get(email);
                long elapsedMinutes = (System.currentTimeMillis() - pending.timestamp) / 60000;

                String message = "A registration for this email address is already pending. " +
                               "Please check your email for the auth token (sent " + elapsedMinutes + " minute(s) ago) " +
                               "or contact an administrator if you did not receive it.";

                logger.warn("HTTP Register: rejected duplicate registration attempt for email=" + email + ", ip=" + clientIp);
                ctx.status(HttpStatus.TOO_MANY_REQUESTS)
                   .json(new RegisterResponse(false, message));
                return;
            }
        }

        synchronized (pendingRegistrationsByIp) {
            if (pendingRegistrationsByIp.containsKey(clientIp)) {
                long timestamp = pendingRegistrationsByIp.get(clientIp);
                long elapsedMinutes = (System.currentTimeMillis() - timestamp) / 60000;

                String message = "A registration from your IP address is already pending. " +
                               "Please complete the previous registration (started " + elapsedMinutes + " minute(s) ago) " +
                               "or contact an administrator if you are experiencing issues.";

                logger.warn("HTTP Register: rejected duplicate registration attempt from ip=" + clientIp);
                ctx.status(HttpStatus.TOO_MANY_REQUESTS)
                   .json(new RegisterResponse(false, message));
                return;
            }
        }

        // Check if authentication is enabled
        if (!managerFactory.configSettings().isAuthenticationActivated()) {
            String message = Session.REGISTRATION_DISABLED_MESSAGE;
            logger.warn("HTTP Register: registration disabled");
            ctx.status(HttpStatus.FORBIDDEN)
               .json(new RegisterResponse(false, message));
            return;
        }

        // Validate username
        String validationError = validateUserName(username);
        if (validationError != null) {
            logger.warn("HTTP Register: invalid username - " + validationError);
            ctx.status(HttpStatus.BAD_REQUEST)
               .json(new RegisterResponse(false, validationError));
            return;
        }

        // Validate password
        validationError = validatePassword(password, username);
        if (validationError != null) {
            logger.warn("HTTP Register: invalid password - " + validationError);
            ctx.status(HttpStatus.BAD_REQUEST)
               .json(new RegisterResponse(false, validationError));
            return;
        }

        // Validate email
        validationError = validateEmail(email);
        if (validationError != null) {
            logger.warn("HTTP Register: invalid email - " + validationError);
            ctx.status(HttpStatus.BAD_REQUEST)
               .json(new RegisterResponse(false, validationError));
            return;
        }

        // Generate 6-digit auth token
        String authToken = String.format("%06d", RANDOM.nextInt(1000000));

        // Create pending registration
        PendingRegistration pendingReg = new PendingRegistration(username, password, email, authToken, clientIp);

        // Store pending registration
        synchronized (pendingRegistrationsByEmail) {
            pendingRegistrationsByEmail.put(email, pendingReg);
            registrationHistory.put(email, System.currentTimeMillis());
        }
        synchronized (pendingRegistrationsByIp) {
            pendingRegistrationsByIp.put(clientIp, System.currentTimeMillis());
        }

        // Send auth token via email (or print in test mode)
        String subject = "XMage Registration Auth Token";
        String text = "Use this auth token to complete registration for username " + username + ": " + authToken + "\n" +
                     "This token is valid until the next server restart.";

        boolean emailSuccess;
        if (testMode) {
            // Test mode: print auth token to logs
            String message = "TEST MODE: Auth token for user=" + username + ", email=" + email + ", token=" + authToken;
            logger.info(message);
            ctx.status(HttpStatus.OK)
               .json(new RegisterResponse(true,
                   "Registration initiated. TEST MODE - Auth token: " + authToken +
                   " (in production, this would be emailed to " + email + ")"));
            emailSuccess = true;
        } else {
            // Production mode: send email
            if (!managerFactory.configSettings().getMailUser().isEmpty()) {
                emailSuccess = managerFactory.mailClient().sendMessage(email, subject, text);
            } else {
                emailSuccess = managerFactory.mailgunClient().sendMessage(email, subject, text);
            }

            if (emailSuccess) {
                String message = "Auth token email sent to " + email + " for user " + username;
                logger.info("HTTP Register Step 1: " + message);
                ctx.status(HttpStatus.OK)
                   .json(new RegisterResponse(true,
                       "Registration initiated. Please check your email for the auth token to complete registration."));
            } else {
                // Email sending failed - clear pending registration
                synchronized (pendingRegistrationsByEmail) {
                    pendingRegistrationsByEmail.remove(email);
                }
                synchronized (pendingRegistrationsByIp) {
                    pendingRegistrationsByIp.remove(clientIp);
                }

                String message = "Email sending failed. Please try another email address or contact an administrator.";
                logger.error("HTTP Register Step 1: email sending failed for user=" + username + ", email=" + email);
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .json(new RegisterResponse(false, message));
            }
        }
    }

    /**
     * Step 2: Handle registration confirmation with auth token - complete registration.
     */
    private void handleConfirmRegistration(Context ctx, String username, String authToken) {
        logger.info("HTTP Register Step 2: attempting to confirm registration for user=" + username);

        // Find pending registration by username
        PendingRegistration pendingReg = null;
        String email = null;

        synchronized (pendingRegistrationsByEmail) {
            for (Map.Entry<String, PendingRegistration> entry : pendingRegistrationsByEmail.entrySet()) {
                if (entry.getValue().username.equals(username)) {
                    pendingReg = entry.getValue();
                    email = entry.getKey();
                    break;
                }
            }
        }

        if (pendingReg == null) {
            logger.warn("HTTP Register Step 2: no pending registration found for user=" + username);
            ctx.status(HttpStatus.BAD_REQUEST)
               .json(new RegisterResponse(false, "No pending registration found for this username. Please initiate registration first."));
            return;
        }

        // Verify auth token
        if (!pendingReg.authToken.equals(authToken)) {
            logger.warn("HTTP Register Step 2: invalid auth token for user=" + username);
            ctx.status(HttpStatus.UNAUTHORIZED)
               .json(new RegisterResponse(false, "Invalid auth token"));
            return;
        }

        // Complete registration - create user in database
        try {
            synchronized (AuthorizedUserRepository.getInstance()) {
                AuthorizedUserRepository.getInstance().add(pendingReg.username, pendingReg.password, pendingReg.email);
            }

            // Clear pending registration
            synchronized (pendingRegistrationsByEmail) {
                pendingRegistrationsByEmail.remove(email);
            }
            synchronized (pendingRegistrationsByIp) {
                pendingRegistrationsByIp.remove(pendingReg.ipAddress);
            }

            String message = "Registration completed for user " + username + ". You can now log in.";
            logger.info("HTTP Register Step 2: " + message);
            ctx.status(HttpStatus.OK)
               .json(new RegisterResponse(true, message));

        } catch (Exception e) {
            logger.error("HTTP Register Step 2: failed to create user " + username, e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
               .json(new RegisterResponse(false, "Failed to complete registration: " + e.getMessage()));
        }
    }

    /**
     * Validate username according to server rules.
     */
    private String validateUserName(String userName) {
        if (userName == null || userName.trim().isEmpty()) {
            return "User name is required";
        }
        if (userName.length() < 3 || userName.length() > 14) {
            return "User name must be between 3 and 14 characters";
        }
        if (!userName.matches("^[a-zA-Z0-9_\\-]+$")) {
            return "User name can only contain letters, numbers, underscore, and hyphen";
        }
        synchronized (AuthorizedUserRepository.getInstance()) {
            if (AuthorizedUserRepository.getInstance().getByName(userName) != null) {
                return "User name already exists";
            }
        }
        return null;
    }

    /**
     * Validate password according to server rules.
     */
    private String validatePassword(String password, String userName) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        if (password.equals(userName)) {
            return "Password cannot be the same as username";
        }
        return null;
    }

    /**
     * Validate email according to server rules.
     */
    private String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email is required";
        }
        // Basic email validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return "Invalid email format";
        }
        synchronized (AuthorizedUserRepository.getInstance()) {
            if (AuthorizedUserRepository.getInstance().getByEmail(email) != null) {
                return "Email address already registered";
            }
        }
        return null;
    }

    /**
     * Request body for registration endpoint.
     * Password is sent via Authorization header (Basic Auth), not in the body.
     */
    public static class RegisterRequest {
        public String email;
        public String authToken; // Optional - for step 2 confirmation
    }

    /**
     * Response body for registration endpoint.
     */
    public static class RegisterResponse {
        public boolean success;
        public String message;

        public RegisterResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
