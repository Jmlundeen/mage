package mage.server.ws;

import mage.ws.ProtocolVersion;
import mage.ws.v1.WsProto;
import mage.ws.v1.view.ViewProto;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Broadcasts lobby updates to all connected WebSocket clients.
 * @author Jmlundeen
 */
public class LobbyBroadcaster {

    private static final Logger logger = Logger.getLogger(LobbyBroadcaster.class);

    /**
     * Broadcast lobby information to all connected clients.
     *
     * @param tables The current tables
     * @param roomUsers The room users
     * @param finishedMatches The finished matches
     */
    public static void broadcastLobbyUpdate(
            List<ViewProto.TableView> tables,
            ViewProto.RoomUsersView roomUsers,
            List<ViewProto.MatchView> finishedMatches) {

        try {
            WsConnectionRegistry connections = WsServerMain.getConnections();
            if (connections.getConnectionCount() == 0) {
                return;
            }

            // Build lobby info response
            WsProto.LobbyInfoResponse.Builder lobbyInfoBuilder = WsProto.LobbyInfoResponse.newBuilder();

            if (tables != null && !tables.isEmpty()) {
                lobbyInfoBuilder.addAllTables(tables);
            }

            if (roomUsers != null) {
                lobbyInfoBuilder.setRoomUsers(roomUsers);
            }

            if (finishedMatches != null && !finishedMatches.isEmpty()) {
                lobbyInfoBuilder.addAllFinishedMatches(finishedMatches);
            }

            // Build server message (no requestId means it's a push message)
            WsProto.ServerMessage message = WsProto.ServerMessage.newBuilder()
                    .setProtocolVersion(ProtocolVersion.getVersion())
                    .setRequestId("") // Empty requestId indicates push message
                    .setSessionId("")
                    .setLobbyGetInfo(lobbyInfoBuilder.build())
                    .build();

            // Broadcast to all connected clients
            connections.broadcast(message);

            if (logger.isDebugEnabled()) {
                int tableCount = tables != null ? tables.size() : 0;
                int matchCount = finishedMatches != null ? finishedMatches.size() : 0;
                logger.debug("Broadcasted lobby update: " + tableCount + " tables, " + matchCount + " finished matches");
            }
        } catch (Exception e) {
            logger.error("Failed to broadcast lobby update", e);
        }
    }
}
