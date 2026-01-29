
package mage.remote.interfaces;

import mage.remote.MageRemoteException;
import mage.view.MatchView;
import mage.view.RoomUsersView;
import mage.view.TableView;
import mage.view.UserView;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * @author noxx
 */
public interface ServerState {

    UUID getMainRoomId();

    List<UserView> getUsers();

    RoomUsersView getRoomUsers (UUID roomId) throws MageRemoteException;
    
    List<String> getServerMessages();

    Collection<TableView> getTables(UUID roomId) throws MageRemoteException;

    Collection<MatchView> getFinishedMatches(UUID roomId) throws MageRemoteException;

    String getVersionInfo();

    Boolean isServerReady();

}
