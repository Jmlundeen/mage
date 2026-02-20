package mage.players.net;

import mage.ws.model.ModelProto;

import java.io.Serializable;
import java.util.*;

/**
 * User data that is passed during connection to the server.
 *
 * @author ayrat
 */
public class UserData implements Serializable {

    protected int groupId;
    protected int avatarId;
    protected boolean allowRequestShowHandCards;
    protected boolean confirmEmptyManaPool;
    protected UserSkipPrioritySteps userSkipPrioritySteps;
    protected String flagName;
    protected boolean askMoveToGraveOrder;
    protected boolean manaPoolAutomatic;
    protected boolean manaPoolAutomaticRestricted;
    protected boolean passPriorityCast;
    protected boolean passPriorityActivation;
    protected boolean autoOrderTrigger; // auto-order triggers with same rule text
    protected int autoTargetLevel;
    protected boolean useSameSettingsForReplacementEffects;
    protected boolean useFirstManaAbility = false;
    private String userIdStr; // TODO: delete as un-used or use for hardware id instead?
    protected Map<UUID, Set<UUID>> requestedHandPlayersList; // game -> players list

    protected String matchHistory;
    protected int matchQuitRatio;
    protected String tourneyHistory;
    protected int tourneyQuitRatio;

    private int generalRating;
    private int constructedRating;
    private int limitedRating;

    public UserData(UserGroup userGroup,
                    int avatarId,
                    boolean allowRequestShowHandCards,
                    boolean confirmEmptyManaPool,
                    UserSkipPrioritySteps userSkipPrioritySteps,
                    String flagName,
                    boolean askMoveToGraveOrder,
                    boolean manaPoolAutomatic,
                    boolean manaPoolAutomaticRestricted,
                    boolean passPriorityCast,
                    boolean passPriorityActivation,
                    boolean autoOrderTrigger,
                    int autoTargetLevel,
                    boolean useSameSettingsForReplacementEffects,
                    boolean useFirstManaAbility,
                    String userIdStr) {
        this.groupId = userGroup.getGroupId();
        this.avatarId = avatarId;
        this.allowRequestShowHandCards = allowRequestShowHandCards;
        this.userSkipPrioritySteps = userSkipPrioritySteps;
        this.confirmEmptyManaPool = confirmEmptyManaPool;
        this.flagName = flagName;
        this.askMoveToGraveOrder = askMoveToGraveOrder;
        this.manaPoolAutomatic = manaPoolAutomatic;
        this.manaPoolAutomaticRestricted = manaPoolAutomaticRestricted;
        this.passPriorityCast = passPriorityCast;
        this.passPriorityActivation = passPriorityActivation;
        this.autoOrderTrigger = autoOrderTrigger;
        this.autoTargetLevel = autoTargetLevel;
        this.useSameSettingsForReplacementEffects = useSameSettingsForReplacementEffects;
        this.useFirstManaAbility = useFirstManaAbility;
        this.matchHistory = "";
        this.matchQuitRatio = 0;
        this.tourneyHistory = "";
        this.tourneyQuitRatio = 0;
        this.userIdStr = userIdStr;
        this.requestedHandPlayersList = new HashMap<>();
    }

    public void update(UserData userData) {
        this.groupId = userData.groupId;
        this.avatarId = userData.avatarId;
        this.allowRequestShowHandCards = userData.allowRequestShowHandCards;
        this.userSkipPrioritySteps = userData.userSkipPrioritySteps;
        this.confirmEmptyManaPool = userData.confirmEmptyManaPool;
        this.flagName = userData.flagName;
        this.askMoveToGraveOrder = userData.askMoveToGraveOrder;
        this.manaPoolAutomatic = userData.manaPoolAutomatic;
        this.manaPoolAutomaticRestricted = userData.manaPoolAutomaticRestricted;
        this.passPriorityCast = userData.passPriorityCast;
        this.passPriorityActivation = userData.passPriorityActivation;
        this.autoOrderTrigger = userData.autoOrderTrigger;
        this.autoTargetLevel = userData.autoTargetLevel;
        this.useSameSettingsForReplacementEffects = userData.useSameSettingsForReplacementEffects;
        this.useFirstManaAbility = userData.useFirstManaAbility;
        this.userIdStr = userData.userIdStr;
        // todo: why we don't update user stats here? => can't be updated from client side
    }

