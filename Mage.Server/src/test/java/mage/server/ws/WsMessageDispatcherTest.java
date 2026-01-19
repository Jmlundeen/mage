package mage.server.ws;

import mage.server.MainManagerFactory;
import mage.server.util.ConfigWrapper;
import mage.server.util.config.*;
import mage.ws.ProtocolVersion;
import mage.ws.v1.WsProto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

public class WsMessageDispatcherTest {

    @Test
    public void shouldRejectInvalidProtocolVersion() {
        WsMessageDispatcher dispatcher = new WsMessageDispatcher(createManagerFactory(), false);

        WsProto.ClientMessage in = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion("some-other")
                .setRequestId("1")
                .setHello(WsProto.HelloRequest.newBuilder().setClientName("test").build())
                .build();

        WsProto.ServerMessage out = dispatcher.handle(in);
        Assertions.assertEquals(WsProto.ServerMessage.PayloadCase.ERROR, out.getPayloadCase());
        Assertions.assertEquals(WsProto.ErrorCode.INVALID_PROTOCOL_VERSION, out.getError().getCode());
    }

    @Test
    public void shouldRejectMissingRequestId() {
        WsMessageDispatcher dispatcher = new WsMessageDispatcher(createManagerFactory(), false);

        WsProto.ClientMessage in = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId("")
                .setHello(WsProto.HelloRequest.newBuilder().build())
                .build();

        WsProto.ServerMessage out = dispatcher.handle(in);
        Assertions.assertEquals(WsProto.ServerMessage.PayloadCase.ERROR, out.getPayloadCase());
        Assertions.assertEquals(WsProto.ErrorCode.MISSING_REQUEST_ID, out.getError().getCode());
    }

    @Test
    public void helloShouldReturnHelloResponse() {
        WsMessageDispatcher dispatcher = new WsMessageDispatcher(createManagerFactory(), false);

        WsProto.ClientMessage in = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId("abc")
                .setHello(WsProto.HelloRequest.newBuilder().setClientName("test").build())
                .build();

        WsProto.ServerMessage out = dispatcher.handle(in);
        Assertions.assertEquals(WsProto.ServerMessage.PayloadCase.HELLO, out.getPayloadCase());
        Assertions.assertEquals("abc", out.getRequestId());
        Assertions.assertEquals(ProtocolVersion.getVersion(), out.getProtocolVersion());
        Assertions.assertFalse(out.getHello().getServerName().isEmpty());
    }

    @Test
    public void shouldRejectMissingPayload() {
        WsMessageDispatcher dispatcher = new WsMessageDispatcher(createManagerFactory(), false);

        WsProto.ClientMessage in = WsProto.ClientMessage.newBuilder()
                .setProtocolVersion(ProtocolVersion.getVersion())
                .setRequestId("missing-payload")
                .build();

        WsProto.ServerMessage out = dispatcher.handle(in);
        Assertions.assertEquals(WsProto.ServerMessage.PayloadCase.ERROR, out.getPayloadCase());
        Assertions.assertEquals(WsProto.ErrorCode.UNKNOWN_MESSAGE_TYPE, out.getError().getCode());
    }

    private static MainManagerFactory createManagerFactory() {
        Config c = new Config();
        // Ensure wrapper getters are safe.
        c.setPlayerTypes(new PlayerTypes());
        c.setGameTypes(new GameTypes());
        c.setTournamentTypes(new TournamentTypes());
        c.setDraftCubes(new DraftCubes());
        c.setDeckTypes(new DeckTypes());

        Server server = new Server();
        server.setServerAddress("127.0.0.1");
        server.setServerName("test-server");
        server.setPort(BigInteger.valueOf(17171));
        server.setSecondaryBindPort(BigInteger.valueOf(0));
        server.setLeasePeriod(BigInteger.valueOf(0));
        server.setSocketWriteTimeout(BigInteger.valueOf(0));
        server.setMaxPoolSize(BigInteger.valueOf(1));
        server.setNumAcceptThreads(BigInteger.valueOf(1));
        server.setBacklogSize(BigInteger.valueOf(1));
        server.setMaxGameThreads(BigInteger.valueOf(1));
        server.setMaxSecondsIdle(BigInteger.valueOf(1));
        server.setMinUserNameLength(BigInteger.valueOf(1));
        server.setMaxUserNameLength(BigInteger.valueOf(20));
        server.setInvalidUserNamePattern("^");
        server.setMinPasswordLength(BigInteger.valueOf(1));
        server.setMaxPasswordLength(BigInteger.valueOf(20));
        server.setMaxAiOpponents("1");
        server.setSaveGameActivated(false);
        server.setAuthenticationActivated(false);
        server.setGoogleAccount("");
        server.setMailgunApiKey("");
        server.setMailgunDomain("");
        server.setMailSmtpHost("");
        server.setMailSmtpPort("");
        server.setMailUser("");
        server.setMailPassword("");
        server.setMailFromAddress("");
        c.setServer(server);

        return new MainManagerFactory(new ConfigWrapper(c));
    }
}
