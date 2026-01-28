package mage.server.ws;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import io.javalin.websocket.*;
import mage.interfaces.MageServer;
import mage.server.DisconnectReason;
import mage.server.MainManagerFactory;
import mage.server.util.ConfigWrapper;
import mage.ws.ProtocolVersion;
import mage.ws.v1.WsProto;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * It is started from {@link mage.server.Main} and shares the same MageServer implementation.
 * @author Jmlundeen
 */
public final class WsServerMain {

    private static final Logger logger = Logger.getLogger(WsServerMain.class);

    private static final WsConnectionRegistry connections = new WsConnectionRegistry();
    private static MainManagerFactory managerFactory; // For session management in disconnect handlers

    private Javalin app;

    public WsServerMain() {
    }

    public synchronized void start(ConfigWrapper config, MainManagerFactory managerFactory, MageServer mageServer, boolean detailsMode) {
        if (app != null) {
            return;
        }

        // Store manager factory for disconnect handling
        WsServerMain.managerFactory = managerFactory;

        // Check if server is in test mode
        boolean testMode = false;
        try {
            testMode = mageServer.getServerState().isTestMode();
        } catch (Exception e) {
            logger.warn("Could not determine test mode status", e);
        }

        logger.info("Starting MAGE WS SERVER protocolVersion=" + ProtocolVersion.getVersion() + (testMode ? " (TEST MODE)" : ""));

        // Keep WS always active and run it in parallel with JBoss remoting.
        // Port is derived deterministically from the main server port.
        int wsPort = config.getPort() + 500;

        WsMessageDispatcher dispatcher = new WsMessageDispatcher(managerFactory, mageServer, detailsMode);
        HttpLoginHandler loginHandler = new HttpLoginHandler(managerFactory, detailsMode);
        HttpRegisterHandler registerHandler = new HttpRegisterHandler(managerFactory, testMode);

        Gson gson = new GsonBuilder().create();
        JsonMapper gsonMapper = new JsonMapper() {
            @NotNull
            @Override
            public String toJsonString(@NotNull Object obj, @NotNull Type type) {
                return gson.toJson(obj, type);
            }

            @NotNull
            @Override
            public <T> T fromJsonString(@NotNull String json, @NotNull Type targetType) {
                return gson.fromJson(json, targetType);
            }
        };


        app = Javalin.create(javalinConfig -> {
            javalinConfig.showJavalinBanner = false;
            javalinConfig.jsonMapper(gsonMapper);
        });

        // HTTP endpoint for Basic Auth login
        app.post("/login", loginHandler::handleLogin);

        // HTTP endpoint for user registration
        app.post("/register", registerHandler::handleRegister);

        // WebSocket endpoint for game communication (requires prior login)
        app.ws("/ws", ws -> {
            ws.onConnect(WsServerMain::onConnect);
            ws.onBinaryMessage(ctx -> onBinary(ctx, dispatcher));
            ws.onClose(WsServerMain::onClose);
            ws.onError(WsServerMain::onError);
        });

        app.start(config.getServerAddress(), wsPort);
        logger.info("HTTP/WS server listening on " + config.getServerAddress() + ':' + wsPort);
        logger.info("  - HTTP login: POST http://" + config.getServerAddress() + ':' + wsPort + "/login");
        logger.info("  - HTTP register: POST http://" + config.getServerAddress() + ':' + wsPort + "/register");
        logger.info("  - WebSocket: ws://" + config.getServerAddress() + ':' + wsPort + "/ws");
    }

    public synchronized void stop() {
        if (app == null) {
            return;
        }
        try {
            app.stop();
        } catch (Exception e) {
            logger.debug("WS stop error", e);
        } finally {
            app = null;
        }
    }

    private static void onConnect(WsConnectContext ctx) {
        // Get sessionId from query parameter (e.g., ws://server/ws?sessionId=xxx)
        String sessionId = ctx.queryParam("sessionId");

        if (sessionId == null || sessionId.isEmpty()) {
            logger.warn("WS connect: no sessionId provided in query parameter");
            try {
                ctx.session.close(1008, "Missing sessionId query parameter");
            } catch (Exception e) {
                logger.debug("Error closing WebSocket session", e);
            }
            return;
        }

        // Store sessionId as attribute for logging and later use
        ctx.attribute("sessionId", sessionId);
        logger.info("WS connect: sessionId=" + sessionId);

        // Create connection wrapper
        WsConnectionRegistry.CloseableConnection connection = createConnection(ctx);
        connections.onSessionIdentified(sessionId, connection);

        // Assign WebSocket callback handler to the session immediately
        if (managerFactory != null) {
            managerFactory.sessionManager().getSession(sessionId).ifPresentOrElse(
                session -> {
                    WebSocketCallbackHandler wsHandler = new WebSocketCallbackHandlerImpl(connection, sessionId);
                    session.setWebSocketCallbackHandler(wsHandler);
                    session.joinChat(sessionId);
                },
                () -> {
                    logger.warn("WS connect: session not found for sessionId=" + sessionId);
                    try {
                        ctx.session.close(1008, "Session not found");
                    } catch (Exception e) {
                        logger.debug("Error closing WebSocket session", e);
                    }
                }
            );
        }
    }

