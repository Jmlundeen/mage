package mage.cards;

import mage.MageInt;
import mage.MageObject;
import mage.ObjectColor;
import mage.abilities.Abilities;
import mage.abilities.AbilitiesImpl;
import mage.abilities.Ability;
import mage.abilities.costs.mana.ManaCost;
import mage.abilities.costs.mana.ManaCosts;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.cards.repository.TokenInfo;
import mage.cards.repository.TokenRepository;
import mage.constants.CardType;
import mage.constants.Rarity;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.game.Game;
import mage.game.permanent.PermanentCard;
import mage.game.stack.Spell;
import mage.util.SubTypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CopiableValues implements Serializable {

    // MTG copiable values
    protected String name = "";
    protected ManaCosts<ManaCost> manaCost = new ManaCostsImpl<>();
    protected ObjectColor color = new ObjectColor();
    protected List<CardType> cardType = new ArrayList<>();
    protected List<SuperType> superType = new ArrayList<>();
    protected SubTypes subtype = new SubTypes();
    protected Abilities<Ability> abilities = new AbilitiesImpl<>();
    protected MageInt power = new MageInt(0);
    protected MageInt toughness = new MageInt(0);
    protected int startingLoyalty;
    protected int startingDefense;
    // Xmage copiable values
    protected Rarity rarity = Rarity.SPECIAL;
    protected String expansionSetCode;
    protected boolean usesVariousArt;
    protected String cardNumber;
    protected String imageFileName;
    protected int imageNumber;

    public CopiableValues(boolean faceDown) {
        if (faceDown) {
            TokenInfo tokenInfo = TokenRepository.instance.findPreferredTokenInfoForXmage(
                    TokenRepository.XMAGE_IMAGE_NAME_FACE_DOWN_MANUAL,
                    null
            );
            if (tokenInfo != null) {
                this.expansionSetCode = tokenInfo.getSetCode();
                this.usesVariousArt = false;
                this.cardNumber = "0";
                this.imageFileName = tokenInfo.getName();
                this.imageNumber = tokenInfo.getImageNumber();
            }
        }
    }

    public CopiableValues(MageObject mageObject, Game game) {
        copyFrom(mageObject, game);
    }

    private CopiableValues(final CopiableValues values) {
        this.name = values.name;
        this.manaCost = values.manaCost.copy();
        this.color = values.color.copy();
        this.cardType = new ArrayList<>(values.cardType);
        this.superType = new ArrayList<>(values.superType);
        this.subtype.copyFrom(values.subtype);
        this.abilities = values.abilities.copy();
        this.power = values.power.copy();
        this.toughness = values.toughness.copy();
        this.startingLoyalty = values.startingLoyalty;
        this.startingDefense = values.startingDefense;
        this.rarity = values.rarity;
        this.expansionSetCode = values.expansionSetCode;
        this.usesVariousArt = values.usesVariousArt;
        this.cardNumber = values.cardNumber;
        this.imageFileName = values.imageFileName;
        this.imageNumber = values.imageNumber;
    }

    public void copyFrom(MageObject mageObject, Game game) {
        this.name = mageObject.getName();
        if (mageObject instanceof PermanentCard && mageObject.getManaCost().isEmpty()) {
            this.manaCost = ((PermanentCard) mageObject).getCard().getManaCost().copy();
        } else{
            this.manaCost = mageObject.getManaCost().copy();
        }
        this.color = mageObject.getColor().copy();
        this.cardType.clear();
        this.cardType.addAll(mageObject.getCardType());
        this.superType.clear();
        this.superType.addAll(mageObject.getSuperType());
        this.subtype.copyFrom(mageObject.getSubtype());
        if (mageObject instanceof Card) {
            this.abilities = ((Card) mageObject).getAbilities(game).copy();
        } else {
            this.abilities = mageObject.getAbilities().copy();
        }
        this.power = mageObject.getPower().copy();
        this.toughness = mageObject.getToughness().copy();
        this.startingLoyalty = mageObject.getStartingLoyalty();
        this.startingDefense = mageObject.getStartingDefense();
        if (mageObject instanceof Card) {
            this.rarity = ((Card) mageObject).getRarity();
        }
        this.expansionSetCode = mageObject.getExpansionSetCode();
        this.usesVariousArt = mageObject.getUsesVariousArt();
        this.cardNumber = mageObject.getCardNumber();
        this.imageFileName = mageObject.getImageFileName();
        this.imageNumber = mageObject.getImageNumber();
    }

    /**
     * Applies the copiable values to the given mage object.
     * Does not use state, only use for blueprint/copy
     * @param mageObject
     */
    public void applyTo(MageObject mageObject) {
        mageObject.setName(this.name);
        mageObject.setManaCost(this.manaCost);
        mageObject.getColor().setColor(this.color);
        mageObject.getCardType().clear();
        mageObject.getCardType().addAll(this.cardType);
        mageObject.getSuperType().clear();
        mageObject.getSuperType().addAll(this.superType);
        mageObject.getSubtype().clear();
        mageObject.getSubtype().addAll(this.subtype);
        mageObject.getAbilities().clear();
        mageObject.getAbilities().addAll(this.abilities);
        mageObject.getPower().setModifiedBaseValue(this.getPower().getValue());
        mageObject.getToughness().setModifiedBaseValue(this.getToughness().getValue());
        mageObject.setStartingLoyalty(this.startingLoyalty);
        mageObject.setStartingDefense(this.startingDefense);
        if (!(mageObject instanceof Spell)) {
            mageObject.setExpansionSetCode(this.expansionSetCode);
            mageObject.setUsesVariousArt(this.usesVariousArt);
            mageObject.setCardNumber(this.cardNumber);
            mageObject.setImageFileName(this.imageFileName);
            mageObject.setImageNumber(this.imageNumber);
        }
    }

    public void clear() {
        this.name = "";
        this.manaCost.clear();
        this.color = new ObjectColor();
        this.cardType.clear();
        this.superType.clear();
        this.subtype.clear();
        this.abilities.clear();
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);
        this.startingLoyalty = 0;
        this.startingDefense = 0;
    }

    public CopiableValues copy() {
        return new CopiableValues(this);
    }

    public void add(CopiableValues addedValues) {
        if (!addedValues.getName().isEmpty()) {
            this.name = addedValues.getName();
        }

        this.manaCost.add(addedValues.getManaCost());

        this.color.addColor(addedValues.getColor());

        for (CardType type : addedValues.getCardType()) {
            if (!this.cardType.contains(type)) {
                this.cardType.add(type);
            }
        }
        for (SuperType type : addedValues.getSuperType()) {
            if (!this.superType.contains(type)) {
                this.superType.add(type);
            }
        }
        for (SubType type : addedValues.getSubtype()) {
            if (!this.subtype.contains(type)) {
                this.subtype.add(type);
            }
        }
        for (Ability ability : addedValues.getAbilities()) {
            if (!this.abilities.contains(ability)) {
                this.abilities.add(ability);
            }
        }
        if (addedValues.getPower().getModifiedBaseValue() != this.getPower().getModifiedBaseValue()) {
            this.power = addedValues.getPower().copy();
        }
        if (addedValues.getToughness().getModifiedBaseValue() != this.getToughness().getModifiedBaseValue()) {
            this.toughness = addedValues.getToughness().copy();
        }
        if (addedValues.getStartingLoyalty() != 0) {
            this.startingLoyalty = addedValues.getStartingLoyalty();
        }
        if (addedValues.getStartingDefense() != 0) {
            this.startingDefense = addedValues.getStartingDefense();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ManaCosts<ManaCost> getManaCost() {
        return manaCost;
    }

    public void setManaCost(ManaCosts<ManaCost> manaCost) {
        this.manaCost = manaCost;
    }

    public ObjectColor getColor() {
        return color;
    }

    public void setColor(ObjectColor color) {
        this.color = color;
    }

    public List<CardType> getCardType() {
        return cardType;
    }

    public void setCardType(List<CardType> cardType) {
        this.cardType = cardType;
    }

    public List<SuperType> getSuperType() {
        return superType;
    }

    public void setSuperType(List<SuperType> superType) {
        this.superType = superType;
    }

    public SubTypes getSubtype() {
        return subtype;
    }

    public void setSubtype(SubTypes subtype) {
        this.subtype = subtype;
    }

    public Abilities<Ability> getAbilities() {
        return abilities;
    }

    public void setAbilities(Abilities<Ability> abilities) {
        this.abilities = abilities;
    }

    public MageInt getPower() {
        return power;
    }

    public void setPower(MageInt power) {
        this.power = power;
    }

    public MageInt getToughness() {
        return toughness;
    }

    public void setToughness(MageInt toughness) {
        this.toughness = toughness;
    }

    public int getStartingLoyalty() {
        return startingLoyalty;
    }

    public void setStartingLoyalty(int startingLoyalty) {
        this.startingLoyalty = startingLoyalty;
    }

    public int getStartingDefense() {
        return startingDefense;
    }

    public void setStartingDefense(int startingDefense) {
        this.startingDefense = startingDefense;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public void setRarity(Rarity rarity) {
        this.rarity = rarity;
    }

    public String getExpansionSetCode() {
        return expansionSetCode;
    }

    public void setExpansionSetCode(String expansionSetCode) {
        this.expansionSetCode = expansionSetCode;
    }

    public boolean getUsesVariousArt() {
        return usesVariousArt;
    }

    public void setUsesVariousArt(boolean usesVariousArt) {
        this.usesVariousArt = usesVariousArt;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public int getImageNumber() {
        return imageNumber;
    }

    public void setImageNumber(int imageNumber) {
        this.imageNumber = imageNumber;
    }

    @Override
    public String toString() {
        return "CopiableValues{" +
                "name='" + name + '\'' +
                ", manaCost=" + manaCost +
                ", color=" + color +
                ", cardTypes=" + cardType +
                ", superTypes=" + superType +
                ", subTypes=" + subtype +
                ", abilities=" + abilities +
                ", power=" + power +
                ", toughness=" + toughness +
                ", startingLoyalty=" + startingLoyalty +
                ", startingDefense=" + startingDefense +
                ", rarity=" + rarity +
                ", expansionSetCode='" + expansionSetCode + '\'' +
                ", usesVariousArt=" + usesVariousArt +
                ", cardNumber='" + cardNumber + '\'' +
                ", imageFileName='" + imageFileName + '\'' +
                ", imageNumber=" + imageNumber +
                '}';
    }
}
