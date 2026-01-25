package mage.remote;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.javalin.http.HttpStatus;
import mage.MageException;
import mage.cards.decks.DeckCardLists;
import mage.constants.ManaType;
import mage.constants.PlayerAction;
import mage.game.GameException;
import mage.game.match.MatchOptions;
import mage.game.tournament.TournamentOptions;
import mage.interfaces.MageClient;
import mage.interfaces.ServerState;
import mage.players.PlayerType;
import mage.players.net.UserData;
import mage.remote.transport.GetLobbyInfoResult;
import mage.remote.transport.LobbyEventBus;
import mage.remote.transport.WsClientTransport;
import mage.view.DraftPickView;
import mage.view.TournamentView;
import mage.ws.v1.view.ViewProto;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Network: client side session over WS+Protobuf.
 */
public class WsSessionImpl implements Session {

    private static final Logger logger = Logger.getLogger(WsSessionImpl.class);
    private static final Gson gson = new Gson();

    private enum SessionState {
        DISCONNECTED, CONNECTED, CONNECTING, DISCONNECTING, SERVER_STARTING
    }
    private static final int PING_CYCLES = 10;

    private final MageClient client;

    private Connection connection;
    private String sessionId = "";
    private String restoreSessionId = "";
    private String lastError = "";
    private String lastPingInfo = "";
    private LinkedList<Long> pingTimes = new LinkedList<>();

    private WsClientTransport transport;

    private ServerState serverState;
    private SessionState sessionState = SessionState.DISCONNECTED;

    // One-time warnings for unsupported calls.
    private final Set<String> warned = Collections.synchronizedSet(new HashSet<>());

    public WsSessionImpl(MageClient client) {
        this.client = client;
    }

    // --- helpers

    private void warnUnsupported(String feature) {
        if (feature == null || feature.isEmpty()) {
            feature = "<unknown>";
        }
        if (warned.add(feature)) {
            logger.warn("WS session: unsupported feature (no-op): " + feature);
        }
    }

    private void setLastError(String error) {
        lastError = error == null ? "" : error;
    }

    // --- Connect

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void setRestoreSessionId(String restoreSessionId) {
        this.restoreSessionId = restoreSessionId == null ? "" : restoreSessionId;
    }

    @Override
    public String getLastError() {
        return lastError;
    }

    @Override
    public String getServerHost() {
        return isConnected() && connection != null ? connection.getHost() : "";
    }

    @Override
    public synchronized boolean connectStart(Connection connection) {
        this.connection = connection;
        setLastError("");

        if (this.connection == null) {
            setLastError("No connection");
            return false;
        }

        // Close any existing connection
        if (isConnected()) {
            connectStop(true, false);
        }

        try {
            // Step 1: Authenticate via HTTP Basic Auth to get session ID
            logger.info("HTTP Login: authenticating user " + connection.getUsername() + " at " + connection.getHost() + ':' + connection.getPort());

            if (!httpLogin(connection)) {
                logger.warn("HTTP Login: failed");
                return false;
            }

            logger.info("HTTP Login: success, received sessionId=" + sessionId);

            // Step 2: Connect WebSocket with the session ID
            logger.info("WS Connect: establishing WebSocket connection");

            if (!connectWebSocket(connection)) {
                logger.warn("WS Connect: failed");
                return false;
            }

            logger.info("WS Connect: success");

            serverState = getServerState();
            if (serverState == null) {
                handleMageException(new MageException("Failed to get server state after WebSocket connection"));
            }

            if (client.getVersion().compareTo(serverState.getVersion()) != 0) {
                handleMageException(new MageVersionException(client.getVersion(), serverState.getVersion()));
            }

            logger.info("Login complete for user " + connection.getUsername());
            client.connected(connection.getUsername() + '@' + connection.getHost() + ':' + connection.getPort() + ' ');
            return true;
        } catch (Exception e) {
            logger.error("Connect failed", e);
            setLastError(e.getMessage());
            return false;
        }
    }

