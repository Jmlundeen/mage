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
    private boolean flipped;
    private boolean phasedIn;
    private boolean summoningSickness;
    private int damage;
    private List<UUID> attachments;
    private CardView original; // original card before transforms and modifications (null for opponents face down cards)
    private boolean copy;
    private String nameOwner; // only filled if != controller
    private String nameController;
    private boolean controlled;
    private UUID attachedTo;
    private boolean morphed;
    private boolean disguised;
    private boolean manifested;
    private boolean cloaked;
    private boolean attachedToPermanent;
    // If this card is attached to a permanent which is controlled by a player other than the one which controls this permanent
    private boolean attachedControllerDiffers;

    protected PermanentView() {
        super();
    }

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
        PermanentView view = new PermanentView();
        // fill CardView fields
        CardView cardView = CardView.fromProto(proto.getCardView());
        view.id = cardView.id;
        view.expansionSetCode = cardView.expansionSetCode;
        view.cardNumber = cardView.cardNumber;
        view.usesVariousArt = cardView.usesVariousArt;
        view.gameObject = cardView.gameObject;
        view.isChoosable = cardView.isChoosable;
        view.isSelected = cardView.isSelected;
        view.playableStats = cardView.playableStats;

        view.parentId = cardView.parentId;
        view.name = cardView.name;
        view.displayName = cardView.displayName;
        view.displayFullName = cardView.displayFullName;
        view.rules = cardView.rules;
        view.power = cardView.power;
        view.toughness = cardView.toughness;
        view.loyalty = cardView.loyalty;
        view.defense = cardView.defense;
        view.startingLoyalty = cardView.startingLoyalty;
        view.startingDefense = cardView.startingDefense;
        view.cardTypes = cardView.cardTypes;
        view.subTypes = cardView.subTypes;
        view.superTypes = cardView.superTypes;
        view.color = cardView.color;
        view.frameColor = cardView.frameColor;
        view.frameStyle = cardView.frameStyle;
        view.manaCostLeftStr = cardView.manaCostLeftStr;
        view.manaCostRightStr = cardView.manaCostRightStr;
        view.manaValue = cardView.manaValue;
        view.rarity = cardView.rarity;
        view.mageObjectType = cardView.mageObjectType;
        view.isAbility = cardView.isAbility;
        view.abilityType = cardView.abilityType;
        view.isToken = cardView.isToken;
        view.ability = cardView.ability;
        view.imageFileName = cardView.imageFileName;
        view.imageNumber = cardView.imageNumber;
        view.extraDeckCard = cardView.extraDeckCard;
        view.transformable = cardView.transformable;
        view.secondCardFace = cardView.secondCardFace;
        view.transformed = cardView.transformed;
        view.flipCard = cardView.flipCard;
        view.faceDown = cardView.faceDown;
        view.alternateName = cardView.alternateName;
        view.alternateNumber = cardView.alternateNumber;
        view.isSplitCard = cardView.isSplitCard;
        view.leftSplitName = cardView.leftSplitName;
        view.leftSplitCostsStr = cardView.leftSplitCostsStr;
        view.leftSplitRules = cardView.leftSplitRules;
        view.leftSplitTypeLine = cardView.leftSplitTypeLine;
        view.rightSplitName = cardView.rightSplitName;
        view.rightSplitCostsStr = cardView.rightSplitCostsStr;
        view.rightSplitRules = cardView.rightSplitRules;
        view.rightSplitTypeLine = cardView.rightSplitTypeLine;
        view.isDoubleFacedCard = cardView.isDoubleFacedCard;
        view.artRect = cardView.artRect;
        view.targets = cardView.targets;
        view.pairedCard = cardView.pairedCard;
        view.bandedCards = cardView.bandedCards;
        view.paid = cardView.paid;
        view.counters = cardView.counters;
        view.controlledByOwner = cardView.controlledByOwner;
        view.zone = cardView.zone;
        view.rotate = cardView.rotate;
        view.hideInfo = cardView.hideInfo;
        view.canAttack = cardView.canAttack;
        view.canBlock = cardView.canBlock;
        view.inViewerOnly = cardView.inViewerOnly;
        view.cardIcons = cardView.cardIcons;
        view.originalPower = cardView.originalPower;
        view.originalToughness = cardView.originalToughness;
        view.originalColorIdentity = cardView.originalColorIdentity;
        view.originalIsCopy = cardView.originalIsCopy;

        // fill PermanentView fields
        view.tapped = proto.getTapped();
        view.flipped = proto.getFlipped();
        view.phasedIn = proto.getPhasedIn();
        view.summoningSickness = proto.getSummoningSickness();
        view.damage = proto.getDamage();
        view.attachments = proto.getAttachmentsList().stream().map(UUID::fromString).collect(Collectors.toList());
        if (proto.hasOriginal()) {
            view.original = CardView.fromProto(proto.getOriginal());
        }
        view.copy = proto.getCopy();
        view.nameOwner = proto.getNameOwner();
        view.nameController = proto.getNameController();
        view.controlled = proto.getControlled();
        view.attachedTo = proto.getAttachedTo().isEmpty() ? null : UUID.fromString(proto.getAttachedTo());
        view.morphed = proto.getMorphed();
        view.disguised = proto.getDisguised();
        view.manifested = proto.getManifested();
        view.cloaked = proto.getCloaked();
        view.attachedToPermanent = proto.getAttachedToPermanent();
        view.attachedControllerDiffers = proto.getAttachedControllerDiffers();

        return view;
    }
}
