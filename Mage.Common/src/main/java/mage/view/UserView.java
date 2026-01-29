
package mage.view;

import mage.ws.v1.view.ViewProto;

import java.io.Serializable;
import java.util.Date;

/**
 * Admin Console View
 *
 * @author BetaSteward_at_googlemail.com
 */
public class UserView implements Serializable {

    private final String userName;
    private final String host;
    private final String sessionId;
    private final Date timeConnected;
    private final Date lastActivity;
    private final String gameInfo;
    private final String userState;
    private final Date muteChatUntil;
    private final String clientVersion;
    private final String email;
    private final String userIdStr;

    public UserView(String userName, String host, String sessionId, Date timeConnected, Date lastActivity, String gameInfo, String userState, Date muteChatUntil, String clientVersion, String email, String userIdStr) {
        this.userName = userName;
        this.host = host;
        this.sessionId = sessionId;
        this.timeConnected = timeConnected;
        this.lastActivity = lastActivity;
        this.gameInfo = gameInfo;
        this.userState = userState;
        this.muteChatUntil = muteChatUntil;
        this.clientVersion = clientVersion;
        this.email = email;
        this.userIdStr = userIdStr;
    }

    public String getUserName() {
        return userName;
    }

    public String getHost() {
        return host;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getGameInfo() {
        return gameInfo;
    }

    public String getUserState() {
        return userState;
    }

    public Date getMuteChatUntil() {
        return muteChatUntil;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public Date getTimeConnected() {
        return timeConnected;
    }

    public Date getLastActivity() {
        return lastActivity;
    }

    public String getEmail() {
        return email;
    }

    public String getUserIdStr() {
        return userIdStr;
    }

    public ViewProto.UserView toProto() {
        ViewProto.UserView.Builder builder = ViewProto.UserView.newBuilder()
                .setUserName(userName != null ? userName : "")
                .setHost(host != null ? host : "")
                .setSessionId(sessionId != null ? sessionId : "")
                .setGameInfo(gameInfo != null ? gameInfo : "")
                .setUserState(userState != null ? userState : "")
                .setClientVersion(clientVersion != null ? clientVersion : "")
                .setEmail(email != null ? email : "")
                .setUserIdStr(userIdStr != null ? userIdStr : "");

        if (timeConnected != null) {
            builder.setTimeConnectedInMillis(timeConnected.getTime());
        }

        if (lastActivity != null) {
            builder.setLastActivityInMillis(lastActivity.getTime());
        }

        if (muteChatUntil != null) {
            builder.setMuteChatUntilInMillis(muteChatUntil.getTime());
        }

        return builder.build();
    }

    public static UserView fromProto(ViewProto.UserView proto) {
        return new UserView(proto);
    }

    // Private constructor for fromProto
    private UserView(ViewProto.UserView proto) {
        this.userName = proto.getUserName();
        this.host = proto.getHost();
        this.sessionId = proto.getSessionId();
        this.timeConnected = proto.getTimeConnectedInMillis() > 0 ? new Date(proto.getTimeConnectedInMillis()) : null;
        this.lastActivity = proto.getLastActivityInMillis() > 0 ? new Date(proto.getLastActivityInMillis()) : null;
        this.gameInfo = proto.getGameInfo();
        this.userState = proto.getUserState();
        this.muteChatUntil = proto.getMuteChatUntilInMillis() > 0 ? new Date(proto.getMuteChatUntilInMillis()) : null;
        this.clientVersion = proto.getClientVersion();
        this.email = proto.getEmail();
        this.userIdStr = proto.getUserIdStr();
    }
}
