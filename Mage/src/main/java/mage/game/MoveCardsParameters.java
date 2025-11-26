package mage.game;

import mage.abilities.effects.common.continuous.BecomesFaceDownCreatureEffect;
import mage.cards.Card;
import mage.cards.CopiableValues;
import mage.constants.Zone;

import java.util.*;

/**
 * Represents the parameters for moving cards in the game.
 * This class encapsulates the details of the cards being moved,
 * the destination zone, and additional properties such as whether
 * the cards are tapped, face-down, or moved by their owner.
 * Author: Jmlundeen
 */
public class MoveCardsParameters {
    // The set of cards to be moved
    Set<Card> cards = null;

    // The destination zone where the cards will be moved
    Zone toZone;

    // Indicates whether the cards should be tapped after being moved
    boolean tapped = false;

    // Indicates whether the cards should be face-down after being moved
    boolean faceDown = false;

    // Indicates whether the cards are being moved by their owner
    boolean byOwner = false;

    // The ID associated with the exile zone, if applicable
    UUID exileId = null;

    // The name associated with the exile zone, if applicable
    String exileName = "";

    // Indicates whether the moving player can look at the card in exile face down
    boolean canLookFaceDownInExile = false;

    // Indicates whether the cards should be placed at the top of the library
    boolean toTopOfLibrary = true;

    // Values to set for face-down cards
    CopiableValues faceDownValues;

    // Type of effect setting the face-down status
    BecomesFaceDownCreatureEffect.FaceDownType faceDownType;

    /**
     * Constructs a new MoveCardsParameters object with the specified cards and destination zone.
     * By default, the cards are not tapped, not face-down, and not moved by their owner.
     *
     * @param cards  The set of cards to be moved.
     * @param toZone The destination zone where the cards will be moved.
     */
    public MoveCardsParameters(List<Card> cards, Zone toZone) {
        this.cards = new LinkedHashSet<>(cards);
        this.toZone = toZone;
    }

    public MoveCardsParameters(Collection<? extends Card> cards, Zone toZone) {
        this.cards = new LinkedHashSet<>(cards);
        this.toZone = toZone;
    }

    /**
     * Constructs a new MoveCardsParameters object with the specified cards and destination zone.
     * By default, the cards are not tapped, not face-down, and not moved by their owner.
     *
     * @param cards  The set of cards to be moved.
     * @param toZone The destination zone where the cards will be moved.
     */
    public MoveCardsParameters(Set<Card> cards, Zone toZone) {
        this.cards = cards;
        this.toZone = toZone;
    }

    public MoveCardsParameters(Card card, Zone toZone) {
        this.cards = new HashSet<>();
        this.cards.add(card);
        this.toZone = toZone;
    }

    public MoveCardsParameters(Zone toZone) {
        this.cards = new HashSet<>();
        this.toZone = toZone;
    }

    /**
     * Gets the list of cards to be moved.
     *
     * @return The list of cards.
     */
    public Set<Card> getCards() {
        return cards;
    }

    /**
     * Sets the list of cards to be moved.
     *
     * @param cards The list of cards.
     * @return The updated MoveCardsParameters object.
     */
    public MoveCardsParameters setCards(Set<Card> cards) {
        this.cards = cards;
        return this;
    }

    /**
     * Sets a single card to be moved.
     * @param card
     */
    public void setCards(Card card) {
        this.cards.clear();
        this.cards.add(card);
    }

    /**
     * Gets the destination zone where the cards will be moved.
     *
     * @return The destination zone.
     */
    public Zone getToZone() {
        return toZone;
    }

    /**
     * Sets the destination zone where the cards will be moved.
     *
     * @param toZone The destination zone.
     * @return The updated MoveCardsParameters object.
     */
    public MoveCardsParameters setToZone(Zone toZone) {
        this.toZone = toZone;
        return this;
    }

    /**
     * Checks if the cards should be tapped after being moved.
     *
     * @return True if the cards should be tapped, false otherwise.
     */
    public boolean isTapped() {
        return tapped;
    }

