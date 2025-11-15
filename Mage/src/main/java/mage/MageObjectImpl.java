package mage;

import mage.abilities.Abilities;
import mage.abilities.AbilitiesImpl;
import mage.abilities.Ability;
import mage.abilities.costs.mana.ManaCost;
import mage.abilities.costs.mana.ManaCosts;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.Effect;
import mage.abilities.mana.ActivatedManaAbilityImpl;
import mage.cards.CopiableValues;
import mage.cards.FrameStyle;
import mage.cards.mock.MockCard;
import mage.constants.*;
import mage.game.Game;
import mage.game.MageObjectAttribute;
import mage.game.events.ZoneChangeEvent;
import mage.util.GameLog;
import mage.util.SubTypes;
import org.apache.log4j.Logger;

import java.util.*;

public abstract class MageObjectImpl implements MageObject {

    private static final Logger logger = Logger.getLogger(MageObjectImpl.class);

    protected UUID objectId;

    protected String name;
    protected ManaCosts<ManaCost> manaCost;

    protected ObjectColor color;
    protected ObjectColor frameColor;
    protected FrameStyle frameStyle;

    private String expansionSetCode = "";
    private String cardNumber = "";
    private boolean usesVariousArt = false;
    private String imageFileName = "";
    private int imageNumber = 0;

    protected List<SuperType> supertype = new ArrayList<>();
    protected List<CardType> cardType = new ArrayList<>();
    protected SubTypes subtype = new SubTypes();
    protected Abilities<Ability> abilities;

    protected String text;
    protected MageInt power;
    protected MageInt toughness;
    protected int startingLoyalty = -1; // -2 means X, -1 means none, 0 and up is normal
    protected int startingDefense = -1; // -2 means X, -1 means none, 0 and up is normal

    protected boolean copy;
    protected MageObject copyFrom; // copied card INFO (used to call original adjusters)
    protected CopiableValues faceDownValues = new CopiableValues(true);
    protected boolean faceDown;

    public MageObjectImpl() {
        this(UUID.randomUUID());
    }

    public MageObjectImpl(UUID id) {
        objectId = id;
        power = new MageInt(0);
        toughness = new MageInt(0);
        color = new ObjectColor();
        frameColor = new ObjectColor();
        frameStyle = FrameStyle.M15_NORMAL;
        manaCost = new ManaCostsImpl<>();
        abilities = new AbilitiesImpl<>();
    }

    protected MageObjectImpl(final MageObjectImpl object) {
        objectId = object.objectId;
        name = object.name;
        manaCost = object.manaCost.copy();
        text = object.text;
        color = object.color.copy();
        frameColor = object.frameColor.copy();
        frameStyle = object.frameStyle;
        expansionSetCode = object.expansionSetCode;
        usesVariousArt = object.usesVariousArt;
        cardNumber = object.cardNumber;
        imageFileName = object.imageFileName;
        imageNumber = object.imageNumber;
        power = object.power.copy();
        toughness = object.toughness.copy();
        startingLoyalty = object.startingLoyalty;
        startingDefense = object.startingDefense;
        abilities = object.abilities.copy();
        this.cardType.addAll(object.cardType);
        this.subtype.copyFrom(object.subtype);
        supertype.addAll(object.supertype);
        this.copy = object.copy;
        this.copyFrom = (object.copyFrom != null ? object.copyFrom.copy() : null);
        this.faceDownValues = object.faceDownValues.copy();
        this.faceDown = object.faceDown;
    }

    @Override
    public UUID getId() {
        return objectId;
    }

    @Override
    public String getName() {
        if (faceDown) {
            return faceDownValues.getName();
        }
        return name;
    }

    @Override
    public String getIdName() {
        return getName() + " [" + getId().toString().substring(0, 3) + ']';
    }

    @Override
    public String getLogName() {
        return GameLog.getColoredObjectIdName(this);
    }

    @Override
    public void setName(String name) {
        if (faceDown) {
            faceDownValues.setName(name);
            return;
        }
        this.name = name;
    }

    @Override
    public List<CardType> getCardType(Game game) {
        if (game != null) {
            // dynamic
            MageObjectAttribute mageObjectAttribute = game.getState().getMageObjectAttribute(getId());
            if (mageObjectAttribute != null) {
                return mageObjectAttribute.getCardType();
            }
        }
        if (faceDown) {
            return faceDownValues.getCardType();
        }

        // static
        return cardType;
    }

    @Override
    public SubTypes getSubtype() {
        return getSubtype(null);
    }

    @Override
    public SubTypes getSubtype(Game game) {
        if (game != null) {
            MageObjectAttribute mageObjectAttribute = game.getState().getMageObjectAttribute(getId());
            if (mageObjectAttribute != null) {
                return mageObjectAttribute.getSubtype();
            }
        }
        if (this.isFaceDown()) {
            return faceDownValues.getSubtype();
        }
        return subtype;
    }

