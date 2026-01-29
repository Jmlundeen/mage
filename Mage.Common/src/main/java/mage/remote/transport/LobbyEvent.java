package mage.remote.transport;

import mage.view.MatchView;
import mage.view.RoomUsersView;
import mage.view.TableView;

import java.util.List;

/**
 * Event fired when lobby information is updated from the server.
 * This includes tables, finished matches, and room users.
 */
public record LobbyEvent(List<TableView> tables, RoomUsersView roomUsers, List<MatchView> finishedMatches) {

}
