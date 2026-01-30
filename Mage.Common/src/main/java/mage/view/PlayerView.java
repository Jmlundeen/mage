package mage.view;

import mage.cards.Card;
import mage.counters.Counter;
import mage.designations.Designation;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.GameState;
import mage.game.command.*;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.players.net.UserData;
import mage.util.CardUtil;
import mage.ws.v1.model.ModelProto;
import mage.ws.v1.view.ViewProto;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class PlayerView implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID playerId;
    private final String name;
    private final boolean controlled; // gui: player is current user
    private final boolean isHuman; // human or computer
    private final int life;
    private final List<CounterView> counters;
    private final int wins;
    private final int winsNeeded;
    private final int libraryCount;
    private final int handCount;
    private final boolean isActive;
    private final boolean hasPriority;
    private final boolean timerActive;
    private final boolean hasLeft;
    private final ManaPoolView manaPool;
    private final CardsView graveyard = new CardsView();
    private final CardsView exile = new CardsView();
    private final CardsView sideboard = new CardsView();
    private final CardsView helperCards = new CardsView();
    private final Map<UUID, PermanentView> battlefield = new LinkedHashMap<>();
    private final CardView topCard;
    private final UserData userData;
    private final List<CommandObjectView> commandList = new ArrayList<>();
    private final List<UUID> attachments = new ArrayList<>();
    private final int statesSavedSize;
    private final long priorityTimeSavedTimeMs;
    private final int priorityTimeLeftSecs;
    private final int bufferTimeLeft;
    private final boolean passedTurn; // F4
    private final boolean passedUntilEndOfTurn; // F5
    private final boolean passedUntilNextMain; // F6
    private final boolean passedUntilStackResolved; // F8
    private final boolean passedAllTurns; // F9
    private final boolean passedUntilEndStepBeforeMyTurn; // F11
    private final boolean monarch;
    private final boolean initiative;
    private final List<String> designationNames = new ArrayList<>();

    public PlayerView(Player player, GameState state, Game game, UUID createdForPlayerId, UUID watcherUserId) {
        this.playerId = player.getId();
        this.name = player.getName();
        this.controlled = player.getId().equals(createdForPlayerId);
        this.isHuman = player.isHuman();
        this.life = player.getLife();
        this.wins = player.getMatchPlayer().getWins();
        this.winsNeeded = player.getMatchPlayer().getWinsNeeded();
        this.libraryCount = player.getLibrary().size();
        this.handCount = player.getHand().size();
        this.manaPool = new ManaPoolView(player.getManaPool());
        this.isActive = (player.getId().equals(state.getActivePlayerId()));
        this.hasPriority = player.getId().equals(state.getPriorityPlayerId());
        this.priorityTimeLeftSecs = player.getPriorityTimeLeft();
        this.priorityTimeSavedTimeMs = System.currentTimeMillis();
        this.bufferTimeLeft = player.getBufferTimeLeft();
        this.timerActive = (this.hasPriority && player.isGameUnderControl())
                || (player.getPlayersUnderYourControl().contains(state.getPriorityPlayerId()))
                || player.getId().equals(game.getState().getChoosingPlayerId());

        this.hasLeft = player.hasLeft();
        for (Card card : player.getGraveyard().getCards(game)) {
            graveyard.put(card.getId(), new CardView(card, game, CardUtil.canShowAsControlled(card, createdForPlayerId)));
        }
        for (ExileZone exileZone : game.getExile().getExileZones()) {
            for (Card card : exileZone.getCards(game)) {
                if (player.getId().equals(card.getOwnerId())) {
                    exile.put(card.getId(), new CardView(card, game, exileZone.isPlayerAllowedToSeeCard(createdForPlayerId, card)));
                }
            }
        }
        if (this.controlled || !player.isHuman()) {
            // sideboard available for itself or for computer only
            for (Card card : player.getSideboard().getCards(game)) {
                sideboard.put(card.getId(), new CardView(card, game, CardUtil.canShowAsControlled(card, createdForPlayerId)));
            }
        }
        for (Permanent permanent : state.getBattlefield().getAllPermanents()) {
            if (showInBattlefield(permanent, state)) {
                PermanentView view = new PermanentView(permanent, game.getCard(permanent.getId()), createdForPlayerId, game);
                battlefield.put(view.getId(), view);
            }
        }

        Card cardOnTop = (player.isTopCardRevealed() && player.getLibrary().hasCards())
                ? player.getLibrary().getFromTop(game) : null;
        this.topCard = cardOnTop != null ? new CardView(cardOnTop, game) : null;
        if (player.getUserData() != null) {
            this.userData = player.getUserData();
        } else {
            this.userData = UserData.getDefaultUserDataView();
        }

        for (CommandObject commandObject : game.getState().getCommand()) {
            if (commandObject instanceof Emblem) {
                Emblem emblem = (Emblem) commandObject;
                if (emblem.getControllerId().equals(this.playerId)) {
                    commandList.add(new EmblemView(emblem, game));
                }
            } else if (commandObject instanceof Dungeon) {
                Dungeon dungeon = (Dungeon) commandObject;
                if (dungeon.getControllerId().equals(this.playerId)) {
                    commandList.add(new DungeonView(dungeon));
                }
            } else if (commandObject instanceof Plane) {
                Plane plane = (Plane) commandObject;
                // Planes are universal and all players can see them.
                commandList.add(new PlaneView(plane, game));
            } else if (commandObject instanceof Commander) {
                Commander commander = (Commander) commandObject;
                if (commander.getControllerId().equals(this.playerId)) {
                    Card sourceCard = game.getCard(commander.getSourceId());
                    if (sourceCard != null) {
                        commandList.add(new CommanderView(commander, sourceCard, game, createdForPlayerId));
                    }
                }
            }
        }

        if (player.getAttachments() != null) {
            attachments.addAll(player.getAttachments());
        }

        this.statesSavedSize = player.getStoredBookmark();

        this.passedTurn = player.getPassedTurn();
        this.passedUntilEndOfTurn = player.getPassedUntilEndOfTurn();
        this.passedUntilNextMain = player.getPassedUntilNextMain();
        this.passedAllTurns = player.getPassedAllTurns();
        this.passedUntilStackResolved = player.getPassedUntilStackResolved();
        this.passedUntilEndStepBeforeMyTurn = player.getPassedUntilEndStepBeforeMyTurn();
        this.monarch = player.getId().equals(game.getMonarchId());
        this.initiative = player.getId().equals(game.getInitiativeId());
        for (Designation designation : player.getDesignations()) {
            this.designationNames.add(designation.getName());
        }
        this.counters = new ArrayList<>();
        for (Counter counter : player.getCountersAsCopy().values()) {
            counters.add(new CounterView(counter));
        }
    }

    // private constructor for fromProto
    protected PlayerView(ViewProto.PlayerView proto) {
        this.playerId = proto.getPlayerId().isEmpty() ? null : UUID.fromString(proto.getPlayerId());
        this.name = proto.getName();
        this.controlled = proto.getControlled();
        this.isHuman = proto.getIsHuman();
        this.life = proto.getLife();
        this.wins = proto.getWins();
        this.winsNeeded = proto.getWinsNeeded();
        this.libraryCount = proto.getLibraryCount();
        this.handCount = proto.getHandCount();
        this.isActive = proto.getIsActive();
        this.hasPriority = proto.getHasPriority();
        this.timerActive = proto.getTimerActive();
        this.hasLeft = proto.getHasLeft();
        this.manaPool = ManaPoolView.fromProto(proto.getManaPool());
        if (proto.hasTopCard()) {
            this.topCard = CardView.fromProto(proto.getTopCard());
        } else {
            this.topCard = null;
        }
        this.userData = UserData.fromProto(proto.getUserData());
        this.statesSavedSize = proto.getStatesSavedSize();
        this.priorityTimeLeftSecs = proto.getPriorityTimeLeftSecs();
        this.priorityTimeSavedTimeMs = System.currentTimeMillis();
        this.bufferTimeLeft = proto.getBufferTimeLeft();
        this.passedTurn = proto.getPassedTurn();
        this.passedUntilEndOfTurn = proto.getPassedUntilEndOfTurn();
        this.passedUntilNextMain = proto.getPassedUntilNextMain();
        this.passedUntilStackResolved = proto.getPassedUntilStackResolved();
        this.passedAllTurns = proto.getPassedAllTurns();
        this.passedUntilEndStepBeforeMyTurn = proto.getPassedUntilEndStepBeforeMyTurn();
        this.monarch = proto.getMonarch();
        this.initiative = proto.getInitiative();

        this.designationNames.addAll(proto.getDesignationNamesList());
        proto.getAttachmentsList().forEach(att -> this.attachments.add(UUID.fromString(att)));

        this.counters = new ArrayList<>();
        proto.getCountersList().forEach(c -> this.counters.add(CounterView.fromProto(c)));

        proto.getCommandListList().forEach(commandProto -> {
            if (commandBuilderHasEmblem(commandProto)) {
                this.commandList.add(EmblemView.fromProto(commandProto.getEmblem()));
            } else if (commandBuilderHasDungeon(commandProto)) {
                this.commandList.add(DungeonView.fromProto(commandProto.getDungeon()));
            } else if (commandBuilderHasPlane(commandProto)) {
                this.commandList.add(PlaneView.fromProto(commandProto.getPlane()));
            } else if (commandBuilderHasCommander(commandProto)) {
                this.commandList.add(CommanderView.fromProto(commandProto.getCommander()));
            }
        });

        proto.getGraveyardMap().forEach((uid, card) -> this.graveyard.put(UUID.fromString(uid), CardView.fromProto(card)));
        proto.getExileMap().forEach((uid, card) -> this.exile.put(UUID.fromString(uid), CardView.fromProto(card)));
        proto.getSideboardMap().forEach((uid, card) -> this.sideboard.put(UUID.fromString(uid), CardView.fromProto(card)));
        proto.getHelperCardsMap().forEach((uid, card) -> this.helperCards.put(UUID.fromString(uid), CardView.fromProto(card)));
        proto.getBattlefieldMap().forEach((uid, perm) -> this.battlefield.put(UUID.fromString(uid), PermanentView.fromProto(perm)));

    }

    private boolean showInBattlefield(Permanent permanent, GameState state) {

        //show permanents controlled by player or attachments to permanents controlled by player
        if (permanent.getAttachedTo() == null) {
            return permanent.getControllerId().equals(playerId);
        } else {
            Permanent attachedTo = state.getPermanent(permanent.getAttachedTo());
            if (attachedTo != null) {
                return attachedTo.getControllerId().equals(playerId);
            } else {
                return permanent.getControllerId().equals(playerId);
            }
        }
    }

    public boolean getControlled() {
        return this.controlled;
    }

    public boolean isHuman() {
        return this.isHuman;
    }

    public int getLife() {
        return this.life;
    }

    public List<CounterView> getCounters() {
        return this.counters;
    }

    public int getLibraryCount() {
        return this.libraryCount;
    }

    public int getWins() {
        return wins;
    }

    public int getWinsNeeded() {
        return winsNeeded;
    }

    public int getHandCount() {
        return this.handCount;
    }

    public ManaPoolView getManaPool() {
        return this.manaPool;
    }

    public CardsView getGraveyard() {
        return this.graveyard;
    }

    public CardsView getExile() {
        return exile;
    }

    public CardsView getSideboard() {
        return this.sideboard;
    }

    public Map<UUID, PermanentView> getBattlefield() {
        return this.battlefield;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public String getName() {
        return this.name;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public boolean hasLeft() {
        return this.hasLeft;
    }

    public CardView getTopCard() {
        return this.topCard;
    }

    public UserData getUserData() {
        return this.userData;
    }

    public List<CommandObjectView> getCommandObjectList() {
        return commandList;
    }

    public List<UUID> getAttachments() {
        return attachments;
    }

    public boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }

    public int getStatesSavedSize() {
        return statesSavedSize;
    }

    public int getPriorityTimeLeftSecs() {
        // workaround to find real time
        int secsAfterUpdate = (int) ((System.currentTimeMillis() - this.priorityTimeSavedTimeMs) / 1000);
        return Math.max(0, this.priorityTimeLeftSecs - secsAfterUpdate);
    }

    public int getBufferTimeLeft() {
        return bufferTimeLeft;
    }

    public boolean hasPriority() {
        return hasPriority;
    }

    public boolean isTimerActive() {
        return timerActive;
    }

    public boolean isPassedTurn() {
        return passedTurn;
    }

    public boolean isPassedUntilEndOfTurn() {
        return passedUntilEndOfTurn;
    }

    public boolean isPassedUntilNextMain() {
        return passedUntilNextMain;
    }

    public boolean isPassedAllTurns() {
        return passedAllTurns;
    }

    public boolean isPassedUntilStackResolved() {
        return passedUntilStackResolved;
    }

    public boolean isPassedUntilEndStepBeforeMyTurn() {
        return passedUntilEndStepBeforeMyTurn;
    }

    public boolean isMonarch() {
        return monarch;
    }

    public boolean isInitiative() {
        return initiative;
    }

    public List<String> getDesignationNames() {
        return designationNames;
    }


    public ViewProto.PlayerView toProto() {
        ViewProto.PlayerView.Builder builder = ViewProto.PlayerView.newBuilder()
                .setPlayerId(playerId != null ? playerId.toString() : "")
                .setName(name != null ? name : "")
                .setControlled(controlled)
                .setIsHuman(isHuman)
                .setLife(life)
                .setWins(wins)
                .setWinsNeeded(winsNeeded)
                .setLibraryCount(libraryCount)
                .setHandCount(handCount)
                .setIsActive(isActive)
                .setHasPriority(hasPriority)
                .setTimerActive(timerActive)
                .setHasLeft(hasLeft)
                .setManaPool(manaPool != null ? manaPool.toProto() : ViewProto.ManaPoolView.getDefaultInstance())
                .setUserData(userData != null ? userData.toProto() : ModelProto.UserData.getDefaultInstance())
                .setStatesSavedSize(statesSavedSize)
                .setPriorityTimeLeftSecs(getPriorityTimeLeftSecs())
                .setBufferTimeLeft(bufferTimeLeft)
                .setPassedTurn(passedTurn)
                .setPassedUntilEndOfTurn(passedUntilEndOfTurn)
                .setPassedUntilNextMain(passedUntilNextMain)
                .setPassedUntilStackResolved(passedUntilStackResolved)
                .setPassedAllTurns(passedAllTurns)
                .setPassedUntilEndStepBeforeMyTurn(passedUntilEndStepBeforeMyTurn)
                .setMonarch(monarch)
                .setInitiative(initiative);

        if (topCard != null) {
            builder.setTopCard(topCard.toCardViewProto());
        }

        builder.addAllDesignationNames(designationNames);

        builder.addAllAttachments(attachments.stream().map(UUID::toString).collect(Collectors.toList()));

        if (counters != null) {
            for (CounterView counter : counters) {
                builder.addCounters(counter.toProto());
            }
        }

        for (CommandObjectView command : commandList) {
            ViewProto.CommandObjectView.Builder commandBuilder = ViewProto.CommandObjectView.newBuilder();
            if (command instanceof EmblemView) {
                commandBuilder.setEmblem(((EmblemView) command).toProto());
            } else if (command instanceof DungeonView) {
                commandBuilder.setDungeon(((DungeonView) command).toProto());
            } else if (command instanceof PlaneView) {
                commandBuilder.setPlane(((PlaneView) command).toProto());
            } else if (command instanceof CommanderView) {
                commandBuilder.setCommander(((CommanderView) command).toCommanderViewProto());
            }
            builder.addCommandList(commandBuilder.build());
        }

        graveyard.forEach((uid, card) -> builder.putGraveyard(uid.toString(), card.toCardViewProto()));
        exile.forEach((uid, card) -> builder.putExile(uid.toString(), card.toCardViewProto()));
        sideboard.forEach((uid, card) -> builder.putSideboard(uid.toString(), card.toCardViewProto()));
        helperCards.forEach((uid, card) -> builder.putHelperCards(uid.toString(), card.toCardViewProto()));
        battlefield.forEach((uid, perm) -> builder.putBattlefield(uid.toString(), perm.toPermanentViewProto()));

        return builder.build();
    }

    public static PlayerView fromProto(ViewProto.PlayerView proto) {
        return new PlayerView(proto);
    }

    private static boolean commandBuilderHasEmblem(ViewProto.CommandObjectView commandProto) {
        return commandProto.getTypeCase() == ViewProto.CommandObjectView.TypeCase.EMBLEM;
    }

    private static boolean commandBuilderHasDungeon(ViewProto.CommandObjectView commandProto) {
        return commandProto.getTypeCase() == ViewProto.CommandObjectView.TypeCase.DUNGEON;
    }

    private static boolean commandBuilderHasPlane(ViewProto.CommandObjectView commandProto) {
        return commandProto.getTypeCase() == ViewProto.CommandObjectView.TypeCase.PLANE;
    }

    private static boolean commandBuilderHasCommander(ViewProto.CommandObjectView commandProto) {
        return commandProto.getTypeCase() == ViewProto.CommandObjectView.TypeCase.COMMANDER;
    }
}
