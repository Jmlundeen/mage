package mage.server.ws;

import mage.interfaces.callback.ClientCallback;
import mage.ws.MessageProto;
import mage.ws.ProtocolVersion;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;

/**
 * WebSocket-based implementation of callback handler for sending ClientCallback messages.
 *
 * @author Jmlundeen
 */
public class WebSocketCallbackHandlerImpl implements WebSocketCallbackHandler {

    private static final Logger logger = Logger.getLogger(WebSocketCallbackHandlerImpl.class);

    private final WsConnectionRegistry.CloseableConnection connection;
    private final String sessionId;
    private volatile boolean connected;

    public WebSocketCallbackHandlerImpl(WsConnectionRegistry.CloseableConnection connection, String sessionId) {
        this.connection = connection;
        this.sessionId = sessionId;
        this.connected = true;
    }

    @Override
    public void sendCallback(ClientCallback callback) {
        if (!connected || !connection.isOpen()) {
            if (logger.isDebugEnabled()) {
                logger.debug("WebSocket session is closed, cannot send callback for sessionId=" + sessionId);
            }
            connected = false;
            return;
        }

        sendCallbackInternal(callback);
    }

    private void sendCallbackInternal(ClientCallback callback) {
        try {
            // Convert ClientCallback to protobuf
            MessageProto.ClientCallback callbackProto = callback.toProto();

            // Wrap in ServerMessage envelope
            MessageProto.ServerMessage message = MessageProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setSessionId(sessionId)
                    .setClientCallback(callbackProto)
                    .build();

            byte[] data = message.toByteArray();

            // Log large frames
            WsFrameLogger.log(logger, "OUT-CB", sessionId, message.getRequestId(), callback.getMethod().name(), data.length);

            // Send via WebSocket
            connection.send(ByteBuffer.wrap(data));

        } catch (Exception e) {
            logger.error("Error sending callback via WebSocket for sessionId=" + sessionId +
                    ", callback=" + callback.getInfo() + ": " + e.getMessage(), e);
            connected = false;
        }
    }

    @Override
    public boolean isConnected() {
        return connected && connection.isOpen();
    }

    @Override
    public void close() {
        connected = false;
        try {
            connection.close();
        } catch (Exception e) {
            logger.error("Error closing WebSocket connection for sessionId=" + sessionId + ": " + e.getMessage(), e);
        }
    }
}
