package mage.remote.transport;

import mage.ws.v1.view.ViewProto;

import java.util.List;

/**
 * Single lobby snapshot (tables + optional users + optional finished matches).
 * @author jmlundeen
 */
public record GetLobbyInfoResult(List<ViewProto.TableView> tables, ViewProto.RoomUsersView roomUsers,
                                 List<ViewProto.MatchView> finishedMatches) {

}
