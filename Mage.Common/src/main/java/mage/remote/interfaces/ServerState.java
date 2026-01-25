
package mage.remote.interfaces;

import mage.remote.MageRemoteException;
import mage.ws.v1.view.ViewProto;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * @author noxx
 */
public interface ServerState {

    UUID getMainRoomId();

    List<ViewProto.UserView> getUsers();

    ViewProto.RoomUsersView getRoomUsers (UUID roomId) throws MageRemoteException;
    
    List<String> getServerMessages();

    Collection<ViewProto.TableView> getTables(UUID roomId) throws MageRemoteException;

    Collection<ViewProto.MatchView> getFinishedMatches(UUID roomId) throws MageRemoteException;

    String getVersionInfo();

    Boolean isServerReady();

}
