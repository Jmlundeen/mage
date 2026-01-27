package mage.game.match;

import mage.cards.decks.DeckCardInfo;
import mage.constants.*;
import mage.game.mulligan.MulliganType;
import mage.game.result.ResultProtos;
import mage.players.PlayerType;

import java.io.Serializable;
import java.util.*;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class MatchOptions implements Serializable {

    protected String name;
    protected MultiplayerAttackOption attackOption = MultiplayerAttackOption.LEFT;
    protected RangeOfInfluence range = RangeOfInfluence.ALL;
    protected int winsNeeded;
    protected int freeMulligans;
    protected boolean customStartLifeEnabled;
    protected int customStartLife; // Only use if customStartLifeEnabled is True
    protected boolean customStartHandSizeEnabled;
    protected int customStartHandSize; // Only use if customStartHandSizeEnabled is True
    protected String gameType;
    protected String deckType;
    protected boolean limited;
    protected List<PlayerType> playerTypes = new ArrayList<>();
    protected boolean multiPlayer; // allow to play single game with all tourney's players
    protected String password;
    protected SkillLevel skillLevel = SkillLevel.CASUAL;
    protected boolean rollbackTurnsAllowed;
    protected boolean spectatorsAllowed;
    protected boolean planeChase;
    protected int quitRatio;
    protected int minimumRating;
    protected int edhPowerLevel;
    protected boolean rated;
    protected Set<String> bannedUsers = new HashSet<>();

    protected MatchTimeLimit matchTimeLimit = MatchTimeLimit.NONE; // total time limit for priority
    protected MatchBufferTime matchBufferTime = MatchBufferTime.NONE; // additional/buffer time limit for each priority before real time ticking starts
    protected MulliganType mulliganType = MulliganType.GAME_DEFAULT;

    protected Collection<DeckCardInfo> perPlayerEmblemCards = Collections.emptySet();
    protected Collection<DeckCardInfo> globalEmblemCards = Collections.emptySet();

    public MatchOptions(String name, String gameType, boolean multiPlayer) {
        this.name = name;
        this.gameType = gameType;
        this.password = "";
        this.multiPlayer = multiPlayer;
    }

    public boolean isSingleGameTourney() {
        return multiPlayer;
    }

    public String getName() {
        return name;
    }

    public MultiplayerAttackOption getAttackOption() {
        return attackOption;
    }

    public void setAttackOption(MultiplayerAttackOption attackOption) {
        this.attackOption = attackOption;
    }

    public RangeOfInfluence getRange() {
        return range;
    }

    public void setRange(RangeOfInfluence range) {
        this.range = range;
    }

    public int getWinsNeeded() {
        return winsNeeded;
    }

    public void setWinsNeeded(int winsNeeded) {
        this.winsNeeded = winsNeeded;
    }

    public int getFreeMulligans() {
        return freeMulligans;
    }

    public void setFreeMulligans(int freeMulligans) {
        this.freeMulligans = freeMulligans;
    }

    public boolean isCustomStartLifeEnabled() {
        return customStartLifeEnabled;
    }

    public void setCustomStartLifeEnabled(boolean value) {
        this.customStartLifeEnabled = value;
    }

    public int getCustomStartLife() {
        return customStartLife;
    }

    public void setCustomStartLife(int startLife) {
        this.customStartLife = startLife;
    }

    public boolean isCustomStartHandSizeEnabled() {
        return customStartHandSizeEnabled;
    }

    public void setCustomStartHandSizeEnabled(boolean value) {
        this.customStartHandSizeEnabled = value;
    }

    public int getCustomStartHandSize() {
        return customStartHandSize;
    }

    public void setCustomStartHandSize(int startHandSize) {
        this.customStartHandSize = startHandSize;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getDeckType() {
        return deckType;
    }

    public void setDeckType(String deckType) {
        this.deckType = deckType;
    }

    public List<PlayerType> getPlayerTypes() {
        return playerTypes;
    }

    public boolean isLimited() {
        return limited;
    }

    public void setLimited(boolean limited) {
        this.limited = limited;
    }

    public MatchTimeLimit getMatchTimeLimit() {
        return this.matchTimeLimit;
    }

    public void setMatchTimeLimit(MatchTimeLimit matchTimeLimit) {
        this.matchTimeLimit = Optional.ofNullable(matchTimeLimit).orElse(MatchTimeLimit.NONE);
    }

    public MatchBufferTime getMatchBufferTime() {
        return this.matchBufferTime;
    }

    public void setMatchBufferTime(MatchBufferTime matchBufferTime) {
        this.matchBufferTime = Optional.ofNullable(matchBufferTime).orElse(MatchBufferTime.NONE);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public SkillLevel getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(SkillLevel skillLevel) {
        this.skillLevel = skillLevel;
    }

    public boolean isRollbackTurnsAllowed() {
        return rollbackTurnsAllowed;
    }

    public void setRollbackTurnsAllowed(boolean rollbackTurnsAllowed) {
        this.rollbackTurnsAllowed = rollbackTurnsAllowed;
    }

    public boolean isSpectatorsAllowed() {
        return spectatorsAllowed;
    }

    public void setSpectatorsAllowed(boolean spectatorsAllowed) {
        this.spectatorsAllowed = spectatorsAllowed;
    }

    public boolean isPlaneChase() {
        return planeChase;
    }

    public void setPlaneChase(boolean planeChase) {
        this.planeChase = planeChase;
    }

    public int getQuitRatio() {
        return quitRatio;
    }

    public void setQuitRatio(int quitRatio) {
        this.quitRatio = quitRatio;
    }

    public int getMinimumRating() {
        return minimumRating;
    }

    public void setMinimumRating(int minimumRating) {
        this.minimumRating = minimumRating;
    }

    public int getEdhPowerLevel() {
        return edhPowerLevel;
    }

    public void setEdhPowerLevel(int edhPowerLevel) {
        this.edhPowerLevel = edhPowerLevel;
    }

    public boolean isRated() {
        return rated;
    }

    public void setRated(boolean rated) {
        this.rated = rated;
    }

    public Set<String> getBannedUsers() {
        return bannedUsers;
    }

    public void setBannedUsers(Set<String> bannedUsers) {
        this.bannedUsers = bannedUsers;
    }

    public ResultProtos.MatchOptionsProto toProto() {
        ResultProtos.MatchOptionsProto.Builder builder = ResultProtos.MatchOptionsProto.newBuilder()
                .setName(this.getName())
                .setLimited(this.isLimited())
                .setRated(this.isRated())
                .setWinsNeeded(this.getWinsNeeded())
                .setGameType(this.getGameType() != null ? this.getGameType() : "")
                .setDeckType(this.getDeckType() != null ? this.getDeckType() : "")
                .setMultiPlayer(this.isSingleGameTourney())
                .setPassword(this.getPassword() != null ? this.getPassword() : "")
                .setFreeMulligans(this.getFreeMulligans())
                .setCustomStartLifeEnabled(this.isCustomStartLifeEnabled())
                .setCustomStartLife(this.getCustomStartLife())
                .setCustomStartHandSizeEnabled(this.isCustomStartHandSizeEnabled())
                .setCustomStartHandSize(this.getCustomStartHandSize())
                .setRollbackTurnsAllowed(this.isRollbackTurnsAllowed())
                .setSpectatorsAllowed(this.isSpectatorsAllowed())
                .setPlaneChase(this.isPlaneChase())
                .setQuitRatio(this.getQuitRatio())
                .setMinimumRating(this.getMinimumRating())
                .setEdhPowerLevel(this.getEdhPowerLevel())
                .addAllBannedUsers(this.getBannedUsers());

        // Convert SkillLevel enum
        ResultProtos.SkillLevel skillLevel = switch (this.getSkillLevel()) {
            case CASUAL -> ResultProtos.SkillLevel.CASUAL;
            case SERIOUS -> ResultProtos.SkillLevel.SERIOUS;
            default -> ResultProtos.SkillLevel.BEGINNER;
        };
        builder.setSkillLevel(skillLevel);

        // Convert MultiplayerAttackOption enum
        ResultProtos.MultiplayerAttackOption attackOption = switch (this.getAttackOption()) {
            case LEFT -> ResultProtos.MultiplayerAttackOption.ATTACK_LEFT;
            case RIGHT -> ResultProtos.MultiplayerAttackOption.ATTACK_RIGHT;
            default -> ResultProtos.MultiplayerAttackOption.ATTACK_MULTIPLE;
        };
        builder.setAttackOption(attackOption);

        // Convert RangeOfInfluence enum
        ResultProtos.RangeOfInfluence range = switch (this.getRange()) {
            case ONE -> ResultProtos.RangeOfInfluence.RANGE_ONE;
            case TWO -> ResultProtos.RangeOfInfluence.RANGE_TWO;
            default -> ResultProtos.RangeOfInfluence.RANGE_ALL;
        };
        builder.setRange(range);

        // Convert MatchTimeLimit enum
        ResultProtos.MatchTimeLimit timeLimit = switch (this.getMatchTimeLimit()) {
            case MIN___5 -> ResultProtos.MatchTimeLimit.TIME_MIN_5;
            case MIN__10 -> ResultProtos.MatchTimeLimit.TIME_MIN_10;
            case MIN__15 -> ResultProtos.MatchTimeLimit.TIME_MIN_15;
            case MIN__20 -> ResultProtos.MatchTimeLimit.TIME_MIN_20;
            case MIN__25 -> ResultProtos.MatchTimeLimit.TIME_MIN_25;
            case MIN__30 -> ResultProtos.MatchTimeLimit.TIME_MIN_30;
            case MIN__35 -> ResultProtos.MatchTimeLimit.TIME_MIN_35;
            case MIN__40 -> ResultProtos.MatchTimeLimit.TIME_MIN_40;
            case MIN__45 -> ResultProtos.MatchTimeLimit.TIME_MIN_45;
            case MIN__50 -> ResultProtos.MatchTimeLimit.TIME_MIN_50;
            case MIN__55 -> ResultProtos.MatchTimeLimit.TIME_MIN_55;
            case MIN__60 -> ResultProtos.MatchTimeLimit.TIME_MIN_60;
            case MIN__90 -> ResultProtos.MatchTimeLimit.TIME_MIN_90;
            case MIN_120 -> ResultProtos.MatchTimeLimit.TIME_MIN_120;
            default -> ResultProtos.MatchTimeLimit.TIME_NONE;
        };
        builder.setMatchTimeLimit(timeLimit);

        // Convert MatchBufferTime enum
        ResultProtos.MatchBufferTime bufferTime = switch (this.getMatchBufferTime()) {
            case SEC__01 -> ResultProtos.MatchBufferTime.BUFFER_SEC_1;
            case SEC__02 -> ResultProtos.MatchBufferTime.BUFFER_SEC_2;
            case SEC__03 -> ResultProtos.MatchBufferTime.BUFFER_SEC_3;
            case SEC__05 -> ResultProtos.MatchBufferTime.BUFFER_SEC_5;
            case SEC__10 -> ResultProtos.MatchBufferTime.BUFFER_SEC_10;
            case SEC__15 -> ResultProtos.MatchBufferTime.BUFFER_SEC_15;
            case SEC__20 -> ResultProtos.MatchBufferTime.BUFFER_SEC_20;
            case SEC__25 -> ResultProtos.MatchBufferTime.BUFFER_SEC_25;
            case SEC__30 -> ResultProtos.MatchBufferTime.BUFFER_SEC_30;
            default -> ResultProtos.MatchBufferTime.BUFFER_NONE;
        };
        builder.setMatchBufferTime(bufferTime);

        // Convert MulliganType enum
        ResultProtos.MulliganType mulliganType = switch (this.getMulliganType()) {
            case VANCOUVER -> ResultProtos.MulliganType.MULLIGAN_VANCOUVER;
            case PARIS -> ResultProtos.MulliganType.MULLIGAN_PARIS;
            case LONDON -> ResultProtos.MulliganType.MULLIGAN_LONDON;
            case SMOOTHED_LONDON -> ResultProtos.MulliganType.MULLIGAN_SMOOTHED_LONDON;
            case CANADIAN_HIGHLANDER -> ResultProtos.MulliganType.MULLIGAN_CANADIAN_HIGHLANDER;
            default -> ResultProtos.MulliganType.MULLIGAN_GAME_DEFAULT;
        };
        builder.setMulliganType(mulliganType);

        // Add player types as strings
        for (PlayerType playerType : this.getPlayerTypes()) {
            builder.addPlayerTypes(playerType.toString());
        }

        return builder.build();
    }

    public static MatchOptions fromProto(ResultProtos.MatchOptionsProto proto) {
        MatchOptions options = new MatchOptions(
                proto.getName(),
                proto.getGameType().isEmpty() ? "" : proto.getGameType(),
                proto.getMultiPlayer()
        );
        options.setLimited(proto.getLimited());
        options.setRated(proto.getRated());
        options.setWinsNeeded(proto.getWinsNeeded());
        options.setDeckType(proto.getDeckType().isEmpty() ? "" : proto.getDeckType());
        options.setPassword(proto.getPassword().isEmpty() ? "" : proto.getPassword());
        options.setFreeMulligans(proto.getFreeMulligans());
        options.setCustomStartLifeEnabled(proto.getCustomStartLifeEnabled());
        options.setCustomStartLife(proto.getCustomStartLife());
        options.setCustomStartHandSizeEnabled(proto.getCustomStartHandSizeEnabled());
        options.setCustomStartHandSize(proto.getCustomStartHandSize());
        options.setRollbackTurnsAllowed(proto.getRollbackTurnsAllowed());
        options.setSpectatorsAllowed(proto.getSpectatorsAllowed());
        options.setPlaneChase(proto.getPlaneChase());
        options.setQuitRatio(proto.getQuitRatio());
        options.setMinimumRating(proto.getMinimumRating());
        options.setEdhPowerLevel(proto.getEdhPowerLevel());
        options.setBannedUsers(new HashSet<>(proto.getBannedUsersList()));

        // Convert SkillLevel enum
        SkillLevel skillLevel = switch (proto.getSkillLevel()) {
            case CASUAL -> SkillLevel.CASUAL;
            case SERIOUS -> SkillLevel.SERIOUS;
            default -> SkillLevel.BEGINNER;
        };
        options.setSkillLevel(skillLevel);

        // Convert MultiplayerAttackOption enum
        MultiplayerAttackOption attackOption = switch (proto.getAttackOption()) {
            case ATTACK_LEFT -> MultiplayerAttackOption.LEFT;
            case ATTACK_RIGHT -> MultiplayerAttackOption.RIGHT;
            default -> MultiplayerAttackOption.MULTIPLE;
        };
        options.setAttackOption(attackOption);

        // Convert RangeOfInfluence enum
        RangeOfInfluence range = switch (proto.getRange()) {
            case RANGE_ONE -> RangeOfInfluence.ONE;
            case RANGE_TWO -> RangeOfInfluence.TWO;
            default -> RangeOfInfluence.ALL;
        };
        options.setRange(range);

        // Convert MatchTimeLimit enum
        MatchTimeLimit timeLimit = switch (proto.getMatchTimeLimit()) {
            case TIME_MIN_5 -> MatchTimeLimit.MIN___5;
            case TIME_MIN_10 -> MatchTimeLimit.MIN__10;
            case TIME_MIN_15 -> MatchTimeLimit.MIN__15;
            case TIME_MIN_20 -> MatchTimeLimit.MIN__20;
            case TIME_MIN_25 -> MatchTimeLimit.MIN__25;
            case TIME_MIN_30 -> MatchTimeLimit.MIN__30;
            case TIME_MIN_35 -> MatchTimeLimit.MIN__35;
            case TIME_MIN_40 -> MatchTimeLimit.MIN__40;
            case TIME_MIN_45 -> MatchTimeLimit.MIN__45;
            case TIME_MIN_50 -> MatchTimeLimit.MIN__50;
            case TIME_MIN_55 -> MatchTimeLimit.MIN__55;
            case TIME_MIN_60 -> MatchTimeLimit.MIN__60;
            case TIME_MIN_90 -> MatchTimeLimit.MIN__90;
            case TIME_MIN_120 -> MatchTimeLimit.MIN_120;
            default -> MatchTimeLimit.NONE;
        };
        options.setMatchTimeLimit(timeLimit);

        // Convert MatchBufferTime enum
        MatchBufferTime bufferTime = switch (proto.getMatchBufferTime()) {
            case BUFFER_SEC_1 -> MatchBufferTime.SEC__01;
            case BUFFER_SEC_2 -> MatchBufferTime.SEC__02;
            case BUFFER_SEC_3 -> MatchBufferTime.SEC__03;
            case BUFFER_SEC_5 -> MatchBufferTime.SEC__05;
            case BUFFER_SEC_10 -> MatchBufferTime.SEC__10;
            case BUFFER_SEC_15 -> MatchBufferTime.SEC__15;
            case BUFFER_SEC_20 -> MatchBufferTime.SEC__20;
            case BUFFER_SEC_25 -> MatchBufferTime.SEC__25;
            case BUFFER_SEC_30 -> MatchBufferTime.SEC__30;
            default -> MatchBufferTime.NONE;
        };
        options.setMatchBufferTime(bufferTime);

        // Convert MulliganType enum
        MulliganType mulliganType = switch (proto.getMulliganType()) {
            case MULLIGAN_VANCOUVER -> MulliganType.VANCOUVER;
            case MULLIGAN_PARIS -> MulliganType.PARIS;
            case MULLIGAN_LONDON -> MulliganType.LONDON;
            case MULLIGAN_SMOOTHED_LONDON -> MulliganType.SMOOTHED_LONDON;
            case MULLIGAN_CANADIAN_HIGHLANDER -> MulliganType.CANADIAN_HIGHLANDER;
            default -> MulliganType.GAME_DEFAULT;
        };
        options.setMullgianType(mulliganType);

        // Convert player type strings back to PlayerType enum
        options.getPlayerTypes().clear();
        for (String playerTypeStr : proto.getPlayerTypesList()) {
            options.getPlayerTypes().add(PlayerType.getByDescription(playerTypeStr));
        }

        return options;
    }

    public void setMullgianType(MulliganType mulliganType) {
        this.mulliganType = Optional.ofNullable(mulliganType).orElse(MulliganType.GAME_DEFAULT);
    }

    public MulliganType getMulliganType() {
        return mulliganType;
    }

    public Collection<DeckCardInfo> getPerPlayerEmblemCards() {
        return perPlayerEmblemCards;
    }

    public void setPerPlayerEmblemCards(Collection<DeckCardInfo> perPlayerEmblemCards) {
        this.perPlayerEmblemCards = perPlayerEmblemCards;
    }

    public Collection<DeckCardInfo> getGlobalEmblemCards() {
        return globalEmblemCards;
    }

    public void setGlobalEmblemCards(Collection<DeckCardInfo> globalEmblemCards) {
        this.globalEmblemCards = globalEmblemCards;
    }
}
