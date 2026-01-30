package mage.view;

import mage.cards.Card;
import mage.cards.FlipCardHalf;
import mage.cards.RoomCard;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.PermanentToken;
import mage.game.permanent.token.Token;
import mage.players.Player;
import mage.util.CardUtil;
import mage.ws.v1.view.ViewProto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class PermanentView extends CardView {

    private static final long serialVersionUID = 1L;

    private boolean tapped;
    private final boolean flipped;
    private final boolean phasedIn;
    private final boolean summoningSickness;
    private final int damage;
    private List<UUID> attachments;
    private final CardView original; // original card before transforms and modifications (null for opponents face down cards)
    private final boolean copy;
    private final String nameOwner; // only filled if != controller
    private final String nameController;
    private final boolean controlled;
    private final UUID attachedTo;
    private final boolean morphed;
    private final boolean disguised;
    private final boolean manifested;
    private final boolean cloaked;
    private final boolean attachedToPermanent;
    // If this card is attached to a permanent which is controlled by a player other than the one which controls this permanent
    private final boolean attachedControllerDiffers;

    public PermanentView(Permanent permanent, Card card, UUID createdForPlayerId, Game game) {
        super(permanent, game, CardUtil.canShowAsControlled(permanent, createdForPlayerId));
        this.controlled = permanent.getControllerId() != null && permanent.getControllerId().equals(createdForPlayerId);
        this.tapped = permanent.isTapped();
        this.flipped = permanent.isFlipped();
        this.phasedIn = permanent.isPhasedIn();
        this.summoningSickness = permanent.hasSummoningSickness();
        this.morphed = permanent.isMorphed();
        this.disguised = permanent.isDisguised();
        this.manifested = permanent.isManifested();
        this.cloaked = permanent.isCloaked();
        this.damage = permanent.getDamage();
        this.attachments = new ArrayList<>(permanent.getAttachments());
        this.attachedTo = permanent.getAttachedTo();

        // store original card, e.g. for sides switch in GUI
        boolean showFaceDownInfo = controlled || (game != null && game.hasEnded());
        this.copy = permanent.isCopy();
        if (copy) {
            // Handle the case where the permanent is a copy
            original = isToken
                ? new CardView(((PermanentToken) permanent).getToken().copy(), null)
                : new CardView(card.copy(), (Game) null);
            this.setAlternateName(original.getName());
            this.transformable = false;
        } else if (permanent.getOtherFace() != null) {
            // Handle the case where the permanent has another face
            original = isToken
                ? new CardView((Token) permanent.getOtherFace().copy(), null)
                : new CardView((Card) permanent.getOtherFace().copy(), (Game) null);
        } else if (isFaceDown() && showFaceDownInfo) {
            // face down card must be hidden from opponent, but shown on game end for all
            if (isToken) {
                Token token = ((PermanentToken) permanent).getToken().copy();
                token.setFaceDown(false);
                original = new CardView(token, null);
            } else {
                Card cardCopy = card.copy();
                cardCopy.setFaceDown(false);
                original = new CardView(cardCopy, (Game) null);
            }
        } else {
            // Default case where no original card is available
            original = null;
        }

        if (!copy && card instanceof RoomCard || card instanceof FlipCardHalf) {
            this.imageFileName = card.getName();
        }

        if (permanent.getOwnerId() != null && !permanent.getOwnerId().equals(permanent.getControllerId())) {
            Player owner = game.getPlayer(permanent.getOwnerId());
            if (owner != null) {
                this.nameOwner = owner.getName();
            } else {
                this.nameOwner = "";
            }
        } else {
            this.nameOwner = "";
        }

        String nameController = "";
        if (game != null) {
            Player controller = game.getPlayer(permanent.getControllerId());
            if (controller != null) {
                nameController = controller.getName();
            }
        }
        this.nameController = nameController;

        // determines if shown in it's own column
        boolean attachedToPermanent = false;
        boolean attachedControllerDiffers = false;
        if (game != null) {
            Permanent attachment = game.getPermanent(permanent.getAttachedTo());
            if (attachment != null) {
                attachedToPermanent = true;
                attachedControllerDiffers = !attachment.getControllerId().equals(permanent.getControllerId());
            }
        }
        this.attachedToPermanent = attachedToPermanent;
        this.attachedControllerDiffers = attachedControllerDiffers;
    }

    public PermanentView(PermanentView permanentView, Card card, UUID createdForPlayerId, Game game) {
        super(permanentView);
        this.controlled = permanentView.controlled;
        this.tapped = permanentView.isTapped();
        this.flipped = permanentView.isFlipped();
        this.phasedIn = permanentView.isPhasedIn();
        this.summoningSickness = permanentView.summoningSickness;
        this.damage = permanentView.damage;
        this.attachments = new ArrayList<>(permanentView.attachments);

        boolean showFaceDownInfo = controlled || (game != null && game.hasEnded());

        if (isToken()) {
            original = new CardView(permanentView.original);
            original.expansionSetCode = permanentView.original.getExpansionSetCode();
            expansionSetCode = permanentView.original.getExpansionSetCode();
        } else {
            if (card != null && showFaceDownInfo) {
                // face down card must be hidden from opponent, but shown on game end for all
                original = new CardView(card.copy(), (Game) null);
            } else {
                original = null;
            }
        }

        this.copy = permanentView.copy;
        this.nameOwner = permanentView.nameOwner;
        this.nameController = permanentView.nameController;
        this.attachedTo = permanentView.attachedTo;
        this.morphed = permanentView.morphed;
        this.disguised = permanentView.disguised;
        this.manifested = permanentView.manifested;
        this.cloaked = permanentView.cloaked;
        this.attachedToPermanent = permanentView.attachedToPermanent;
        this.attachedControllerDiffers = permanentView.attachedControllerDiffers;
    }

    // private constructor for fromProto
    private PermanentView(ViewProto.PermanentView proto) {
        CardView cardView = CardView.fromProto(proto.getCardView());
        this.id = cardView.id;
        this.expansionSetCode = cardView.expansionSetCode;
        this.cardNumber = cardView.cardNumber;
        this.usesVariousArt = cardView.usesVariousArt;
        this.gameObject = cardView.gameObject;
        this.isChoosable = cardView.isChoosable;
        this.isSelected = cardView.isSelected;
        this.playableStats = cardView.playableStats;

        this.parentId = cardView.parentId;
        this.name = cardView.name;
        this.displayName = cardView.displayName;
        this.displayFullName = cardView.displayFullName;
        this.rules = cardView.rules;
        this.power = cardView.power;
        this.toughness = cardView.toughness;
        this.loyalty = cardView.loyalty;
        this.defense = cardView.defense;
        this.startingLoyalty = cardView.startingLoyalty;
        this.startingDefense = cardView.startingDefense;
        this.cardTypes = cardView.cardTypes;
        this.subTypes = cardView.subTypes;
        this.superTypes = cardView.superTypes;
        this.color = cardView.color;
        this.frameColor = cardView.frameColor;
        this.frameStyle = cardView.frameStyle;
        this.manaCostLeftStr = cardView.manaCostLeftStr;
        this.manaCostRightStr = cardView.manaCostRightStr;
        this.manaValue = cardView.manaValue;
        this.rarity = cardView.rarity;
        this.mageObjectType = cardView.mageObjectType;
        this.isAbility = cardView.isAbility;
        this.abilityType = cardView.abilityType;
        this.isToken = cardView.isToken;
        this.ability = cardView.ability;
        this.imageFileName = cardView.imageFileName;
        this.imageNumber = cardView.imageNumber;
        this.extraDeckCard = cardView.extraDeckCard;
        this.transformable = cardView.transformable;
        this.secondCardFace = cardView.secondCardFace;
        this.transformed = cardView.transformed;
        this.flipCard = cardView.flipCard;
        this.faceDown = cardView.faceDown;
        this.alternateName = cardView.alternateName;
        this.alternateNumber = cardView.alternateNumber;
        this.isSplitCard = cardView.isSplitCard;
        this.leftSplitName = cardView.leftSplitName;
        this.leftSplitCostsStr = cardView.leftSplitCostsStr;
        this.leftSplitRules = cardView.leftSplitRules;
        this.leftSplitTypeLine = cardView.leftSplitTypeLine;
        this.rightSplitName = cardView.rightSplitName;
        this.rightSplitCostsStr = cardView.rightSplitCostsStr;
        this.rightSplitRules = cardView.rightSplitRules;
        this.rightSplitTypeLine = cardView.rightSplitTypeLine;
        this.isDoubleFacedCard = cardView.isDoubleFacedCard;
        this.artRect = cardView.artRect;
        this.targets = cardView.targets;
        this.pairedCard = cardView.pairedCard;
        this.bandedCards = cardView.bandedCards;
        this.paid = cardView.paid;
        this.counters = cardView.counters;
        this.controlledByOwner = cardView.controlledByOwner;
        this.zone = cardView.zone;
        this.rotate = cardView.rotate;
        this.hideInfo = cardView.hideInfo;
        this.canAttack = cardView.canAttack;
        this.canBlock = cardView.canBlock;
        this.inViewerOnly = cardView.inViewerOnly;
        this.cardIcons = cardView.cardIcons;
        this.originalPower = cardView.originalPower;
        this.originalToughness = cardView.originalToughness;
        this.originalColorIdentity = cardView.originalColorIdentity;
        this.originalIsCopy = cardView.originalIsCopy;

        // fill PermanentView fields
        this.tapped = proto.getTapped();
        this.flipped = proto.getFlipped();
        this.phasedIn = proto.getPhasedIn();
        this.summoningSickness = proto.getSummoningSickness();
        this.damage = proto.getDamage();
        this.attachments = proto.getAttachmentsList().stream().map(UUID::fromString).collect(Collectors.toList());
        if (proto.hasOriginal()) {
            this.original = CardView.fromProto(proto.getOriginal());
        } else {
            this.original = null;
        }
        this.copy = proto.getCopy();
        this.nameOwner = proto.getNameOwner();
        this.nameController = proto.getNameController();
        this.controlled = proto.getControlled();
        this.attachedTo = proto.getAttachedTo().isEmpty() ? null : UUID.fromString(proto.getAttachedTo());
        this.morphed = proto.getMorphed();
        this.disguised = proto.getDisguised();
        this.manifested = proto.getManifested();
        this.cloaked = proto.getCloaked();
        this.attachedToPermanent = proto.getAttachedToPermanent();
        this.attachedControllerDiffers = proto.getAttachedControllerDiffers();
    }

    public boolean isTapped() {
        return tapped;
    }

    public int getDamage() {
        return damage;
    }

    public boolean isFlipped() {
        return flipped;
    }

    public boolean isCopy() {
        return copy;
    }

    public boolean isPhasedIn() {
        return phasedIn;
    }

    public boolean hasSummoningSickness() {
        return summoningSickness;
    }

    public List<UUID> getAttachments() {
        return attachments;
    }

    public CardView getOriginal() {
        return original;
    }

    public void overrideTapped(boolean tapped) {
        this.tapped = tapped;
    }

    public String getNameOwner() {
        return nameOwner;
    }

    public String getNameController() {
        return nameController;
    }

    public boolean isControlled() {
        return controlled;
    }

    public UUID getAttachedTo() {
        return attachedTo;
    }

    public boolean isAttachedTo() {
        return attachedTo != null;
    }

    public boolean isAttachedToPermanent() {
        return attachedToPermanent;
    }

    public boolean isAttachedToDifferentlyControlledPermanent() {
        return attachedControllerDiffers;
    }

    public boolean isMorphed() {
        return morphed;
    }

    public boolean isDisguised() {
        return disguised;
    }

    public boolean isManifested() {
        return manifested;
    }

    public boolean isCloaked() {
        return cloaked;
    }

    public ViewProto.PermanentView toPermanentViewProto() {
        ViewProto.PermanentView.Builder builder = ViewProto.PermanentView.newBuilder()
                .setCardView(this.toCardViewProto())
                .setTapped(tapped)
                .setFlipped(flipped)
                .setPhasedIn(phasedIn)
                .setSummoningSickness(summoningSickness)
                .setDamage(damage)
                .addAllAttachments(attachments != null ? attachments.stream().map(UUID::toString).collect(Collectors.toList()) : new ArrayList<>())
                .setCopy(copy)
                .setNameOwner(nameOwner != null ? nameOwner : "")
                .setNameController(nameController != null ? nameController : "")
                .setControlled(controlled)
                .setAttachedTo(attachedTo == null ? "" : attachedTo.toString())
                .setMorphed(morphed)
                .setDisguised(disguised)
                .setManifested(manifested)
                .setCloaked(cloaked)
                .setAttachedToPermanent(attachedToPermanent)
                .setAttachedControllerDiffers(attachedControllerDiffers);

        if (original != null) {
            builder.setOriginal(original.toCardViewProto());
        }

        return builder.build();
    }

    public static PermanentView fromProto(ViewProto.PermanentView proto) {
        return new PermanentView(proto);
    }
}
