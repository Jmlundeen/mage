
package mage.view;

import mage.ws.view.ViewProto;

import java.io.Serializable;

/**
 *
 * @author LevelX2
 */
public class UsersView implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String flagName;
    private final String userName;
    private final String matchHistory;
    private final int matchQuitRatio;
    private final String tourneyHistory;
    private final int tourneyQuitRatio;
    private final String infoGames;
    private final String infoPing;
    private final int generalRating;
    private final int constructedRating;
    private final int limitedRating;

    public UsersView(String flagName, String userName, String matchHistory, int matchQuitRatio,
            String tourneyHistory, int tourneyQuitRatio, String infoGames, String infoPing,
            int generalRating, int constructedRating, int limitedRating) {
        this.flagName = flagName;
        this.matchHistory = matchHistory;
        this.matchQuitRatio = matchQuitRatio;
        this.tourneyHistory = tourneyHistory;
        this.tourneyQuitRatio = tourneyQuitRatio;
        this.userName = userName;
        this.infoGames = infoGames;
        this.infoPing = infoPing;
        this.generalRating = generalRating;
        this.constructedRating = constructedRating;
        this.limitedRating = limitedRating;
    }

    public String getFlagName() {
        return flagName;
    }

    public String getUserName() {
        return userName;
    }

    public String getMatchHistory() {
        return matchHistory;
    }

    public int getMatchQuitRatio() {
        return matchQuitRatio;
    }

    public String getTourneyHistory() {
        return tourneyHistory;
    }

    public int getTourneyQuitRatio() {
        return tourneyQuitRatio;
    }

    public String getInfoGames() {
        return infoGames;
    }

    public String getInfoPing() {
        return infoPing;
    }

    public int getGeneralRating() {
        return generalRating;
    }

    public int getConstructedRating() {
        return constructedRating;
    }

    public int getLimitedRating() {
        return limitedRating;
    }

    public ViewProto.UsersView toProto() {
        return ViewProto.UsersView.newBuilder()
                .setFlagName(flagName != null ? flagName : "")
                .setUserName(userName != null ? userName : "")
                .setMatchHistory(matchHistory != null ? matchHistory : "")
                .setMatchQuitRatio(matchQuitRatio)
                .setTourneyHistory(tourneyHistory != null ? tourneyHistory : "")
                .setTourneyQuitRatio(tourneyQuitRatio)
                .setInfoGames(infoGames != null ? infoGames : "")
                .setInfoPing(infoPing != null ? infoPing : "")
                .setGeneralRating(generalRating)
                .setConstructedRating(constructedRating)
                .setLimitedRating(limitedRating)
                .build();
    }

    public static UsersView fromProto(ViewProto.UsersView proto) {
        return new UsersView(proto);
    }

    // Private constructor for fromProto
    private UsersView(ViewProto.UsersView proto) {
        this.flagName = proto.getFlagName();
        this.userName = proto.getUserName();
        this.matchHistory = proto.getMatchHistory();
        this.matchQuitRatio = proto.getMatchQuitRatio();
        this.tourneyHistory = proto.getTourneyHistory();
        this.tourneyQuitRatio = proto.getTourneyQuitRatio();
        this.infoGames = proto.getInfoGames();
        this.infoPing = proto.getInfoPing();
        this.generalRating = proto.getGeneralRating();
        this.constructedRating = proto.getConstructedRating();
        this.limitedRating = proto.getLimitedRating();
    }
}
