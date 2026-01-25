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

        logger.info("Starting MAGE WS SERVER protocolVersion=" + ProtocolVersion.getVersion());

        // Keep WS always active and run it in parallel with JBoss remoting.
        // Port is derived deterministically from the main server port.
        int wsPort = config.getPort() + 500;

        WsMessageDispatcher dispatcher = new WsMessageDispatcher(managerFactory, mageServer, detailsMode);
        HttpLoginHandler loginHandler = new HttpLoginHandler(managerFactory, detailsMode);

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
        logger.info("WS connect: " + ctx.attribute("sessionId"));
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
            WsFrameLogger.log(logger, "IN", in.getSessionId(), "Client Message." + in.getPayloadCase(), bytes.length);

            // If the client sends a sessionId, and it's the first time we see it on this socket,
            // just stash it as an attribute to help with logging.
            if (ctx.attribute("sessionId") == null && !in.getSessionId().isEmpty()) {
                ctx.attribute("sessionId", in.getSessionId());
                connections.onSessionIdentified(in.getSessionId(), createConnection(ctx));
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
        WsFrameLogger.logIfLarge(logger, "OUT", sessionId == null ? "" : sessionId, out.getPayloadCase().name(), outBytes.length);
        ByteBuffer outBuffer = ByteBuffer.wrap(outBytes);
        ctx.send(outBuffer);
    }
}