    private static void onClose(WsCloseContext ctx) {
        String sessionId = ctx.attribute("sessionId");
        logger.info("WS close: sessionId=" + (sessionId != null ? sessionId : "unknown"));

        // Handle disconnection similar to MageServerConnectionListener
        if (sessionId != null && managerFactory != null) {
            try {
                // Disconnect with LostConnection reason, keeping tables active for potential reconnect
                managerFactory.sessionManager().disconnect(sessionId, DisconnectReason.LostConnection, true);
            } catch (Exception e) {
                logger.error("Error handling WebSocket disconnect for sessionId=" + sessionId, e);
            }
        }

        connections.onDisconnect(createConnection(ctx));
    }

    private static void onError(WsErrorContext ctx) {
        String sessionId = ctx.attribute("sessionId");
        logger.warn("WS error: sessionId=" + (sessionId != null ? sessionId : "unknown"));

        // Handle error similar to connection close
        if (sessionId != null && managerFactory != null) {
            try {
                managerFactory.sessionManager().disconnect(sessionId, DisconnectReason.LostConnection, true);
            } catch (Exception e) {
                logger.error("Error handling WebSocket error for sessionId=" + sessionId, e);
            }
        }

        connections.onDisconnect(createConnection(ctx));
    }

    private static WsConnectionRegistry.CloseableConnection createConnection(WsContext ctx) {
        return new WsConnectionRegistry.CloseableConnection() {
            @Override
            public void close() {
                try {
                    ctx.session.close();
                } catch (Exception e) {
                    logger.debug("Error closing session", e);
                }
            }

            @Override
            public void send(ByteBuffer data) {
                try {
                    ctx.send(data);
                } catch (Exception e) {
                    logger.debug("Error sending data", e);
                }
            }

            @Override
            public boolean isOpen() {
                return ctx.session.isOpen();
            }
        };
    }

    /**
     * Get the connection registry for broadcasting messages.
     */
    public static WsConnectionRegistry getConnections() {
        return connections;
    }

    private static void onBinary(WsBinaryMessageContext ctx, WsMessageDispatcher dispatcher) {
        ByteBuffer data = ByteBuffer.wrap(ctx.data());
        byte[] bytes = new byte[data.remaining()];
        data.get(bytes);


        WsProto.ServerMessage out;
        try {
            WsProto.ClientMessage in = WsProto.ClientMessage.parseFrom(bytes);
            WsFrameLogger.log(logger, "IN", in.getSessionId(), in.getRequestId(), "Client Message." + in.getPayloadCase(), bytes.length);

            // If the client sends a sessionId in the message, verify it matches the connection sessionId
            String ctxSessionId = ctx.attribute("sessionId");
            if (!in.getSessionId().isEmpty() && !in.getSessionId().equals(ctxSessionId)) {
                logger.warn("WS message sessionId mismatch: connection=" + ctxSessionId + ", message=" + in.getSessionId());
                out = WsProto.ServerMessage.newBuilder()
                        .setProtocolVersion(ProtocolVersion.getVersion())
                        .setRequestId(in.getRequestId())
                        .setSessionId(in.getSessionId())
                        .setError(WsProto.Error.newBuilder()
                                .setCode(WsProto.ErrorCode.INVALID_PROTOCOL_VERSION)
                                .setMessage("SessionId mismatch")
                                .build())
                        .build();
                byte[] outBytes = out.toByteArray();
                WsFrameLogger.logIfLarge(logger, "OUT", ctxSessionId, out.getRequestId(), out.getPayloadCase().name(), outBytes.length);
                ctx.send(ByteBuffer.wrap(outBytes));
                return;
            }

            out = dispatcher.handle(in);
        } catch (Exception e) {
            out = WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId(UUID.randomUUID().toString())
                    .setSessionId("")
                    .setError(WsProto.Error.newBuilder()
                            .setCode(WsProto.ErrorCode.SERVER_ERROR)
                            .setMessage("Invalid protobuf frame")
                            .build())
                    .build();
        }
        if (out == null) {
            // disconnect, no response expected
            return;
        }
        byte[] outBytes = out.toByteArray();
        String sessionId = ctx.attribute("sessionId");
        WsFrameLogger.logIfLarge(logger, "OUT", sessionId == null ? "" : sessionId, out.getRequestId(), out.getPayloadCase().name(), outBytes.length);
        ByteBuffer outBuffer = ByteBuffer.wrap(outBytes);
        ctx.send(outBuffer);
    }
}
