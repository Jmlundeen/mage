package mage.remote.transport;

import mage.ws.v1.view.ViewProto;

import java.util.List;

/**
 * Event fired when lobby information is updated from the server.
 * This includes tables, finished matches, and room users.
 */
public record LobbyEvent(List<ViewProto.TableView> tables, ViewProto.RoomUsersView roomUsers, List<ViewProto.MatchView> finishedMatches) {

}