    @Override
    public List<SuperType> getSuperType(Game game) {
        if (game != null) {
            // dynamic
            MageObjectAttribute mageObjectAttribute = game.getState().getMageObjectAttribute(getId());
            if (mageObjectAttribute != null) {
                return mageObjectAttribute.getSuperType();
            }
        }
        if (this.isFaceDown()) {
            return faceDownValues.getSuperType();
        }
        return supertype;
    }

    @Override
    public Abilities<Ability> getAbilities() {
        if (this.isFaceDown()) {
            return faceDownValues.getAbilities();
        }
        return abilities;
    }

    @Override
    public boolean hasAbility(Ability ability, Game game) {
        if (this.getAbilities().contains(ability)) {
            return true;
        }
        Abilities<Ability> otherAbilities = game.getState().getAllOtherAbilities(getId());
        return otherAbilities != null && otherAbilities.contains(ability);
    }

    @Override
    public MageInt getPower() {
        if (faceDown) {
            return faceDownValues.getPower();
        }
        return power;
    }

    @Override
    public MageInt getToughness() {
        if (faceDown) {
            return faceDownValues.getToughness();
        }
        return toughness;
    }

    @Override
    public int getStartingLoyalty() {
        if (faceDown) {
            return faceDownValues.getStartingLoyalty();
        }
        return startingLoyalty;
    }

    @Override
    public void setStartingLoyalty(int startingLoyalty) {
        if (faceDown) {
            faceDownValues.setStartingLoyalty(startingLoyalty);
            return;
        }
        this.startingLoyalty = startingLoyalty;
    }

    @Override
    public int getStartingDefense() {
        if (faceDown) {
            return faceDownValues.getStartingDefense();
        }
        return startingDefense;
    }

    @Override
    public void setStartingDefense(int startingDefense) {
        if (faceDown) {
            faceDownValues.setStartingDefense(startingDefense);
            return;
        }
        this.startingDefense = startingDefense;
    }

    @Override
    public ObjectColor getColor() {
        return getColor(null);
    }

    @Override
    public ObjectColor getColor(Game game) {
        if (game != null) {
            MageObjectAttribute mageObjectAttribute = game.getState().getMageObjectAttribute(getId());
            if (mageObjectAttribute != null) {
                return mageObjectAttribute.getColor();
            }
        }
        if (faceDown) {
            return faceDownValues.getColor();
        }
        return color;
    }

    @Override
    public ObjectColor getFrameColor(Game game) {
        // For lands, add any colors of mana the land can produce to
        // its frame colors while game is active to represent ability changes during the game.
        if (this.isLand(game) && !(this instanceof MockCard)) {
            ObjectColor cl = frameColor.copy();
            Set<ManaType> manaTypes = EnumSet.noneOf(ManaType.class);
            for (Ability ab : getAbilities()) {
                if (ab instanceof ActivatedManaAbilityImpl) {
                    manaTypes.addAll(((ActivatedManaAbilityImpl) ab).getProducableManaTypes(game));
                }
            }
            cl.setWhite(manaTypes.contains(ManaType.WHITE));
            cl.setBlue(manaTypes.contains(ManaType.BLUE));
            cl.setBlack(manaTypes.contains(ManaType.BLACK));
            cl.setRed(manaTypes.contains(ManaType.RED));
            cl.setGreen(manaTypes.contains(ManaType.GREEN));
            return cl;
        } else {
            // For everything else, just return the frame colors
            return frameColor;
        }
    }

    @Override
    public FrameStyle getFrameStyle() {
        return frameStyle;
    }

    @Override
    public String getExpansionSetCode() {
        if (faceDown) {
            return faceDownValues.getExpansionSetCode();
        }
        return expansionSetCode;
    }

    @Override
    public void setExpansionSetCode(String expansionSetCode) {
        if (faceDown) {
            faceDownValues.setExpansionSetCode(expansionSetCode);
            return;
        }
        this.expansionSetCode = expansionSetCode;
    }

    @Override
    public boolean getUsesVariousArt() {
        if (faceDown) {
            return faceDownValues.getUsesVariousArt();
        }
        return usesVariousArt;
    }

    @Override
    public void setUsesVariousArt(boolean usesVariousArt) {
        if (faceDown) {
            faceDownValues.setUsesVariousArt(usesVariousArt);
            return;
        }
        this.usesVariousArt = usesVariousArt;
    }

    @Override
    public String getCardNumber() {
        if (faceDown) {
            return faceDownValues.getCardNumber();
        }
        return cardNumber;
    }

