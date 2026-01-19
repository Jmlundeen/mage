package mage.server.ws;

import mage.server.MainManagerFactory;
import mage.server.SessionManagerImpl;
import mage.ws.ProtocolVersion;
import mage.ws.v1.WsProto;
import org.apache.log4j.Logger;
import org.jboss.remoting.callback.InvokerCallbackHandler;

/**
 * Minimal dispatcher for the new WS+Protobuf transport.
 *
 * Contract:
 * - Always returns exactly one ServerMessage per input client message.
 * - Never throws (errors are mapped to ServerMessage.error).
 */
public class WsMessageDispatcher {

    private static final Logger logger = Logger.getLogger(WsMessageDispatcher.class);

    private final MainManagerFactory managerFactory;
    private final SessionManagerImpl sessionManager;
    private final boolean detailsMode;

    private final InvokerCallbackHandler noopCallbackHandler = callback -> {
        // WS transport does not use JBoss callbacks.
        // Existing Session code may try to send messages; those will be ignored for now.
    };

    public WsMessageDispatcher(MainManagerFactory managerFactory, boolean detailsMode) {
        this.managerFactory = managerFactory;
        this.sessionManager = (SessionManagerImpl) managerFactory.sessionManager();
        this.detailsMode = detailsMode;
    }

    private static final class MissingSessionException extends RuntimeException {

        private MissingSessionException(String message) {
            super(message);
        }
    }

    public WsProto.ServerMessage handle(WsProto.ClientMessage msg) {
        String requestId = msg.getRequestId();
        String sessionId = msg.getSessionId();

        if (msg.getProtocolVersion().isEmpty() || !ProtocolVersion.equalsStrict(msg.getProtocolVersion())) {
            return error(requestId, sessionId, WsProto.ErrorCode.INVALID_PROTOCOL_VERSION,
                    "Invalid protocolVersion. ServerProtocol=" + ProtocolVersion.getVersion());
        }

        if (requestId.trim().isEmpty()) {
            return error("", sessionId, WsProto.ErrorCode.MISSING_REQUEST_ID, "Missing requestId");
        }

        try {
            switch (msg.getPayloadCase()) {
                case HELLO:
                    return hello(requestId, sessionId, msg.getHello());
                case PING:
                    requireSession(sessionId);
                    return ping(requestId, sessionId, msg.getPing());
                case AUTH:
                    requireSession(sessionId);
                    return auth(requestId, sessionId, msg.getAuth());
                case PAYLOAD_NOT_SET:
                default:
                    return error(requestId, sessionId, WsProto.ErrorCode.UNKNOWN_MESSAGE_TYPE, "Unknown message type");
            }
        } catch (MissingSessionException e) {
            return error(requestId, sessionId, WsProto.ErrorCode.MISSING_SESSION_ID, e.getMessage());
        } catch (Exception e) {
            logger.error("WS dispatch error", e);
            return error(requestId, sessionId, WsProto.ErrorCode.SERVER_ERROR, "Server error");
        }
    }

    private WsProto.ServerMessage hello(String requestId, String sessionId, WsProto.HelloRequest req) {
        return WsProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId)
                .setSessionId(sessionId == null ? "" : sessionId)
                .setHello(WsProto.HelloResponse.newBuilder()
                        .setServerName(managerFactory.configSettings().getServerName())
                        .setServerVersion(ProtocolVersion.getVersion())
                        .build())
                .build();
    }

    private WsProto.ServerMessage ping(String requestId, String sessionId, WsProto.PingRequest req) {
        return WsProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId)
                .setSessionId(sessionId)
                .setPing(WsProto.PingResponse.newBuilder()
                        .setServerTimeMillis(System.currentTimeMillis())
                        .setClientTimeMillis(req.getClientTimeMillis())
                        .build())
                .build();
    }

    private WsProto.ServerMessage auth(String requestId, String sessionId, WsProto.AuthRequest req) {
        // For now: implement a minimal login call path.
        // Ensure a server-side session exists for that sessionId.
        if (!sessionManager.getSession(sessionId).isPresent()) {
            sessionManager.createSession(sessionId, noopCallbackHandler);
        }

        boolean ok;
        try {
            ok = sessionManager.connectUser(sessionId, "", req.getUserName(), req.getPassword(), "ws", detailsMode);
        } catch (Exception e) {
            logger.warn("Auth failed", e);
            ok = false;
        }

        return WsProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId)
                .setSessionId(sessionId)
                .setAuth(WsProto.AuthResponse.newBuilder()
                        .setOk(ok)
                        .setMessage(ok ? "OK" : "Auth failed")
                        .build())
                .build();
    }

    private static WsProto.ServerMessage error(String requestId, String sessionId, WsProto.ErrorCode code, String message) {
        return WsProto.ServerMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(requestId == null ? "" : requestId)
                .setSessionId(sessionId == null ? "" : sessionId)
                .setError(WsProto.Error.newBuilder()
                        .setCode(code)
                        .setMessage(message == null ? "" : message)
                        .build())
                .build();
    }

    private static void requireSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new MissingSessionException("Missing sessionId");
        }
    }
}