    /**
     * Step 1: Authenticate via HTTP POST with Basic Auth.
     * Returns session ID from server.
     */
    private boolean httpLogin(Connection connection) {
        try {
            // Calculate HTTP port (WS server port = main port + 500)
            int httpPort = connection.getPort() + 500;
            String loginUrl = "http://" + connection.getHost() + ":" + httpPort + "/login";

            if (!restoreSessionId.isEmpty()) {
                loginUrl += "?restoreSessionId=" + restoreSessionId;
            }

            // Create Basic Auth header
            String credentials = connection.getUsername() + ":" + connection.getPassword();
            String base64Credentials = java.util.Base64.getEncoder().encodeToString(credentials.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String authHeader = "Basic " + base64Credentials;

            // Make HTTP POST request
            URL url = new URL(loginUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", authHeader);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpStatus.OK.getCode()) {
                InputStream errorStream = conn.getErrorStream();
                if (errorStream != null) {
                    String error = readStream(errorStream);
                    setLastError("HTTP Login failed: " + error);
                } else {
                    setLastError("HTTP Login failed with code: " + responseCode);
                }
                return false;
            }

            // Parse response JSON
            InputStream inputStream = conn.getInputStream();
            String response = readStream(inputStream);

            // Simple JSON parsing (looking for "sessionId":"xxx")
            JsonObject json = gson.fromJson(response, JsonObject.class);
            if (!json.has("sessionId")) {
                setLastError("HTTP Login: missing sessionId in response");
                return false;
            }

            sessionId = json.get("sessionId").getAsString();

            if (sessionId == null || sessionId.isEmpty()) {
                setLastError("HTTP Login: empty sessionId");
                return false;
            }

            return true;
        } catch (Exception e) {
            logger.error("HTTP Login error", e);
            setLastError("HTTP Login error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Step 2: Connect WebSocket using the session ID from HTTP login.
     */
    private boolean connectWebSocket(Connection connection) {
        try {
            // Create new transport
            if (transport == null) {
                transport = new WsClientTransport();
            }

            // Connect WebSocket
            transport.connect(connection);

            serverState = transport.getServerState(sessionId);
            if (serverState == null) {
                handleThrowable(new MageException("Failed to get server state after WebSocket connection"));
                setLastError("WebSocket: failed to get server state");
                return false;
            }
            if (client.getVersion().compareTo(serverState.getVersion()) != 0) {
                handleThrowable(new MageVersionException(client.getVersion(), serverState.getVersion()));
                setLastError("Version mismatch: client " + client.getVersion() + " vs server " + serverState.getVersion());
                return false;
            }

            logger.info("WS Connect: WebSocket established with sessionId=" + sessionId);
            return true;
        } catch (Exception e) {
            logger.error("WebSocket connection error", e);
            setLastError("WebSocket error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get server state from server.
     */
    private ServerState getServerState() throws MageException {
        try {
            return transport.getServerState(sessionId);
        } catch (Exception e) {
            handleThrowable(e);
            throw new MageException("Failed to get server state: " + e.getMessage());
        }
    }

    /**
     * Helper method to read InputStream to String.
     */
    private String readStream(InputStream stream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = stream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8);
    }

    @Override
    public boolean connectAbort() {
        warnUnsupported("connectAbort");
        return true;
    }

    @Override
    public synchronized void connectStop(boolean askForReconnect, boolean keepMySessionActive) {
        if (isConnected()) {
            logger.info("Disconnecting...");
            sessionState = SessionState.DISCONNECTING;
        }
        if (connection == null || sessionState == SessionState.DISCONNECTED) {
            return;
        }

        try {
            if (transport != null && transport.isConnected()) {
                transport.disconnect();
            }
        } catch (Throwable ex) {
            logger.fatal("WS disconnect FAIL", ex);
        }
        if (sessionState == SessionState.DISCONNECTING || sessionState == SessionState.CONNECTING) {
            sessionState = SessionState.DISCONNECTED;
            serverState = null;
            logger.info("Disconnecting DONE");
            if (askForReconnect) {
                client.showError("Network error. Can't connect to " + connection.getHost());
            }
            client.disconnected(askForReconnect, keepMySessionActive);
            pingTimes.clear();
        }
        if (transport != null) {
            transport = null;
        }
    }

    @Override
    public void connectReconnect(Throwable throwable) {
        warnUnsupported("connectReconnect");
        client.disconnected(true, true);
    }

    @Override
    public void ping() {
        try {
            if (!isConnected() || transport == null) {
                return;
            }
            long startTime = System.nanoTime();
            if (!transport.ping(sessionId, lastPingInfo)) {
                logger.error("Ping failed: " + this.getUserName() + " Session: " + sessionId + " to MAGE server at " + connection.getHost() + ":" + connection.getPort());
                throw new MageException("Ping failed");
            }
            pingTimes.add(System.nanoTime() - startTime);
            long milliseconds = TimeUnit.MILLISECONDS.convert(pingTimes.getLast(), TimeUnit.NANOSECONDS);
            String lastPing = milliseconds > 0 ? milliseconds + " ms" : "<1 ms";
            if (pingTimes.size() > PING_CYCLES) {
                pingTimes.poll();
            }
            long sum = 0;
            for (Long time : pingTimes) {
                sum += time;
            }
            milliseconds = TimeUnit.MILLISECONDS.convert(sum / pingTimes.size(), TimeUnit.NANOSECONDS);
            lastPingInfo = lastPing + " (avg " + (milliseconds > 0 ? milliseconds + " ms" : "<1 ms") + ")";
        } catch (MageException e) {
            handleMageException(e);
            connectStop(true, true);
        } catch (Throwable t) {
            handleThrowable(t);
            connectStop(true, true);
        }
    }

    @Override
    public boolean isConnected() {
        return transport != null && connection != null && transport.isConnected();
    }

    // --- ServerState / GameTypes

    @Override
    public String getVersionInfo() {
        if (serverState != null) {
            return serverState.getVersion().toString();
        } else {
            return "<no server state>";
        }
    }

    @Override
    public Boolean isServerReady() {
        // Is server works fine, possible use cases:
        // - client connected by network, but can't process register/login process due errors like wrong username
        // - client connected to broken server that has a wrong config or broken/miss libraries
        return isConnected() && serverState != null && !serverState.getGameTypes().isEmpty();
    }

    @Override
    public PlayerType[] getPlayerTypes() {
        warnUnsupported("getPlayerTypes");
        return new PlayerType[0];
    }

    @Override
    public List<ViewProto.GameTypeView> getGameTypes() {
        return serverState.getGameTypes();
    }

    @Override
    public List<ViewProto.GameTypeView> getTournamentGameTypes() {
        return serverState.getTournamentGameTypes();
    }

    @Override
    public String[] getDeckTypes() {
        warnUnsupported("getDeckTypes");
        return new String[0];
    }

    @Override
    public String[] getDraftCubes() {
        warnUnsupported("getDraftCubes");
        return new String[0];
    }

    @Override
    public List<ViewProto.TournamentTypeView> getTournamentTypes() {
        warnUnsupported("getTournamentTypes");
        return new ArrayList<>();
    }

    @Override
    public boolean isTestMode() {
        if (serverState != null) {
            return serverState.isTestMode();
        }
        return false;
    }

    @Override
    public boolean cheatShow(UUID gameId, UUID playerId) {
        return false;
    }

    // --- Lobby bootstrap

    @Override
    public UUID getMainRoomId() {
        try {
            if (!isConnected() || transport == null) {
                throw new MageException("Not connected");
            }
            return transport.getMainRoomId(sessionId);
        } catch (Exception e) {
            logger.debug("WS getMainRoomId failed", e);
            client.showError("Failed to get main room ID: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<ViewProto.UserView> getUsers() {
        return Collections.emptyList();
    }

    // --- ChatSession (no-op)

    @Override
    public Optional<UUID> getRoomChatId(UUID roomId) {
        try {
            if (!isConnected()) {
                return Optional.empty();
            }
            return Optional.of(transport.getRoomChatId(sessionId, roomId));
        } catch (Exception e) {
            handleThrowable(e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<UUID> getTableChatId(UUID tableId) {
        warnUnsupported("getTableChatId");
        return Optional.empty();
    }

    @Override
    public Optional<UUID> getGameChatId(UUID gameId) {
        warnUnsupported("getGameChatId");
        return Optional.empty();
    }

    @Override
    public Optional<UUID> getTournamentChatId(UUID tournamentId) {
        warnUnsupported("getTournamentChatId");
        return Optional.empty();
    }

    @Override
    public boolean joinChat(UUID chatId) {
        try {
            if (!isConnected()) {
                return false;
            }
            return transport.joinChat(sessionId, chatId);
        } catch (Exception e) {
            handleThrowable(e);
        }
        return false;
    }

    @Override
    public boolean leaveChat(UUID chatId) {
        try {
            if (!isConnected()) {
                return false;
            }
            return transport.leaveChat(sessionId, chatId);
        } catch (Exception e) {
            handleThrowable(e);
        }
        return false;
    }

    @Override
    public boolean sendChatMessage(UUID chatId, String message) {
        warnUnsupported("sendChatMessage");
        return false;
    }

    @Override
    public boolean sendBroadcastMessage(String message) {
        warnUnsupported("sendBroadcastMessage");
        return false;
    }

    // --- ClientData

    @Override
    public boolean updatePreferencesForServer(UserData userData) {
        try {
            if (!isConnected() || transport == null) {
                throw new MageException("Not connected");
            }
            transport.sendUserData(sessionId, userData, client.getVersion(), connection.getUserIdStr());
            return true;
        } catch (MageException e) {
            handleMageException(e);
            return false;
        } catch (Exception e) {
            handleThrowable(e);
            return false;
        }
    }

    @Override
    public boolean isJsonLogActive() {
        warnUnsupported("isJsonLogActive");
        return false;
    }

    @Override
    public void setJsonLogActive(boolean jsonLogActive) {
        warnUnsupported("setJsonLogActive");
    }

    @Override
    public String getUserName() {
        return connection != null && connection.getUsername() != null ? connection.getUsername() : "";
    }

    // --- Feedback

    @Override
    public boolean sendFeedback(String title, String type, String message, String email) {
        warnUnsupported("sendFeedback");
        return false;
    }

    // --- PlayerActions / GamePlay / Replays (no-op)

    @Override
    public boolean sendPlayerUUID(UUID gameId, UUID data) {
        warnUnsupported("sendPlayerUUID");
        return false;
    }

    @Override
    public boolean sendPlayerBoolean(UUID gameId, boolean data) {
        warnUnsupported("sendPlayerBoolean");
        return false;
    }

    @Override
    public boolean sendPlayerInteger(UUID gameId, int data) {
        warnUnsupported("sendPlayerInteger");
        return false;
    }

    @Override
    public boolean sendPlayerString(UUID gameId, String data) {
        warnUnsupported("sendPlayerString");
        return false;
    }

    @Override
    public boolean sendPlayerManaType(UUID gameId, UUID playerId, ManaType data) {
        warnUnsupported("sendPlayerManaType");
        return false;
    }

    @Override
    public boolean quitMatch(UUID gameId) {
        warnUnsupported("quitMatch");
        return false;
    }

    @Override
    public boolean quitTournament(UUID tournamentId) {
        warnUnsupported("quitTournament");
        return false;
    }

    @Override
    public boolean quitDraft(UUID draftId) {
        warnUnsupported("quitDraft");
        return false;
    }

    @Override
    public boolean submitDeck(UUID tableId, DeckCardLists deck) {
        warnUnsupported("submitDeck");
        return false;
    }

    @Override
    public boolean updateDeck(UUID tableId, DeckCardLists deck) {
        warnUnsupported("updateDeck");
        return false;
    }

    @Override
    public boolean setBoosterLoaded(UUID draftId) {
        warnUnsupported("setBoosterLoaded");
        return false;
    }

    @Override
    public DraftPickView sendCardPick(UUID draftId, UUID cardId, Set<UUID> hiddenCards) {
        warnUnsupported("sendCardPick");
        return null;
    }

    @Override
    public DraftPickView sendCardMark(UUID draftId, UUID cardId) {
        warnUnsupported("sendCardMark");
        return null;
    }

    @Override
    public boolean sendPlayerAction(PlayerAction passPriorityAction, UUID gameId, Object data) {
        warnUnsupported("sendPlayerAction");
        return false;
    }

    @Override
    public ViewProto.TableView createTable(UUID roomId, MatchOptions matchOptions) {
        warnUnsupported("createTable");
        return null;
    }

    @Override
    public ViewProto.TableView createTournamentTable(UUID roomId, TournamentOptions tournamentOptions) {
        warnUnsupported("createTournamentTable");
        return null;
    }

    @Override
    public boolean removeTable(UUID roomId, UUID tableId) {
        warnUnsupported("removeTable");
        return false;
    }

    @Override
    public boolean removeTable(UUID tableId) {
        warnUnsupported("removeTable");
        return false;
    }

    @Override
    public boolean joinGame(UUID gameId) {
        warnUnsupported("joinGame");
        return false;
    }

    @Override
    public boolean joinDraft(UUID draftId) {
        warnUnsupported("joinDraft");
        return false;
    }

    @Override
    public boolean joinTournament(UUID tournamentId) {
        warnUnsupported("joinTournament");
        return false;
    }

    @Override
    public boolean leaveTable(UUID roomId, UUID tableId) {
        warnUnsupported("leaveTable");
        return false;
    }

    @Override
    public boolean swapSeats(UUID roomId, UUID tableId, int seatNum1, int seatNum2) {
        warnUnsupported("swapSeats");
        return false;
    }

    @Override
    public boolean startTournament(UUID roomId, UUID tableId) {
        warnUnsupported("startTournament");
        return false;
    }

    @Override
    public boolean startMatch(UUID roomId, UUID tableId) {
        warnUnsupported("startMatch");
        return false;
    }

    @Override
    public boolean watchGame(UUID gameId) {
        warnUnsupported("watchGame");
        return false;
    }

    @Override
    public boolean replayGame(UUID gameId) {
        warnUnsupported("replayGame");
        return false;
    }

    @Override
    public boolean stopWatching(UUID gameId) {
        warnUnsupported("stopWatching");
        return false;
    }

    @Override
    public boolean startReplay(UUID gameId) {
        warnUnsupported("startReplay");
        return false;
    }

    @Override
    public boolean stopReplay(UUID gameId) {
        warnUnsupported("stopReplay");
        return false;
    }

    @Override
    public boolean nextPlay(UUID gameId) {
        warnUnsupported("nextPlay");
        return false;
    }

    @Override
    public boolean previousPlay(UUID gameId) {
        warnUnsupported("previousPlay");
        return false;
    }

    @Override
    public boolean skipForward(UUID gameId, int moves) {
        warnUnsupported("skipForward");
        return false;
    }

    @Override
    public Optional<ViewProto.TableView> getTable(UUID roomId, UUID tableId) {
        warnUnsupported("getTable");
        return Optional.empty();
    }

    @Override
    public TournamentView getTournament(UUID tournamentId) throws MageRemoteException {
        warnUnsupported("getTournament");
        return null;
    }

    @Override
    public boolean isTableOwner(UUID roomId, UUID tableId) {
        warnUnsupported("isTableOwner");
        return false;
    }

    @Override
    public boolean watchTable(UUID roomId, UUID tableId) {
        warnUnsupported("watchTable");
        return false;
    }

    @Override
    public boolean watchTournamentTable(UUID tableId) {
        warnUnsupported("watchTournamentTable");
        return false;
    }

    @Override
    public boolean joinTable(UUID roomId, UUID tableId, String playerName, PlayerType playerType, int skill, DeckCardLists deckList, String password) {
        warnUnsupported("joinTable");
        return false;
    }

    @Override
    public boolean joinTournamentTable(UUID roomId, UUID tableId, String playerName, PlayerType playerType, int skill, DeckCardLists deckList, String password) {
        warnUnsupported("joinTournamentTable");
        return false;
    }

    @Override
    public Collection<ViewProto.TableView> getTables(UUID roomId) throws MageRemoteException {
        try {
            if (!isConnected() || transport == null) {
                throw new MageException("Not connected");
            }
            return transport.lobbyGetTables(sessionId, roomId);
        } catch (MageException e) {
            handleMageException(e);
            throw new MageRemoteException();
        } catch (Exception e) {
            handleThrowable(e);
            throw new MageRemoteException();
        }
    }

    @Override
    public Collection<ViewProto.MatchView> getFinishedMatches(UUID roomId) throws MageRemoteException {
        try {
            if (!isConnected() || transport == null) {
                throw new MageException("Not connected");
            }
            return transport.getFinishedMatches(sessionId, roomId);
        } catch (MageException e) {
            handleMageException(e);
            throw new MageRemoteException();
        } catch (Exception e) {
            handleThrowable(e);
            throw new MageRemoteException();
        }
    }

    @Override
    public ViewProto.RoomUsersView getRoomUsers(UUID roomId) throws MageRemoteException {
        try {
            if (!isConnected() || transport == null) {
                throw new MageException("Not connected");
            }
            return transport.getRoomUsers(sessionId, roomId);
        } catch (MageException e) {
            handleMageException(e);
            throw new MageRemoteException();
        } catch (Exception e) {
            handleThrowable(e);
            throw new MageRemoteException();
        }
    }

    @Override
    public List<String> getServerMessages() {
        try {
            return transport.getServerMessages(sessionId);
        } catch (Exception e) {
            handleMageException(new MageException("Failed to get server messages: " + e.getMessage()));
            return Collections.emptyList();
        }
    }

    @Override
    public boolean sendAdminDisconnectUser(String userSessionId) {
        warnUnsupported("sendAdminDisconnectUser");
        return false;
    }

    @Override
    public boolean sendAdminEndUserSession(String userSessionId) {
        warnUnsupported("sendAdminEndUserSession");
        return false;
    }

    @Override
    public boolean sendAdminMuteUserChat(String userName, long durationMinute) {
        warnUnsupported("sendAdminMuteUserChat");
        return false;
    }

    @Override
    public boolean sendAdminActivateUser(String userName, boolean active) {
        warnUnsupported("sendAdminActivateUser");
        return false;
    }

    @Override
    public boolean sendAdminToggleActivateUser(String userName) {
        warnUnsupported("sendAdminToggleActivateUser");
        return false;
    }

    @Override
    public boolean sendAdminLockUser(String userName, long durationMinute) {
        warnUnsupported("sendAdminLockUser");
        return false;
    }

    @Override
    public void appendJsonLog(ActionData actionData) {
        warnUnsupported("appendJsonLog");
    }

    @Override
    public synchronized boolean sendAuthRegister(Connection connection) {
        warnUnsupported("sendAuthRegister");
        return false;
    }

    @Override
    public synchronized boolean sendAuthSendTokenToEmail(Connection connection) {
        warnUnsupported("sendAuthSendTokenToEmail");
        return false;
    }

    @Override
    public synchronized boolean sendAuthResetPassword(Connection connection) {
        warnUnsupported("sendAuthResetPassword");
        return false;
    }

    public List<ViewProto.TableView> lobbyGetTables(UUID roomId) {
        try {
            if (!isConnected() || transport == null) {
                return Collections.emptyList();
            }
            return transport.lobbyGetTables(sessionId, roomId);
        } catch (Exception e) {
            logger.debug("WS lobbyGetTables failed", e);
            return Collections.emptyList();
        }
    }

    public List<ViewProto.MatchView> lobbyGetFinishedMatches(UUID roomId) {
        try {
            if (!isConnected() || transport == null) {
                return Collections.emptyList();
            }
            return transport.getFinishedMatches(sessionId, roomId);
        } catch (Exception e) {
            logger.debug("WS lobbyGetFinishedMatches failed", e);
            return Collections.emptyList();
        }
    }

    public ViewProto.RoomUsersView lobbyGetRoomUsers(UUID roomId) {
        try {
            if (!isConnected() || transport == null) {
                return null;
            }
            return transport.getRoomUsers(sessionId, roomId);
        } catch (Exception e) {
            logger.debug("WS lobbyGetRoomUsers failed", e);
            return null;
        }
    }

    private void handleThrowable(Throwable t) {

        // ignore interrupted exceptions -- it's connection problem or user's close
        if (t instanceof InterruptedException) {
            //logger.error("Connection error: was interrupted", t);
            Thread.currentThread().interrupt();
            return;
        }

        if (t instanceof RuntimeException) {
            RuntimeException re = (RuntimeException) t;
            if (t.getCause() instanceof InterruptedException) {
                //logger.error("Connection error: was interrupted by runtime exception", t.getCause());
                Thread.currentThread().interrupt();
                return;
            }
        }

        logger.fatal("Connection error: other", t);
    }

    private void handleMageException(MageException ex) {
        logger.fatal("Server error", ex);
        client.showError("Server error: " + ex.getMessage());
    }


    private void handleGameException(GameException ex) {
        logger.warn(ex.getMessage());
        client.showError("Game error: " + ex.getMessage());
    }

    public GetLobbyInfoResult lobbyGetInfo(UUID roomId, boolean includeFinishedMatches, boolean includeRoomUsers) {
        try {
            if (!isConnected() || transport == null) {
                return new GetLobbyInfoResult(Collections.emptyList(), ViewProto.RoomUsersView.getDefaultInstance(), Collections.emptyList());
            }
            return transport.lobbyGetInfo(sessionId, roomId, includeFinishedMatches, includeRoomUsers);
        } catch (Exception e) {
            logger.debug("WS lobbyGetInfo failed", e);
            return new GetLobbyInfoResult(Collections.emptyList(), ViewProto.RoomUsersView.getDefaultInstance(), Collections.emptyList());
        }
    }

    /**
     * Get the lobby event bus for subscribing to lobby updates from the server.
     * UI components can subscribe to receive push notifications when lobby state changes.
     *
     * @return The lobby event bus, or null if not connected
     */
    public LobbyEventBus getLobbyEventBus() {
        return transport != null ? transport.getLobbyEventBus() : null;
    }
}
