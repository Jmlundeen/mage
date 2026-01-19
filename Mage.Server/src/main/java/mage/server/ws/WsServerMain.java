package mage.server.ws;

import io.javalin.Javalin;
import io.javalin.websocket.WsBinaryMessageContext;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsErrorContext;
import mage.server.MainManagerFactory;
import mage.server.util.ConfigFactory;
import mage.server.util.ConfigWrapper;
import mage.ws.ProtocolVersion;
import mage.ws.v1.WsProto;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Parallel WebSocket server entry point.
 *
 * This does not replace the existing JBoss Remoting transport. It can be run in a separate process.
 */
public final class WsServerMain {

    private static final Logger logger = Logger.getLogger(WsServerMain.class);

    private static final String configPathProp = "xmage.config.path";
    private static final String defaultConfigPath = Paths.get("config", "config.xml").toString();

    private static final WsConnectionRegistry connections = new WsConnectionRegistry();

    private WsServerMain() {
    }

    public static void main(String[] unusedArgs) {
        logger.info("Starting MAGE WS SERVER protocolVersion=" + ProtocolVersion.getVersion());

        final String configPath;
        if (System.getProperty(configPathProp) != null) {
            configPath = System.getProperty(configPathProp);
        } else {
            configPath = defaultConfigPath;
        }

        final ConfigWrapper config = new ConfigWrapper(ConfigFactory.loadFromFile(configPath));

        // Keep WS always active and run it in parallel with JBoss remoting.
        // Port is derived deterministically from the main server port.
        int wsPort = config.getPort() + 500;

        MainManagerFactory managerFactory = new MainManagerFactory(config);

        boolean detailsMode = Boolean.getBoolean("xmage.detailsMode");
        WsMessageDispatcher dispatcher = new WsMessageDispatcher(managerFactory, detailsMode);

        Javalin app = Javalin.create(javalinConfig -> javalinConfig.showJavalinBanner = false);

        app.ws("/ws", ws -> {
            ws.onConnect(WsServerMain::onConnect);
            ws.onBinaryMessage(ctx -> onBinary(ctx, dispatcher));
            ws.onClose(WsServerMain::onClose);
            ws.onError(WsServerMain::onError);
        });

        app.start(config.getServerAddress(), wsPort);
        logger.info("WS listening on ws://" + config.getServerAddress() + ":" + wsPort + "/ws");

        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
    }

    private static void onConnect(WsConnectContext ctx) {
        logger.info("WS connect: " + ctx.getSessionId());
    }

    private static void onClose(WsCloseContext ctx) {
        logger.info("WS close: " + ctx.getSessionId());
        connections.onDisconnect(ctx.session::close);
    }

    private static void onError(WsErrorContext ctx) {
        logger.warn("WS error: " + ctx.getSessionId());
        connections.onDisconnect(ctx.session::close);
    }

    private static void onBinary(WsBinaryMessageContext ctx, WsMessageDispatcher dispatcher) {
        ByteBuffer data = ByteBuffer.wrap(ctx.data());
        byte[] bytes = new byte[data.remaining()];
        data.get(bytes);

        WsFrameLogger.logIfLarge(logger, "IN", "", "ClientMessage", bytes.length);

        WsProto.ServerMessage out;
        try {
            WsProto.ClientMessage in = WsProto.ClientMessage.parseFrom(bytes);

            // If the client sends a sessionId and it's the first time we see it on this socket,
            // just stash it as an attribute to help with logging.
            if (ctx.attribute("sessionId") == null && !in.getSessionId().isEmpty()) {
                ctx.attribute("sessionId", in.getSessionId());
                connections.onSessionIdentified(in.getSessionId(), ctx.session::close);
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

        byte[] outBytes = out.toByteArray();
        String sessionId = ctx.attribute("sessionId");
        WsFrameLogger.logIfLarge(logger, "OUT", sessionId == null ? "" : sessionId, out.getPayloadCase().name(), outBytes.length);

        ctx.send(outBytes);
    }
}
