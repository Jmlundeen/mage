package mage.cards;

import mage.MageInt;
import mage.ObjectColor;
import mage.abilities.Abilities;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCost;
import mage.abilities.costs.mana.ManaCosts;
import mage.constants.*;
import mage.counters.Counter;
import mage.counters.Counters;
import mage.game.Game;
import mage.game.GameState;
import mage.util.CardUtil;
import mage.util.SubTypes;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class FlipCard extends CardWithPartsImpl<FlipCardHalfImpl, FlipCard> {

    // this state value controls if a permanent enters the battlefield already flipped
    public static final String VALUE_KEY_ENTER_FLIPPED = "EnterFlipped";

    public FlipCard (
            UUID ownerId, CardSetInfo setInfo,
            CardType[] typesLeft, SubType[] subTypesLeft, String costsLeft,
            String secondSideName,
            CardType[] typesRight, SubType[] subTypesRight
    ) {
        this(
                ownerId, setInfo,
                new SuperType[]{}, typesLeft, subTypesLeft, costsLeft,
                secondSideName,
                new SuperType[]{}, typesRight, subTypesRight
        );
    }

    public FlipCard(UUID ownerId, CardSetInfo setInfo,
                    CardType[] typesLeft, SubType[] subTypesLeft, String costsLeft,
                    String secondSideName,
                    SuperType[] superTypesRight, CardType[] typesRight, SubType[] subTypesRight) {
        this(
                ownerId, setInfo,
                new SuperType[]{}, typesLeft, subTypesLeft, costsLeft,
                secondSideName,
                superTypesRight, typesRight, subTypesRight);
    }

    public FlipCard(
            UUID ownerId, CardSetInfo setInfo,
            SuperType[] superTypesLeft, CardType[] typesLeft, SubType[] subTypesLeft, String costsLeft,
            String secondSideName,
            SuperType[] superTypesRight, CardType[] typesRight, SubType[] subTypesRight
    ) {
        super(ownerId, setInfo, typesLeft, costsLeft, SpellAbilityType.FLIP);

        leftHalfCard = new FlipCardHalfImpl(
                ownerId, setInfo,
                superTypesLeft, typesLeft, subTypesLeft, costsLeft, this,
                SpellAbilityType.FLIP_LEFT
        );

        rightHalfCard = new FlipCardHalfImpl(
                ownerId, new CardSetInfo(secondSideName, setInfo),
                superTypesRight, typesRight, subTypesRight, "", this,
                SpellAbilityType.FLIP_RIGHT
        );
        rightHalfCard.getColor().setColor(leftHalfCard.getColor());
    }

    protected FlipCard(final FlipCard card) {
        super(card);
    }

    @Override
    public Card getDefaultCardSide() {
        return getLeftHalfCard();
    }

    @Override
    protected void updatePartZones(Zone zone, Game game) {
        if (Objects.requireNonNull(zone) == Zone.BATTLEFIELD) {
            throw new IllegalArgumentException("Wrong code usage: attempting to put main card directly to battlefield - " + this);
        } else {
            game.setZone(leftHalfCard.getId(), zone);
            game.setZone(rightHalfCard.getId(), zone);
        }
        checkGoodZones(game);
    }

    @Override
    public void checkGoodZones(Game game) {
        Card leftPart = this.getLeftHalfCard();
        Card rightPart = this.getRightHalfCard();

        Zone zoneMain = game.getState().getZone(this.getId());
        Zone zoneLeft = game.getState().getZone(leftPart.getId());
        Zone zoneRight = game.getState().getZone(rightPart.getId());

        Zone needZoneLeft;
        Zone needZoneRight;
        switch (zoneMain) {
            case BATTLEFIELD:
            case STACK:
                if (zoneMain == zoneLeft) {
                    needZoneLeft = zoneMain;
                } else {
                    // impossible
                    needZoneLeft = Zone.OUTSIDE;
                }
                needZoneRight = Zone.OUTSIDE;
                break;
            default:
                needZoneLeft = zoneMain;
                needZoneRight = zoneMain;
                break;
        }

        if (zoneLeft != needZoneLeft || zoneRight != needZoneRight) {
            throw new IllegalStateException("Wrong code usage: Flip card uses wrong zones - " + this
                    + "\r\n" + String.format("* main zone: %s", zoneMain)
                    + "\r\n" + String.format("* left side: need %s, actual %s", needZoneLeft, zoneLeft)
                    + "\r\n" + String.format("* right side: need %s, actual %s", needZoneRight, zoneRight));
        }
    }

    @Override
    public Abilities<Ability> getAbilities() {
        return getInnerAbilities(true, false);
    }

    @Override
    public Counters getCounters(Game game) {
        return getCounters(game.getState());
    }

    @Override
    public Counters getCounters(GameState state) {
        return state.getCardState(getLeftHalfCard().getId()).getCounters();
    }

    @Override
    public boolean addCounters(Counter counter, UUID playerAddingCounters, Ability source, Game game, List<UUID> appliedEffects, boolean isEffect, int maxCounters) {
        return getLeftHalfCard().addCounters(counter, playerAddingCounters, source, game, appliedEffects, isEffect, maxCounters);
    }

    @Override
    public void removeCounters(String counterName, int amount, Ability source, Game game) {
        getLeftHalfCard().removeCounters(counterName, amount, source, game);
    }

    @Override
    public boolean cast(Game game, Zone fromZone, SpellAbility ability, UUID controllerId) {
        return this.leftHalfCard.cast(game, fromZone, ability, controllerId);
    }

    @Override
    public List<SuperType> getSuperType(Game game) {
        // rules: While a double-faced card isn't on the stack or battlefield, consider only the characteristics of its front face.
        return getLeftHalfCard().getSuperType(game);
    }

    @Override
    public List<CardType> getCardType(Game game) {
        // rules: While a double-faced card isn't on the stack or battlefield, consider only the characteristics of its front face.
        return getLeftHalfCard().getCardType(game);
    }

    @Override
    public SubTypes getSubtype(Game game) {
        // rules: While a double-faced card isn't on the stack or battlefield, consider only the characteristics of its front face.
        return getLeftHalfCard().getSubtype(game);
    }

    @Override
    public boolean hasSubtype(SubType subtype, Game game) {
        return getLeftHalfCard().hasSubtype(subtype, game);
    }

    @Override
    public List<String> getRules() {
        // rules must show only main side (another side visible by toggle/transform button in GUI)
        // card hints from both sides
        return CardUtil.getCardRulesWithAdditionalInfo(
                this,
                this.getInnerAbilities(true, false),
                this.getInnerAbilities(true, true)
        );
    }

    @Override
    public List<String> getRules(Game game) {
        // rules must show only main side (another side visible by toggle/transform button in GUI)
        // card hints from both sides
        return CardUtil.getCardRulesWithAdditionalInfo(
                game,
                this,
                this.getInnerAbilities(game, true, false),
                this.getInnerAbilities(game, true, true)
        );
    }

    @Override
    public boolean hasAbility(Ability ability, Game game) {
        return super.hasAbility(ability, game);
    }

    @Override
    public ObjectColor getColor() {
        return getLeftHalfCard().getColor();
    }

    @Override
    public ObjectColor getColor(Game game) {
        return getLeftHalfCard().getColor(game);
    }

    @Override
    public ObjectColor getFrameColor(Game game) {
        return getLeftHalfCard().getFrameColor(game);
    }

    @Override
    public ManaCosts<ManaCost> getManaCost() {
        return getLeftHalfCard().getManaCost();
    }

    @Override
    public int getManaValue() {
        // Rules:
        // In every zone other than the battlefield, and also on the battlefield before the permanent flips,
        // a flip card has only the normal characteristics of the card.
        return getLeftHalfCard().getManaValue();
    }

    @Override
    public MageInt getPower() {
        return getLeftHalfCard().getPower();
    }

    @Override
    public MageInt getToughness() {
        return getLeftHalfCard().getToughness();
    }

    @Override
    public UUID getIdForBattlefield(Game game, Ability source) {
        return getDefaultCardSide().getId();
    }

}