    @Override
    public void setCardNumber(String cardNumber) {
        if (faceDown) {
            faceDownValues.setCardNumber(cardNumber);
            return;
        }
        this.cardNumber = cardNumber;
    }

    @Override
    public String getImageFileName() {
        if (faceDown) {
            return faceDownValues.getImageFileName();
        }
        return imageFileName;
    }

    @Override
    public void setImageFileName(String imageFileName) {
        if (faceDown) {
            faceDownValues.setImageFileName(imageFileName);
            return;
        }
        this.imageFileName = imageFileName;
    }

    @Override
    public Integer getImageNumber() {
        if (faceDown) {
            return faceDownValues.getImageNumber();
        }
        return imageNumber;
    }

    @Override
    public void setImageNumber(Integer imageNumber) {
        if (faceDown) {
            faceDownValues.setImageNumber(imageNumber);
            return;
        }
        this.imageNumber = imageNumber;
    }

    @Override
    public ManaCosts<ManaCost> getManaCost() {
        if (faceDown) {
            return faceDownValues.getManaCost();
        }
        return manaCost;
    }

    @Override
    public void setManaCost(ManaCosts<ManaCost> costs) {
        if (faceDown) {
            faceDownValues.setManaCost(costs);
            return;
        }
        this.manaCost = costs.copy();
    }

    @Override
    public int getManaValue() {
        if (faceDown) {
            return faceDownValues.getManaCost().manaValue();
        }
        if (manaCost != null) {
            return manaCost.manaValue();
        }
        return 0;
    }

    @Override
    public boolean hasSubtype(SubType value, Game game) {
        if (value == null) {
            return false;
        }
        return value.getSubTypeSet() == SubTypeSet.CreatureType && isAllCreatureTypes(game)
                || value.getSubTypeSet() == SubTypeSet.NonBasicLandType && isAllNonbasicLandTypes(game)
                || getSubtype(game).contains(value);
    }

    @Override
    public void setCopy(boolean isCopy, MageObject copyFrom) {
        this.copy = isCopy;
        this.copyFrom = (copyFrom != null ? copyFrom.copy() : null);
    }

    @Override
    public MageObject getCopyFrom() {
        return this.copyFrom;
    }

    @Override
    public boolean isCopy() {
        return copy;
    }

    @Override
    public int getZoneChangeCounter(Game game) {
        return game.getState().getZoneChangeCounter(objectId);
    }

    @Override
    public void updateZoneChangeCounter(Game game, ZoneChangeEvent event) {
        game.getState().updateZoneChangeCounter(objectId);
    }

    @Override
    public void setZoneChangeCounter(int value, Game game) {
        game.getState().setZoneChangeCounter(objectId, value);
    }

    @Override
    public boolean isAllCreatureTypes(Game game) {
        return this.getSubtype(game).isAllCreatureTypes();
    }

    @Override
    public void setIsAllCreatureTypes(boolean value) {
        this.getSubtype().setIsAllCreatureTypes(value && (this.isKindred() || this.isCreature()));
    }

    @Override
    public void setIsAllCreatureTypes(Game game, boolean value) {
        this.getSubtype(game).setIsAllCreatureTypes(value && (this.isKindred(game) || this.isCreature(game)));
    }

    @Override
    public boolean isAllNonbasicLandTypes(Game game) {
        return this.getSubtype(game).isAllNonbasicLandTypes();
    }

    @Override
    public void setIsAllNonbasicLandTypes(boolean value) {
        this.getSubtype().setIsAllNonbasicLandTypes(value && this.isLand());
    }

    @Override
    public void setIsAllNonbasicLandTypes(Game game, boolean value) {
        this.getSubtype(game).setIsAllNonbasicLandTypes(value && this.isLand(game));
    }

    /**
     * Remove power/toughness character defining abilities
     */
    @Override
    public void removePTCDA() {
        for (Iterator<Ability> iter = this.getAbilities().iterator(); iter.hasNext(); ) {
            Ability ability = iter.next();
            for (Effect effect : ability.getEffects()) {
                if (effect instanceof ContinuousEffect && ((ContinuousEffect) effect).getSublayer() == SubLayer.CharacteristicDefining_7a) {
                    iter.remove();
                    break;
                }
            }
        }
    }

    @Override
    public CopiableValues getFaceDownValues() {
        return faceDownValues;
    }

    @Override
    public boolean isFaceDown() {
        return faceDown;
    }

    @Override
    public void setFaceDown(boolean faceDown) {
        this.faceDown = faceDown;
    }

    @Override
    public String toString() {
        return getIdName() + " (" + super.getClass().getSuperclass().getSimpleName() + " -> " + this.getClass().getSimpleName() + ")";
    }
}