    /**
     * Sets whether the cards should be tapped after being moved.
     *
     * @param tapped True if the cards should be tapped, false otherwise.
     * @return The updated MoveCardsParameters object.
     */
    public MoveCardsParameters setTapped(boolean tapped) {
        this.tapped = tapped;
        return this;
    }

    /**
     * Checks if the cards should be face-down after being moved.
     *
     * @return True if the cards should be face-down, false otherwise.
     */
    public boolean isFaceDown() {
        return faceDown;
    }

    /**
     * Sets whether the cards should be face-down after being moved.
     *
     * @param faceDown True if the cards should be face-down, false otherwise.
     * @return The updated MoveCardsParameters object.
     */
    public MoveCardsParameters setFaceDown(boolean faceDown) {
        this.faceDown = faceDown;
        return this;
    }

    /**
     * Checks if the cards are being moved by their owner.
     *
     * @return True if the cards are moved by their owner, false otherwise.
     */
    public boolean isByOwner() {
        return byOwner;
    }

    /**
     * Sets whether the cards are being moved by their owner.
     *
     * @param byOwner True if the cards are moved by their owner, false otherwise.
     * @return The updated MoveCardsParameters object.
     */
    public MoveCardsParameters setByOwner(boolean byOwner) {
        this.byOwner = byOwner;
        return this;
    }

    /**
     * Gets the ID associated with the exile zone, if applicable.
     *
     * @return The exile ID.
     */
    public UUID getExileId() {
        return exileId;
    }

    /**
     * Sets the ID associated with the exile zone, if applicable.
     *
     * @param exileId The exile ID.
     * @return The updated MoveCardsParameters object.
     */
    public MoveCardsParameters setExileId(UUID exileId) {
        this.exileId = exileId;
        return this;
    }

    /**
     * Gets the name associated with the exile zone, if applicable.
     *
     * @return The exile name.
     */
    public String getExileName() {
        return exileName;
    }

    /**
     * Sets the name associated with the exile zone, if applicable.
     *
     * @param exileName The exile name.
     * @return The updated MoveCardsParameters object.
     */
    public MoveCardsParameters setExileName(String exileName) {
        this.exileName = exileName;
        return this;
    }

    /**
     * Checks if the moving player can look at the card in exile face down.
     *
     * @return True if the player can look at the card, false otherwise.
     */
    public boolean canLookFaceDownInExile() {
        return canLookFaceDownInExile;
    }

    /**
     * Sets whether the moving player can look at the card in exile face down.
     *
     * @param canLookFaceDownInExile True if the player can look at the card, false otherwise.
     * @return The updated MoveCardsParameters object.
     */
    public MoveCardsParameters setCanLookFaceDownInExile(boolean canLookFaceDownInExile) {
        this.canLookFaceDownInExile = canLookFaceDownInExile;
        return this;
    }

    /**
     * Checks if the cards should be placed at the top of the library.
     *
     * @return True if the cards should be placed at the top of the library, false otherwise.
     */
    public boolean isToTopOfLibrary() {
        return toTopOfLibrary;
    }

    /**
     * Sets whether the cards should be placed at the top of the library.
     *
     * @param toTopOfLibrary True if the cards should be placed at the top of the library, false otherwise.
     * @return The updated MoveCardsParameters object.
     */
    public MoveCardsParameters setToTopOfLibrary(boolean toTopOfLibrary) {
        this.toTopOfLibrary = toTopOfLibrary;
        return this;
    }

    public CopiableValues getFaceDownValues() {
        return faceDownValues;
    }

    public MoveCardsParameters setFaceDownValues(CopiableValues faceDownValues) {
        this.faceDownValues = faceDownValues;
        return this;
    }

    public BecomesFaceDownCreatureEffect.FaceDownType getFaceDownType() {
        return faceDownType;
    }

    public MoveCardsParameters setFaceDownType(BecomesFaceDownCreatureEffect.FaceDownType faceDownType) {
        this.faceDownType = faceDownType;
        return this;
    }
}