package mage.players.net;

/**
 * @author ayrat
 */
public enum UserGroup {

    COMPUTER(0),
    PLAYER(1),
    DEFAULT(2),
    MAGE(3),
    ADMIN(7),
    OWNER(15);

    private final int groupId;

    UserGroup(int groupId) {
        this.groupId = groupId;
    }

    public int getGroupId() {
        return this.groupId;
    }

    public static UserGroup fromId(int groupId) {
        for (UserGroup group : UserGroup.values()) {
            if (group.groupId == groupId) {
                return group;
            }
        }
        return DEFAULT;
    }
}
