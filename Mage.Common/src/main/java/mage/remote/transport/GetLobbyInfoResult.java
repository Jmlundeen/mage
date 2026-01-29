package mage.remote.transport;


import mage.view.MatchView;
import mage.view.RoomUsersView;
import mage.view.TableView;

import java.util.List;

/**
 * Single lobby snapshot (tables + optional users + optional finished matches).
 * @author jmlundeen
 */
public record GetLobbyInfoResult(List<TableView> tables, RoomUsersView roomUsers,
                                 List<MatchView> finishedMatches) {

}