    public static UserData getDefaultUserDataView() {
        return new UserData(
                UserGroup.DEFAULT,
                0,
                false,
                true,
                new UserSkipPrioritySteps(),
                getDefaultFlagName(),
                false,
                true,
                true,
                false,
                false,
                true,
                1,
                true,
                false,
                ""
        );
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(int avatarId) {
        this.avatarId = avatarId;
    }

    public boolean isAllowRequestHandToAll() {
        return allowRequestShowHandCards;
    }

    public boolean isAllowRequestHandToPlayer(UUID gameId, UUID requesterPlayerId) {
        // once per game
        boolean allowToPlayer = !requestedHandPlayersList.containsKey(gameId) || !requestedHandPlayersList.get(gameId).contains(requesterPlayerId);
        return isAllowRequestHandToAll() && allowToPlayer;
    }

    public void addPlayerToRequestedHandList(UUID gameId, UUID requesterPlayerId) {
        if (!requestedHandPlayersList.containsKey(gameId)) {
            requestedHandPlayersList.put(gameId, new HashSet<>());
        }
        Set<UUID> requestedPlayers = requestedHandPlayersList.get(gameId);
        requestedPlayers.add(requesterPlayerId);
    }

    public void setAllowRequestShowHandCards(boolean allowRequestShowHandCards) {
        this.allowRequestShowHandCards = allowRequestShowHandCards;
    }

    public void resetRequestedHandPlayersList(UUID gameId) {
        this.requestedHandPlayersList.remove(gameId);
    }

    public UserSkipPrioritySteps getUserSkipPrioritySteps() {
        return userSkipPrioritySteps;
    }

    public void setUserSkipPrioritySteps(UserSkipPrioritySteps userSkipPrioritySteps) {
        this.userSkipPrioritySteps = userSkipPrioritySteps;
    }

    public boolean confirmEmptyManaPool() {
        return confirmEmptyManaPool;
    }

    public void setConfirmEmptyManaPool(boolean confirmEmptyManaPool) {
        this.confirmEmptyManaPool = confirmEmptyManaPool;
    }

    public String getFlagName() {
        return flagName;
    }

    public void setFlagName(String flagName) {
        this.flagName = flagName;
    }

    public boolean askMoveToGraveOrder() {
        return askMoveToGraveOrder;
    }

    public void setAskMoveToGraveOrder(boolean askMoveToGraveOrder) {
        this.askMoveToGraveOrder = askMoveToGraveOrder;
    }

    public boolean isManaPoolAutomatic() {
        return manaPoolAutomatic;
    }

    public void setManaPoolAutomatic(boolean manaPoolAutomatic) {
        this.manaPoolAutomatic = manaPoolAutomatic;
    }

    public boolean isManaPoolAutomaticRestricted() {
        return manaPoolAutomaticRestricted;
    }

    public void setManaPoolAutomaticRestricted(boolean manaPoolAutomaticRestricted) {
        this.manaPoolAutomaticRestricted = manaPoolAutomaticRestricted;
    }

    public boolean isPassPriorityCast() {
        return passPriorityCast;
    }

    public void setPassPriorityCast(boolean passPriorityCast) {
        this.passPriorityCast = passPriorityCast;
    }

    public boolean isPassPriorityActivation() {
        return passPriorityActivation;
    }

    public void setPassPriorityActivation(boolean passPriorityActivation) {
        this.passPriorityActivation = passPriorityActivation;
    }

    public boolean isAutoOrderTrigger() {
        return autoOrderTrigger;
    }

    public void setAutoOrderTrigger(boolean autoOrderTrigger) {
        this.autoOrderTrigger = autoOrderTrigger;
    }

    public int getAutoTargetLevel() {
        return autoTargetLevel;
    }

    public void setAutoTargetLevel(int autoTargetLevel) {
        this.autoTargetLevel = autoTargetLevel;
    }

    public boolean isUseSameSettingsForReplacementEffects() {
        return useSameSettingsForReplacementEffects;
    }

    public boolean isUseFirstManaAbility() {
        return useFirstManaAbility;
    }

    public void setUseFirstManaAbility(boolean useFirstManaAbility) {
        this.useFirstManaAbility = useFirstManaAbility;
    }

    public String getHistory() {
        if (UserGroup.COMPUTER.getGroupId() == this.groupId) { // Why we are checking UserGroup and integer equality??
            return "";
        }
        // todo: add preference to hide rating?
        return "Matches: " + this.matchHistory + " (" + this.matchQuitRatio + "%), Tourneys: " + this.tourneyHistory + " (" + this.tourneyQuitRatio + "%)"
                + ", Constructed Rating: " + getConstructedRating()
                + ", Limited Rating: " + getLimitedRating();
    }

    public void setMatchHistory(String history) {
        this.matchHistory = history;
    }

    public String getMatchHistory() {
        return matchHistory;
    }

    public void setMatchQuitRatio(int ratio) {
        this.matchQuitRatio = ratio;
    }

    public int getMatchQuitRatio() {
        return matchQuitRatio;
    }

    public void setTourneyHistory(String history) {
        this.tourneyHistory = history;
    }

    public String getTourneyHistory() {
        return tourneyHistory;
    }

    public void setTourneyQuitRatio(int ratio) {
        this.tourneyQuitRatio = ratio;
    }

    public int getTourneyQuitRatio() {
        return tourneyQuitRatio;
    }

    public int getGeneralRating() {
        return generalRating;
    }

    public void setGeneralRating(int generalRating) {
        this.generalRating = generalRating;
    }

    public int getConstructedRating() {
        return constructedRating;
    }

    public void setConstructedRating(int constructedRating) {
        this.constructedRating = constructedRating;
    }

    public int getLimitedRating() {
        return limitedRating;
    }

    public void setLimitedRating(int limitedRating) {
        this.limitedRating = limitedRating;
    }

    public static String getDefaultFlagName() {
        return "world.png";
    }

    public ModelProto.UserData toProto() {
        ModelProto.UserData.Builder builder = ModelProto.UserData.newBuilder()
                .setGroupId(this.groupId)
                .setAvatarId(this.avatarId)
                .setUserSkipPrioritySteps(this.userSkipPrioritySteps.toProto())
                .setAllowRequestShowHandCards(this.allowRequestShowHandCards)
                .setConfirmEmptyManaPool(this.confirmEmptyManaPool)
                .setFlagName(this.flagName != null ? this.flagName : "")
                .setAskMoveToGraveOrder(this.askMoveToGraveOrder)
                .setManaPoolAutomatic(this.manaPoolAutomatic)
                .setManaPoolAutomaticRestricted(this.manaPoolAutomaticRestricted)
                .setPassPriorityCast(this.passPriorityCast)
                .setPassPriorityActivation(this.passPriorityActivation)
                .setAutoOrderTrigger(this.autoOrderTrigger)
                .setAutoTargetLevel(this.autoTargetLevel)
                .setUseSameSettingsForReplacementEffects(this.useSameSettingsForReplacementEffects)
                .setUseFirstManaAbility(this.useFirstManaAbility)
                .setUserIdStr(this.userIdStr != null ? this.userIdStr : "")
                .setMatchHistory(this.matchHistory != null ? this.matchHistory : "")
                .setMatchQuitRatio(this.matchQuitRatio)
                .setTourneyHistory(this.tourneyHistory != null ? this.tourneyHistory : "")
                .setTourneyQuitRatio(this.tourneyQuitRatio)
                .setGeneralRating(this.generalRating)
                .setConstructedRating(this.constructedRating)
                .setLimitedRating(this.limitedRating);

        // Convert requestedHandPlayersList map
        if (this.requestedHandPlayersList != null) {
            for (Map.Entry<UUID, Set<UUID>> entry : this.requestedHandPlayersList.entrySet()) {
                ModelProto.PlayerIdList.Builder listBuilder = ModelProto.PlayerIdList.newBuilder();
                for (UUID playerId : entry.getValue()) {
                    listBuilder.addPlayerIds(playerId.toString());
                }
                builder.putRequestedHandPlayersList(entry.getKey().toString(), listBuilder.build());
            }
        }

        return builder.build();
    }

    public static UserData fromProto(ModelProto.UserData proto) {
        // Create UserSkipPrioritySteps from proto


        // Create UserData instance
        UserData userData = new UserData(
                UserGroup.fromId(proto.getGroupId()),
                proto.getAvatarId(),
                proto.getAllowRequestShowHandCards(),
                proto.getConfirmEmptyManaPool(),
                UserSkipPrioritySteps.fromProto(proto.getUserSkipPrioritySteps()),
                proto.getFlagName(),
                proto.getAskMoveToGraveOrder(),
                proto.getManaPoolAutomatic(),
                proto.getManaPoolAutomaticRestricted(),
                proto.getPassPriorityCast(),
                proto.getPassPriorityActivation(),
                proto.getAutoOrderTrigger(),
                proto.getAutoTargetLevel(),
                proto.getUseSameSettingsForReplacementEffects(),
                proto.getUseFirstManaAbility(),
                proto.getUserIdStr()
        );

        // Set additional fields
        userData.setMatchHistory(proto.getMatchHistory());
        userData.setMatchQuitRatio(proto.getMatchQuitRatio());
        userData.setTourneyHistory(proto.getTourneyHistory());
        userData.setTourneyQuitRatio(proto.getTourneyQuitRatio());
        userData.setGeneralRating(proto.getGeneralRating());
        userData.setConstructedRating(proto.getConstructedRating());
        userData.setLimitedRating(proto.getLimitedRating());

        // Convert requestedHandPlayersList map
        for (Map.Entry<String, ModelProto.PlayerIdList> entry : proto.getRequestedHandPlayersListMap().entrySet()) {
            UUID gameId = UUID.fromString(entry.getKey());
            Set<UUID> playerIds = new HashSet<>();
            for (String playerIdStr : entry.getValue().getPlayerIdsList()) {
                playerIds.add(UUID.fromString(playerIdStr));
            }
            userData.requestedHandPlayersList.put(gameId, playerIds);
        }

        return userData;
    }
}
