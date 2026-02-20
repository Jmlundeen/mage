package mage.view;

import mage.cards.Card;
import mage.constants.MageObjectType;
import mage.game.Game;
import mage.game.command.Commander;
import mage.util.CardUtil;
import mage.ws.view.ViewProto;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author Plopman
 */
public class CommanderView extends CardView implements CommandObjectView, Serializable{

    protected CommanderView() {
        super();
    }

    public CommanderView(Commander commander, Card sourceCard, Game game, UUID createdForPlayerId) {
        super(sourceCard, game, CardUtil.canShowAsControlled(sourceCard, createdForPlayerId));
        this.mageObjectType = MageObjectType.COMMANDER;
    }

    public ViewProto.CommanderView toCommanderViewProto() {
        return ViewProto.CommanderView.newBuilder()
                .setCardView(this.toCardViewProto())
                .build();
    }

    public static CommanderView fromProto(ViewProto.CommanderView proto) {
        CommanderView view = new CommanderView();
        CardView cardView = CardView.fromProto(proto.getCardView());
        // Copy fields from CardView manually since we don't have a constructor for it
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

        return view;
    }
}
