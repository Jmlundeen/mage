package mage.abilities.effects.common;

import mage.MageItem;
import mage.MageObject;
import mage.MageObjectReference;
import mage.abilities.Abilities;
import mage.abilities.Ability;
import mage.abilities.common.RoomAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.cards.Card;
import mage.cards.FlipCard;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.PermanentCard;
import mage.game.permanent.PermanentToken;
import mage.util.CardUtil;
import mage.util.functions.CopyApplier;

import java.util.List;
import java.util.UUID;

/**
 * Make battlefield's permanent as a copy of the source object
 * (source can be a card or another permanent)
 *
 * @author BetaSteward_at_googlemail.com
 */
public class CopyEffect extends ContinuousEffectImpl {

    protected MageObject copyFromObject;
    protected UUID copyToObjectId;
    protected CopyApplier applier;

    public CopyEffect(MageObject copyFromObject, UUID copyToObjectId) {
        this(Duration.Custom, copyFromObject, copyToObjectId);
    }

    public CopyEffect(Duration duration, MageObject copyFromObject, UUID copyToObjectId) {
        super(duration, Layer.CopyEffects_1, SubLayer.CopyEffects_1a, Outcome.BecomeCreature);
        this.copyFromObject = copyFromObject;
        this.copyToObjectId = copyToObjectId;
    }

    protected CopyEffect(final CopyEffect effect) {
        super(effect);
        this.copyFromObject = effect.copyFromObject.copy();
        this.copyToObjectId = effect.copyToObjectId;
        this.applier = effect.applier;
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);

        // must copy the default side of the card (example: clone with mdf card)
        if (!(copyFromObject instanceof Permanent) && (copyFromObject instanceof Card)) {
            Card newBluePrint = CardUtil.getDefaultCardSideForBattlefield(game, (Card) copyFromObject);
            this.copyFromObject = new PermanentCard(newBluePrint, source.getControllerId(), game);
        }

