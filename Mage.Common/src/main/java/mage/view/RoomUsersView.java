

package mage.view;

import mage.ws.view.ViewProto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author LevelX2
 */
public class RoomUsersView implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int numberActiveGames;
    private final int numberGameThreads;
    private final int numberMaxGames;

    private final List<UsersView> usersView;

    public RoomUsersView(List<UsersView> usersView, int numberActiveGames, int numberGameThreads, int numberMaxGames) {

        this.numberActiveGames = numberActiveGames;
        this.numberGameThreads = numberGameThreads;
        this.numberMaxGames = numberMaxGames;
        
        this.usersView = usersView;
    }

    public int getNumberActiveGames() {
        return numberActiveGames;
    }

    public int getNumberGameThreads() {
        return numberGameThreads;
    }

    public int getNumberMaxGames() {
        return numberMaxGames;
    }

    public List<UsersView> getUsersView() {
        return usersView;
    }

    public ViewProto.RoomUsersView toProto() {
        ViewProto.RoomUsersView.Builder builder = ViewProto.RoomUsersView.newBuilder()
                .setNumberActiveGames(numberActiveGames)
                .setNumberGameThreads(numberGameThreads)
                .setNumberMaxGames(numberMaxGames);

        for (UsersView user : usersView) {
            builder.addUsersView(user.toProto());
        }

        return builder.build();
    }

    public static RoomUsersView fromProto(ViewProto.RoomUsersView proto) {
        return new RoomUsersView(proto);
    }

    // Private constructor for fromProto
    private RoomUsersView(ViewProto.RoomUsersView proto) {
        this.numberActiveGames = proto.getNumberActiveGames();
        this.numberGameThreads = proto.getNumberGameThreads();
        this.numberMaxGames = proto.getNumberMaxGames();
        this.usersView = new ArrayList<>();

        for (ViewProto.UsersView userProto : proto.getUsersViewList()) {
            this.usersView.add(UsersView.fromProto(userProto));
        }
    }

}
