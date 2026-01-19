package mage.remote.transport;

import mage.remote.Connection;
import mage.ws.ProtocolVersion;
import mage.ws.v1.WsProto;
import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.UUID;

/**
 * Dev/test WS+Protobuf transport client.
 *
 * IMPORTANT:
 * - This is a minimal implementation for the incremental migration (hello/auth/ping).
 * - It currently uses a plain TCP socket as a placeholder until the real WebSocket client is wired.
 *   The server-side currently runs Javalin WebSocket, so this class will be adapted to a true WS client.
 */
public class WsClientTransport implements ClientTransport {

    private static final Logger logger = Logger.getLogger(WsClientTransport.class);

    // Placeholder wire: length-prefixed protobuf over TCP.
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    @Override
    public void connect(Connection connection) throws Exception {
        int wsPort = connection.getPort() + 500;
        this.socket = new Socket(connection.getHost(), wsPort);
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            logger.debug("WS transport close error", e);
        } finally {
            socket = null;
            in = null;
            out = null;
        }
    }

    @Override
    public HelloResult hello(String clientName, String clientVersion) throws Exception {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId("")
                .setHello(WsProto.HelloRequest.newBuilder()
                        .setClientName(clientName == null ? "" : clientName)
                        .setClientVersion(clientVersion == null ? "" : clientVersion)
                        .build())
                .build();

        WsProto.ServerMessage res = roundTrip(req);
        if (res.hasError()) {
            throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
        }
        if (!res.hasHello()) {
            throw new IllegalStateException("Unexpected response type");
        }
        return new HelloResult(res.getHello().getServerName(), res.getHello().getServerVersion());
    }

    @Override
    public AuthResult auth(String sessionId, String userName, String password) throws Exception {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setAuth(WsProto.AuthRequest.newBuilder()
                        .setUserName(userName == null ? "" : userName)
                        .setPassword(password == null ? "" : password)
                        .build())
                .build();

        WsProto.ServerMessage res = roundTrip(req);
        if (res.hasError()) {
            if (res.getError().getCode() == WsProto.ErrorCode.AUTH_FAILED) {
                return new AuthResult(false, res.getError().getMessage());
            }
            throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
        }
        if (!res.hasAuth()) {
            throw new IllegalStateException("Unexpected response type");
        }
        return new AuthResult(res.getAuth().getOk(), res.getAuth().getMessage());
    }

    @Override
    public PingResult ping(String sessionId, long clientTimeMillis) throws Exception {
        WsProto.ClientMessage req = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId(newRequestId())
                .setSessionId(sessionId)
                .setPing(WsProto.PingRequest.newBuilder()
                        .setClientTimeMillis(clientTimeMillis)
                        .build())
                .build();

        WsProto.ServerMessage res = roundTrip(req);
        if (res.hasError()) {
            throw new IllegalStateException(res.getError().getCode() + ": " + res.getError().getMessage());
        }
        if (!res.hasPing()) {
            throw new IllegalStateException("Unexpected response type");
        }
        return new PingResult(res.getPing().getServerTimeMillis(), res.getPing().getClientTimeMillis());
    }

    private WsProto.ServerMessage roundTrip(WsProto.ClientMessage req) throws Exception {
        if (socket == null) {
            throw new IllegalStateException("Not connected");
        }

        byte[] data = req.toByteArray();
        out.writeInt(data.length);
        out.write(data);
        out.flush();

        int len = in.readInt();
        byte[] resData = new byte[len];
        in.readFully(resData);
        return WsProto.ServerMessage.parseFrom(resData);
    }

    private static String newRequestId() {
        return UUID.randomUUID().toString();
    }
}