        Permanent permanent = game.getPermanent(copyToObjectId);
        if (permanent != null) {
            affectedObjectList.add(new MageObjectReference(permanent, game));
            copyToPermanent(permanent, game, source);
        } else if (source.getAbilityType() == AbilityType.STATIC) {
            // for replacement effects that let a permanent enter the battlefield as a copy of another permanent we need to apply that copy
            // before the permanent is added to the battlefield
            permanent = game.getPermanentEntering(copyToObjectId);
            if (permanent != null) {
                copyToPermanent(permanent, game, source);
                // Apply Room characteristics since effects aren't applied to entering permanents yet
                if (permanent.hasSubtype(SubType.ROOM, game)) {
                    Abilities<Ability> abilities = permanent.getAbilities();
                    for (Ability ability : abilities) {
                        if (ability instanceof RoomAbility) {
                            ((RoomAbility) ability).applyCharacteristics(game, permanent);
                            break;
                        }
                    }
                }
                // set reference to the permanent later on the battlefield so we have to add already one (if no token) to the zone change counter
                int ZCCDiff = 1;
                if (permanent instanceof PermanentToken) {
                    ZCCDiff = 0;
                }
                MageObjectReference mor = new MageObjectReference(permanent.getId(), game.getState().getZoneChangeCounter(copyToObjectId) + ZCCDiff, game);
                if (!affectedObjectList.contains(mor)) {
                    affectedObjectList.add(mor);
                }
            }
        } else {
            permanent = game.getPermanentEntering(copyToObjectId);
            int ZCCDiff = 1;
            if ((permanent instanceof PermanentToken)) {
                // Tokens already have battlefield ZCC when they are created
                ZCCDiff = 0;
            }
            MageObjectReference mor = new MageObjectReference(permanent.getId(), game.getState().getZoneChangeCounter(copyToObjectId) + ZCCDiff, game);
            if (!affectedObjectList.contains(mor)) {
                affectedObjectList.add(mor);
            }
        }
        if (permanent != null && applier != null) {
            applier.applyExceptions(game, permanent, source);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageObjectReference mor : affectedObjectList) {
            Permanent permanent = mor.getPermanent(game);
            if (permanent == null) {
                permanent = game.getPermanentEntering(mor.getSourceId());
            }
            if (permanent == null) {
                if (!game.checkShortLivingLKI(getSourceId(), Zone.BATTLEFIELD)) {
                    this.discard();
                    return false;
                }

                // As long as the permanent is still in the short living LKI continue to copy to get triggered abilities to TriggeredAbilities for dies events.
                permanent = (Permanent) game.getLastKnownInformation(getSourceId(), Zone.BATTLEFIELD, source.getStackMomentSourceZCC());
                if (permanent == null) {
                    this.discard();
                    return false;
                }
            }
            affectedObjects.add(permanent);
        }
        return !affectedObjects.isEmpty();
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            copyToPermanent((Permanent) object, game, source);
        }
    }

    protected void copyToPermanent(Permanent permanent, Game game, Ability source) {
        // copy object to object
        permanent.setCopy(true, copyFromObject);
        MageObject copyFrom = copyFromObject;
        if (permanent.isFlipped() && copyFrom instanceof Card && ((Card) copyFrom).getMainCard() instanceof FlipCard) {
            copyFrom = ((FlipCard) ((Card) copyFrom).getMainCard()).getRightHalfCard().copy();
            applier.apply(game, copyFrom, source, permanent.getId());
        }
        permanent.setName(copyFrom.getName());
        permanent.getColor(game).setColor(copyFrom.getColor());
        permanent.getManaCost().clear();
        permanent.getManaCost().add(copyFrom.getManaCost().copy());
        permanent.removeAllCardTypes(game);
        for (CardType type : copyFrom.getCardType()) {
            permanent.addCardType(game, type);
        }

        permanent.removeAllSubTypes(game);
        for (SubType type : copyFrom.getSubtype()) {
            permanent.addSubType(game, type);
        }

        permanent.removeAllSuperTypes(game);
        for (SuperType type : copyFrom.getSuperType()) {
            permanent.addSuperType(game, type);
        }

        permanent.removeAllAbilities(source.getSourceId(), game);
        for (Ability ability : copyFrom.getAbilities()) {
            permanent.addAbility(ability, getSourceId(), game, true);
        }

        // Primal Clay example:
        // If a creature that’s already on the battlefield becomes a copy of this creature, it copies the power, toughness,
        // and abilities that were chosen for this creature as it entered the battlefield. (2018-03-16)
        permanent.getPower().setModifiedBaseValue(copyFrom.getPower().getModifiedBaseValue());
        permanent.getToughness().setModifiedBaseValue(copyFrom.getToughness().getModifiedBaseValue());
        permanent.setStartingLoyalty(copyFrom.getStartingLoyalty());
        permanent.setStartingDefense(copyFrom.getStartingDefense());
        if (copyFrom instanceof Permanent) {
            Permanent targetPermanent = (Permanent) copyFrom;
            //707.2. When copying an object, the copy acquires the copiable values of the original object’s characteristics [..]
            //110.5. A permanent's status is its physical state. There are four status categories, each of which has two possible values:
            // tapped/untapped, flipped/unflipped, face up/face down, and phased in/phased out.
            // Each permanent always has one of these values for each of these categories.
            //110.5a Status is not a characteristic, though it may affect a permanent’s characteristics.
            //Being transformed is not a copiable characteristic, nor is the back side of a DFC
            //permanent.setTransformed(targetPermanent.isTransformed());
            //permanent.setSecondCardFace(targetPermanent.getSecondCardFace());
            permanent.setPrototyped(targetPermanent.isPrototyped());
        }

        CardUtil.copySetAndCardNumber(permanent, copyFrom);

        permanent.saveCopiableValues(game);
    }

    @Override
    public CopyEffect copy() {
        return new CopyEffect(this);
    }

    public MageObject getTarget() {
        return copyFromObject;
    }

    public void setTarget(MageObject target) {
        this.copyFromObject = target;
    }

    public UUID getSourceId() {
        return copyToObjectId;
    }

    public CopyApplier getApplier() {
        return applier;
    }

    public CopyEffect setApplier(CopyApplier applier) {
        this.applier = applier;
        return this;
    }

}
