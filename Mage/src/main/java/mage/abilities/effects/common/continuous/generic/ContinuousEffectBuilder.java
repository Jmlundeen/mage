package mage.abilities.effects.common.continuous.generic;

import mage.MageItem;
import mage.MageObject;
import mage.MageObjectReference;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.ChooseCreatureTypeEffect;
import mage.abilities.mana.*;
import mage.cards.Card;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.FilterPermanent;
import mage.filter.FilterStackObject;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.command.Commander;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Used for consolidating some continuous effect logic. Layers are handled in mutator methods when adding modifications.
 * Other than effects that only use the battlefield, affected zones, filters, and targetController should be set based on
 * what/whose objects are affected. {@link ContinuousAffected} can be used as a shortcut for some logic such as the source object,
 * targeted objects, and attached to.
 *
 * @author Jmlundeen
 */
public class ContinuousEffectBuilder extends ContinuousEffectImpl {

    protected FilterStackObject stackObjectFilter;
    protected FilterPermanent permanentFilter;
    protected FilterCard cardFilter;
    protected TargetController cardsControlledBy = TargetController.YOU;
    protected ContinuousAffected affected = ContinuousAffected.STATIC_OR_DYNAMIC;
    protected List<MageObjectReference> staticAffectedObjects;
    protected List<Zone> affectedZones;
    protected List<Ability> gainedAbilities;
    protected List<Layer> additionalLayers;
    protected List<SubLayer> additionalSublayers;
    protected DynamicValue powerModifier;
    protected DynamicValue toughnessModifier;
    protected DynamicValue basePower;
    protected DynamicValue baseToughness;
    protected List<CardType> addedCardTypes;
    protected List<CardType> removedCardTypes;
    protected List<SuperType> addedSuperTypes;
    protected List<SuperType> removedSuperTypes;
    protected List<SubType> addedSubTypes;
    protected List<SubType> removedSubTypes;
    protected List<SubTypeSet> removedSubTypeSets;
    protected ObjectColor addedColor;
    protected boolean removeOtherAbilities;
    protected boolean removeOtherCardTypes = true;
    protected boolean removeOtherSubtypes = true;
    protected boolean useChosenCreatureType;
    protected boolean useEveryCreatureType;
    protected boolean useEveryLandType;
    protected boolean removeOtherColors;
    protected MakeAbilityFunction makeAbilityFunction;
    protected Map<UUID, Ability> createdAbilities; // cache created abilities to reduce ability creation
    protected int cachedAbilityZcc; // reset created abilities cache if source ZCC changes
    protected List<Class<? extends Ability>> abilitiesToRemove;

    /**
     * Creates a new standard ContinuousEffectBuilder. For abilities that affect cards of the controller
     * Zones need to be set separately using {@link #setAffectedZones(Zone...)} if not using targets
     */
    public ContinuousEffectBuilder(Duration duration, Outcome outcome) {
        super(duration, outcome);
    }

    /**
     * Creates a new ContinuousEffectBuilder. Use this for effects that work on the source object or permanent source is attached to.
     * Zones need to be set separately using {@link #setAffectedZones(Zone...)} if not using targets
     */
    public ContinuousEffectBuilder(Duration duration, Outcome outcome, ContinuousAffected affected) {
        super(duration, outcome);
        this.affected = affected;
    }

    /**
     * Creates a new ContinuousEffectBuilder. Use this for effects that work on the source object or permanent source is attached to.
     * Zones need to be set separately using {@link #setAffectedZones(Zone...)} if not using targets
     */
    public ContinuousEffectBuilder(Outcome outcome, ContinuousAffected affected) {
        this(Duration.WhileOnBattlefield, outcome, affected);
    }

    /**
     * Creates a new ContinuousEffectBuilder that applies to objects controlled by the specified controller.
     * Make sure to set the affected zones using {@link #setAffectedZones(Zone...)}
     */
    public ContinuousEffectBuilder(Duration duration, Outcome outcome, TargetController objectController) {
        super(duration, outcome);
        this.cardsControlledBy = objectController;
    }

    /**
     * Creates a new ContinuousEffectBuilder that applies to objects controlled by the specified controller on the battlefield.
     * @param objectController the controller whose objects are affected. Use {@link TargetController#YOU}, {@link TargetController#OPPONENT}, {@link TargetController#EACH_PLAYER}
     */
    public ContinuousEffectBuilder(Outcome outcome, TargetController objectController, FilterPermanent permanentFilter) {
        super(Duration.WhileOnBattlefield, outcome);
        this.permanentFilter = permanentFilter;
        this.cardsControlledBy = objectController;
        this.affectedZones = Collections.singletonList(Zone.BATTLEFIELD);
    }

    /**
     * Creates a new ContinuousEffectBuilder that applies to permanents on the battlefield the controlling player controls
     */
    public ContinuousEffectBuilder(Outcome outcome, FilterPermanent permanentFilter) {
        super(Duration.WhileOnBattlefield, outcome);
        this.permanentFilter = permanentFilter;
        this.cardsControlledBy = TargetController.YOU;
        this.affectedZones = Collections.singletonList(Zone.BATTLEFIELD);
    }

    /**
     * Creates a new ContinuousEffectBuilder that applies to permanents on the battlefield
     * @param cardsControlledBy the controller whose objects are affected. Use {@link TargetController#YOU}, {@link TargetController#OPPONENT}, {@link TargetController#EACH_PLAYER}
     */
    public ContinuousEffectBuilder(Duration duration, Outcome outcome, TargetController cardsControlledBy, FilterPermanent permanentFilter) {
        super(duration, outcome);
        this.permanentFilter = permanentFilter;
        this.cardsControlledBy = cardsControlledBy;
        this.affectedZones = Collections.singletonList(Zone.BATTLEFIELD);
    }

    /**
     * Creates a new ContinuousEffectBuilder that applies to Cards in the specified zones
     */
    public ContinuousEffectBuilder(Duration duration, Outcome outcome, TargetController cardsControlledBy,
                                   FilterCard cardFilter, Zone... affectedZones) {
        super(duration, outcome);
        this.cardFilter = cardFilter;
        this.cardsControlledBy = cardsControlledBy;
        this.affectedZones = new ArrayList<>();
        Collections.addAll(this.affectedZones, affectedZones);
    }

    protected ContinuousEffectBuilder(final ContinuousEffectBuilder effect) {
        super(effect);
        this.stackObjectFilter = effect.stackObjectFilter;
        this.permanentFilter = effect.permanentFilter;
        this.cardFilter = effect.cardFilter;
        this.cardsControlledBy = effect.cardsControlledBy;
        this.staticAffectedObjects = effect.staticAffectedObjects;
        this.affected = effect.affected;
        this.affectedZones = effect.affectedZones;
        this.gainedAbilities = effect.gainedAbilities;
        this.additionalLayers = effect.additionalLayers;
        this.additionalSublayers = effect.additionalSublayers;
        this.powerModifier = effect.powerModifier;
        this.toughnessModifier = effect.toughnessModifier;
        this.basePower = effect.basePower;
        this.baseToughness = effect.baseToughness;
        this.addedCardTypes = effect.addedCardTypes;
        this.removedCardTypes = effect.removedCardTypes;
        this.addedSuperTypes = effect.addedSuperTypes;
        this.removedSuperTypes = effect.removedSuperTypes;
        this.addedSubTypes = effect.addedSubTypes;
        this.removedSubTypes = effect.removedSubTypes;
        this.removedSubTypeSets = effect.removedSubTypeSets;
        this.addedColor = effect.addedColor;
        this.removeOtherAbilities = effect.removeOtherAbilities;
        this.removeOtherCardTypes = effect.removeOtherCardTypes;
        this.removeOtherSubtypes = effect.removeOtherSubtypes;
        this.useChosenCreatureType = effect.useChosenCreatureType;
        this.useEveryCreatureType = effect.useEveryCreatureType;
        this.useEveryLandType = effect.useEveryLandType;
        this.removeOtherColors = effect.removeOtherColors;
        this.makeAbilityFunction = effect.makeAbilityFunction;
        this.createdAbilities = effect.createdAbilities;
        this.abilitiesToRemove = effect.abilitiesToRemove;
        this.cachedAbilityZcc = effect.cachedAbilityZcc;
    }

    @Override
    public ContinuousEffectBuilder copy() {
        return new ContinuousEffectBuilder(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        if (getAffectedObjectsSet() && affected == ContinuousAffected.STATIC_OR_DYNAMIC && getTargetPointer().getTargets(game, source).isEmpty()) {
            // for static affected objects, set affected objects only once at init

            List<MageItem> affectedObjects = new ArrayList<>();
            queryAffectedObjects(layer, source, game, affectedObjects);
            if (staticAffectedObjects == null && !affectedObjects.isEmpty()) {
                staticAffectedObjects = new ArrayList<>();
            }
            for (MageItem object : affectedObjects) {
                staticAffectedObjects.add(new MageObjectReference((MageObject) object, game));
            }
        }
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        if (layer == Layer.AbilityAddingRemovingEffects_6 && makeAbilityFunction != null) {
            cleanupStaleCreatedAbilities(game, source, affectedObjects);
        }

        for (MageItem object : affectedObjects) {
            MageObject mageObject = (MageObject) object;
            switch (layer) {
                case ControlChangingEffects_2:
                    ((Permanent) mageObject).changeControllerId(source.getControllerId(), game, source);
                    break;
                case TextChangingEffects_3:
                    // TODO: implement
                    break;
                case TypeChangingEffects_4:
                    handleCardTypes(game, mageObject);
                    handleSuperTypes(game, mageObject);
                    handleSubTypes(source, game, mageObject);
                    break;
                case ColorChangingEffects_5:
                    if (removeOtherColors) {
                        mageObject.getColor(game).setColor(addedColor);
                    } else {
                        mageObject.getColor(game).addColor(addedColor);
                    }
                    break;
                case AbilityAddingRemovingEffects_6:
                    removeAbilities(source, game, mageObject);
                    addAbilities(source, game, mageObject);
                    break;
                case PTChangingEffects_7:
                    if (sublayer == SubLayer.CharacteristicDefining_7a || sublayer == SubLayer.SetPT_7b) {
                        setBasePowerAndToughness(source, game, mageObject);
                        break;
                    }
                    if (sublayer == SubLayer.ModifyPT_7c) {
                        addPowerAndToughness(source, game, mageObject);
                        break;
                    }
                    if (sublayer == SubLayer.SwitchPT_e) {
                        ((Permanent) mageObject).switchPowerToughness();
                        break;
                    }
            }
        }
    }

    /**
     * Clean up stale entries from the createdAbilities map by removing abilities
     * for objects that are no longer affected by this continuous effect.
     */
    private void cleanupStaleCreatedAbilities(Game game, Ability source, List<MageItem> currentAffectedObjects) {
        if (createdAbilities == null || createdAbilities.isEmpty() || cachedAbilityZcc == game.getState().getZoneChangeCounter(source.getSourceId())) {
            return;
        }

        Set<UUID> currentAffectedIds = new HashSet<>();
        for (MageItem item : currentAffectedObjects) {
            if (item instanceof MageObject) {
                currentAffectedIds.add(item.getId());
            }
        }

        // Remove abilities for objects that are no longer affected
        Iterator<Map.Entry<UUID, Ability>> iterator = createdAbilities.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Ability> entry = iterator.next();
            UUID objectId = entry.getKey();

            if (!currentAffectedIds.contains(objectId)) {
                iterator.remove();
            }
        }
    }

    private void addPowerAndToughness(Ability source, Game game, MageObject mageObject) {
        if (powerModifier == null || toughnessModifier == null) {
            throw new IllegalArgumentException("Power and/or toughness modifier not set in ContinuousEffectBuilder");
        }
        if (mageObject instanceof Permanent) {
            Permanent permanent = (Permanent) mageObject;
            permanent.addPower(powerModifier.calculate(game, source, this, permanent));
            permanent.addToughness(toughnessModifier.calculate(game, source, this, permanent));
        }
    }

    private void setBasePowerAndToughness(Ability source, Game game, MageObject mageObject) {
        if (basePower == null && baseToughness == null) {
            throw new IllegalArgumentException("Power and toughness not set in ContinuousEffectBuilder");
        }
        if (basePower != null) {
            mageObject.getPower().setModifiedBaseValue(basePower.calculate(game, source, this, mageObject));
        }
        if (baseToughness != null) {
            mageObject.getToughness().setModifiedBaseValue(baseToughness.calculate(game, source, this, mageObject));
        }
    }

    private void addAbilities(Ability source, Game game, MageObject mageObject) {
        if (gainedAbilities != null) {
            for (Ability abilityToAdd : gainedAbilities) {
                addAbilityToObject(source, game, mageObject, abilityToAdd);
            }
        }
        if (makeAbilityFunction != null && mageObject instanceof Card) {
            if (cachedAbilityZcc != game.getState().getZoneChangeCounter(source.getSourceId())) {
                if (createdAbilities != null) {
                    createdAbilities.clear();
                } else {
                    createdAbilities = new HashMap<>();
                }
                cachedAbilityZcc = game.getState().getZoneChangeCounter(source.getSourceId());
            }

            Ability abilityToAdd = createdAbilities != null && createdAbilities.containsKey(mageObject.getId())
                    ? createdAbilities.get(mageObject.getId())
                    : makeAbilityFunction.makeAbility((Card) mageObject, source, game);

            if (abilityToAdd != null) {
                createdAbilities.put(mageObject.getId(), abilityToAdd);
                addAbilityToObject(source, game, mageObject, abilityToAdd);
            }
        }
    }

    protected void addAbilityToObject(Ability source, Game game, MageObject mageObject, Ability abilityToAdd) {
        if (mageObject instanceof Permanent) {
            ((Permanent) mageObject).addAbility(abilityToAdd, source.getSourceId(), game);
        } else if (mageObject instanceof Card) {
            game.getState().addOtherAbility((Card) mageObject, abilityToAdd);
        }
    }

    private void removeAbilities(Ability source, Game game, MageObject mageObject) {
        if (removeOtherAbilities) {
            if (mageObject instanceof Permanent) {
                ((Permanent) mageObject).removeAllAbilities(source.getSourceId(), game);
            } else if (mageObject instanceof Card) {
                game.getState().getCardState(mageObject.getId()).getAbilities().clear();
            }
        }
        if (abilitiesToRemove != null) {
            List<Ability> toRemove = new ArrayList<>();
            for (Class<? extends Ability> abilityClass : abilitiesToRemove) {
                mageObject.getAbilities().stream()
                        .filter(ability -> abilityClass.isAssignableFrom(ability.getClass()))
                        .forEach(toRemove::add);
            }
            for (Ability ability : toRemove) {
                if (mageObject instanceof Permanent) {
                    ((Permanent) mageObject).removeAbility(ability, source.getSourceId(), game);
                } else if (mageObject instanceof Card) {
                    game.getState().getCardState(mageObject.getId()).getAbilities().remove(ability);
                }
            }
        }
    }

    private void handleSubTypes(Ability source, Game game, MageObject mageObject) {
        if (removedSubTypes != null) {
            for (SubType subType : removedSubTypes) {
                if (subType.getSubTypeSet() == SubTypeSet.CreatureType) {
                    mageObject.setIsAllCreatureTypes(game, false);
                }
                if (subType.getSubTypeSet() == SubTypeSet.NonBasicLandType) {
                    mageObject.setIsAllNonbasicLandTypes(game, false);
                }
                mageObject.removeSubType(game, subType);
            }
        }
        if (useEveryCreatureType) {
            mageObject.setIsAllCreatureTypes(game, true);
        }
        if (useEveryLandType) {
            mageObject.setIsAllNonbasicLandTypes(game, true);
        }
        if (useChosenCreatureType) {
            SubType subType = ChooseCreatureTypeEffect.getChosenCreatureType(source.getSourceId(), game);
            if (removeOtherSubtypes) {
                mageObject.removeAllSubTypes(game, SubTypeSet.CreatureType);
            }
            if (subType != null) {
                mageObject.addSubType(game, subType);
            }
        }
        if (addedSubTypes != null) {
            List<SubType> addedBasicLandTypes = new ArrayList<>();
            Set<SubTypeSet> setsToRemove = new HashSet<>();
            for (SubType subType : addedSubTypes) {
                setsToRemove.add(subType.getSubTypeSet());
            }
            // 205.1a. ...Similarly, when an effect sets one or more of an object's subtypes,
            // the new subtype(s) replaces any existing subtypes from the appropriate set
            // (creature types, land types, artifact types, enchantment types, planeswalker types, or spell types).
            for (SubTypeSet set : setsToRemove) {
                if (removeOtherSubtypes || set == SubTypeSet.CreatureType) {
                    mageObject.removeAllSubTypes(game, set);
                }
            }

            for (SubType subType : addedSubTypes) {
                mageObject.addSubType(game, subType);
                if (subType.getSubTypeSet() == SubTypeSet.BasicLandType) {
                    addedBasicLandTypes.add(subType);
                }
            }
            if (!addedBasicLandTypes.isEmpty()) {
                checkManaAbilities(source, game, mageObject, addedBasicLandTypes);
            }
        }
    }

    private static void checkManaAbilities(Ability source, Game game, MageObject mageObject, List<SubType> addedBasicLandTypes) {
        if (!(mageObject instanceof Permanent)) {
            return;
        }

        Permanent permanent = (Permanent) mageObject;

        // check if types being added + existing types include all basic land types
        Set<SubType> allBasicLandTypes = EnumSet.noneOf(SubType.class);
        for (SubType subType : addedBasicLandTypes) {
            if (subType.getSubTypeSet() == SubTypeSet.BasicLandType) {
                allBasicLandTypes.add(subType);
            }
        }
        for (SubType subType : permanent.getSubtype(game)) {
            if (subType.getSubTypeSet() == SubTypeSet.BasicLandType) {
                allBasicLandTypes.add(subType);
            }
        }
        if (allBasicLandTypes.size() == 5) {
            // all basic land types are present, remove basic mana abilities and add AnyColorManaAbility
            for (Ability basicManaAbility : Arrays.asList(
                    new WhiteManaAbility(),
                    new BlueManaAbility(),
                    new BlackManaAbility(),
                    new RedManaAbility(),
                    new GreenManaAbility())) {
                if (permanent.getAbilities(game).containsRule(basicManaAbility)) {
                    permanent.removeAbility(basicManaAbility, source.getSourceId(), game);
                }
            }
            permanent.addAbility(new AnyColorManaAbility(), source.getSourceId(), game);
        } else {
            // not all basic land types are present, add basic mana abilities for added basic land types
            for (SubType subType : addedBasicLandTypes) {
                switch (subType) {
                    case FOREST:
                        if (!permanent.getAbilities(game).containsClass(GreenManaAbility.class)) {
                            permanent.addAbility(new GreenManaAbility(), source.getSourceId(), game);
                        }
                        break;
                    case ISLAND:
                        if (!permanent.getAbilities(game).containsClass(BlueManaAbility.class)) {
                            permanent.addAbility(new BlueManaAbility(), source.getSourceId(), game);
                        }
                        break;
                    case SWAMP:
                        if (!permanent.getAbilities(game).containsClass(BlackManaAbility.class)) {
                            permanent.addAbility(new BlackManaAbility(), source.getSourceId(), game);
                        }
                        break;
                    case MOUNTAIN:
                        if (!permanent.getAbilities(game).containsClass(RedManaAbility.class)) {
                            permanent.addAbility(new RedManaAbility(), source.getSourceId(), game);
                        }
                        break;
                    case PLAINS:
                        if (!permanent.getAbilities(game).containsClass(WhiteManaAbility.class)) {
                            permanent.addAbility(new WhiteManaAbility(), source.getSourceId(), game);
                        }
                        break;
                }
            }
        }
    }

    private void handleSuperTypes(Game game, MageObject mageObject) {
        if (removedSuperTypes != null) {
            for (SuperType superType : removedSuperTypes) {
                mageObject.removeSuperType(game, superType);
            }
        }
        if (addedSuperTypes != null) {
            for (SuperType superType : addedSuperTypes) {
                mageObject.addSuperType(game, superType);
            }
        }
    }

    private void handleCardTypes(Game game, MageObject mageObject) {
        List<CardType> toRemove = new ArrayList<>();

        if (removedCardTypes != null) {
            toRemove.addAll(removedCardTypes);
        }
        if (addedCardTypes != null) {
            if (removeOtherCardTypes) {
            // 205.1a. Some effects set an object's card type. In most such cases, the new card type(s)
            // replaces any existing card types. However, an object with either the instant or sorcery card type retains that type.
            mageObject.getCardType(game).stream()
                    .filter(cardType -> cardType != CardType.INSTANT && cardType != CardType.SORCERY)
                    .forEach(toRemove::add);
            }
            mageObject.addCardType(game, addedCardTypes.toArray(new CardType[0]));
        }
        for (CardType cardType : toRemove) {
            mageObject.removeCardType(game, cardType);
            // if removing card type, also remove related subtypes
            // 205.1a. ...If an object's card type is removed, the subtypes correlated with that card type will
            // remain if they are also the subtypes of a card type the object currently has;
            // otherwise, they are also removed for the entire time the object's card type is removed.
            if (cardType == CardType.CREATURE && mageObject.getCardType(game).contains(CardType.KINDRED)
                    || cardType == CardType.KINDRED && mageObject.getCardType(game).contains(CardType.CREATURE)) {
                continue;
            }
            mageObject.removeAllSubTypes(game, cardType.getSubTypeSet());
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        if (!source.getAffectedObjects().isEmpty()) {
            // re-use already affected objects across multiple layers CR 613.6
            affectedObjects.addAll(source.getAffectedObjects());
            return true;
        }
        if (staticAffectedObjects != null && !staticAffectedObjects.isEmpty()) {
            affectedObjects.addAll(getStaticAffectedObjects(game));
            return !affectedObjects.isEmpty();
        }
        if (!getTargetPointer().getTargets(game, source).isEmpty()) {
            for (UUID uuid : getTargetPointer().getTargets(game, source)) {
                MageItem object = game.getObject(uuid);
                if (object != null) {
                    affectedObjects.add(object);
                }
            }
            return !affectedObjects.isEmpty();
        }
        if (affected != ContinuousAffected.STATIC_OR_DYNAMIC) {
            return handleSpecialAffected(source, game, controller, affectedObjects);
        }

        if (affectedZones == null || affectedZones.isEmpty()) {
            return false;
        }
        for (Zone zone : affectedZones) {
            getObjectsFromZone(game, zone, controller, source, affectedObjects);
        }
        if (additionalLayers != null && !additionalLayers.isEmpty()) {
            source.getAffectedObjects().addAll(affectedObjects);
        }
        return !affectedObjects.isEmpty();
    }

    private boolean handleSpecialAffected(Ability source, Game game, Player controller, List<MageItem> affectedObjects) {
        switch (affected) {
            case SOURCE:
                MageObject sourceObject = game.getObject(source.getSourceId());
                if (sourceObject != null) {
                    affectedObjects.add(sourceObject);
                }
                break;
            case EXILED_WITH_SOURCE:
                ExileZone exileZone = game.getExile().getExileZone(CardUtil.getExileZoneId(
                        game, source.getSourceId(), game.getState().getZoneChangeCounter(source.getSourceId())
                ));
                if (exileZone != null && !exileZone.isEmpty()) {
                    affectedObjects.addAll(exileZone.getCards(game).stream()
                            .filter(card -> cardFilter == null || cardFilter.match(card, controller.getId(), source, game))
                            .collect(Collectors.toList()));
                }
                break;
            case ATTACHED_TO:
                Permanent sourcePermanent = game.getPermanent(source.getSourceId());
                if (sourcePermanent == null || sourcePermanent.getAttachedTo() == null) {
                    return false;
                }
                Permanent attachedTo = game.getPermanent(sourcePermanent.getAttachedTo());
                if (attachedTo != null) {
                    affectedObjects.add(attachedTo);
                }
                break;
            case TOP_OF_LIBRARY:
                Card topCard = controller.getLibrary().getFromTop(game);
                if (topCard != null && (cardFilter == null || cardFilter.match(topCard, controller.getId(), source, game))) {
                    affectedObjects.add(topCard);
                }
                break;
            default:
                return false;
        }
        return !affectedObjects.isEmpty();
    }

    protected void getObjectsFromZone(Game game, Zone zone, Player controller, Ability source, List<MageItem> affectedObjects) {
        if (this.cardsControlledBy.equals(TargetController.YOU)) {
            getPlayersObjectsFromZone(game, zone, controller, source, affectedObjects);
            return;
        }

        for (UUID playerId : game.getOpponents(controller.getId(), true)) {
            Player opponent = game.getPlayer(playerId);
            if (opponent == null) {
                continue;
            }
            getPlayersObjectsFromZone(game, zone, opponent, source, affectedObjects);
        }
        if (this.cardsControlledBy.equals(TargetController.EACH_PLAYER)) {
            getPlayersObjectsFromZone(game, zone, controller, source, affectedObjects);
        }
    }

    protected void getPlayersObjectsFromZone(Game game, Zone zone, Player player, Ability source, List<MageItem> affectedObjects) {
        switch (zone) {
            case GRAVEYARD:
                affectedObjects.addAll(player.getGraveyard().getCards(game).stream()
                        .filter(card -> cardFilter == null || cardFilter.match(card, player.getId(), source, game))
                        .collect(Collectors.toList()));
                break;
            case HAND:
                affectedObjects.addAll(player.getHand().getCards(game).stream()
                        .filter(card -> cardFilter == null || cardFilter.match(card, player.getId(), source, game))
                        .collect(Collectors.toList()));
                break;
            case LIBRARY:
                affectedObjects.addAll(player.getLibrary().getCards(game).stream()
                        .filter(card -> cardFilter == null || cardFilter.match(card, player.getId(), source, game))
                        .collect(Collectors.toList()));
                break;
            case EXILED:
                affectedObjects.addAll(game.getExile().getCardsOwned(cardFilter, player.getId(), source, game));
                break;
            case COMMAND:
                for (Object commObj : game.getState().getCommand()) {
                    if (commObj instanceof Commander) {
                        Card card = game.getCard(((Commander) commObj).getId());
                        if (card != null && card.getControllerOrOwnerId().equals(player.getId()) &&
                                (cardFilter == null || cardFilter.match(card, player.getId(), source, game))) {
                            affectedObjects.add(card);
                        }
                    }
                }
                break;
            case STACK:
                affectedObjects.addAll(game.getStack().stream()
                        .filter(stackObject -> stackObject.getControllerId().equals(player.getId()))
                        .filter(stackObject -> stackObjectFilter != null ? stackObjectFilter.match(stackObject, player.getId(), source, game)
                                : cardFilter == null || (stackObject instanceof Spell && cardFilter.match(((Spell) stackObject), player.getId(), source, game)))
                                .map(stackObject -> game.getCard(stackObject.getSourceId()))
                        .collect(Collectors.toList())
                );
                break;
            case BATTLEFIELD:
                if (permanentFilter == null) {
                    throw new IllegalArgumentException("Permanent filter must be defined for battlefield zone");
                }
                for (Permanent permanent : game.getBattlefield().getAllActivePermanents(player.getId())) {
                    if (permanentFilter.match(permanent, player.getId(), source, game)) {
                        affectedObjects.add(permanent);
                    }
                }
                break;
        }
    }

    /**
     * Get the static affected objects, removing any that are no longer valid.
     */
    private List<MageItem> getStaticAffectedObjects(Game game) {
        List<MageItem> affectedObjects = new ArrayList<>();
        if (staticAffectedObjects != null && !staticAffectedObjects.isEmpty()) {
            for (Iterator<MageObjectReference> it = staticAffectedObjects.iterator(); it.hasNext(); ) {
                MageObjectReference mor = it.next();
                MageObject object;
                if (affectedZones.contains(Zone.BATTLEFIELD)) {
                    object = mor.getPermanentOrLKIBattlefield(game);
                } else if (affectedZones.contains(Zone.STACK)) {
                    object = mor.getSpell(game);
                } else {
                    object = mor.getCard(game);
                }
                if (object != null) {
                    affectedObjects.add(object);
                } else {
                    it.remove();
                }
            }
        }
        return affectedObjects;
    }

    /**
     * Set the controller whose objects are affected by this effect.
     */
    public ContinuousEffectBuilder setCardsControlledBy(TargetController cardsControlledBy) {
        this.cardsControlledBy = cardsControlledBy;
        return this;
    }

    /**
     * Set the zones the effect applies to.
     */
    public ContinuousEffectBuilder setAffectedZones(Zone... affectedZones) {
        this.affectedZones = new ArrayList<>();
        Collections.addAll(this.affectedZones, affectedZones);
        return this;
    }

    /**
     * Set the filter for stack objects (spells and abilities on the stack) that will be affected
     */
    public ContinuousEffectBuilder setStackObjectFilter(FilterStackObject stackObjectFilter) {
        this.stackObjectFilter = stackObjectFilter;
        return this;
    }

    /**
     * Set the filter for permanents that will be affected on the battlefield
     */
    public ContinuousEffectBuilder setPermanentFilter(FilterPermanent permanentFilter) {
        this.permanentFilter = permanentFilter;
        return this;
    }

    /**
     * Set the filter for cards that will be affected in zones other than the battlefield
     */
    public ContinuousEffectBuilder setCardFilter(FilterCard cardFilter) {
        this.cardFilter = cardFilter;
        return this;
    }

    /**
     * Add abilities to the affected objects
     */
    public ContinuousEffectBuilder withGainedAbilities(Ability... gainedAbilities) {
        this.gainedAbilities = new ArrayList<>();
        Collections.addAll(this.gainedAbilities, gainedAbilities);
        this.addLayer(Layer.AbilityAddingRemovingEffects_6);
        return this;
    }

    /**
     * Used to add abilities like Flashback where cost is equal to the card's mana cost.
     * <br>
     * e.g. (card) -> new FlashbackAbility(card, card.getManaCost())
     * <br>
     * See SnapCaster Mage for an example.
     */
    public ContinuousEffectBuilder withGainedAbility(MakeAbilityFunction function) {
        this.makeAbilityFunction = function;
        this.addLayer(Layer.AbilityAddingRemovingEffects_6);
        return this;
    }

    /**
     * Remove all other abilities, not gained from this effect, from the affected objects
     */
    public ContinuousEffectBuilder withRemoveOtherAbilities() {
        this.removeOtherAbilities = true;
        this.addLayer(Layer.AbilityAddingRemovingEffects_6);
        return this;
    }

    /**
     * Remove the specified abilities from the affected objects with the matching classes
     */
    @SafeVarargs
    public final ContinuousEffectBuilder withRemoveAbilities(Class<? extends Ability>... abilitiesToRemove) {
        if (this.abilitiesToRemove == null) {
            this.abilitiesToRemove = new ArrayList<>();
        }
        Collections.addAll(this.abilitiesToRemove, abilitiesToRemove);
        this.addLayer(Layer.AbilityAddingRemovingEffects_6);
        return this;
    }

    /**
     * Add power to the affected objects.
     */
    public ContinuousEffectBuilder withAddPower(int power) {
        return withAddPower(StaticValue.get(power));
    }

    /**
     * Add power to the affected objects.
     */
    public ContinuousEffectBuilder withAddPower(DynamicValue power) {
        setPowerModifier(power);
        this.addLayer(Layer.PTChangingEffects_7);
        this.addSubLayer(SubLayer.ModifyPT_7c);
        return this;
    }

    /**
     * Set power to the affected objects. Sublayer is set based on {@link #affected}
     * <br>
     * 7a - Characteristic Defining Ability (CDA) if affected is SOURCE
     * <br>
     * 7b - Set PT for all other cases
     */
    public ContinuousEffectBuilder withSetPower(int power) {
        return withSetPower(StaticValue.get(power));
    }

    /**
     * Set power to the affected objects. Sublayer is set based on {@link #affected}
     * <br>
     * 7a - Characteristic Defining Ability (CDA) if affected is SOURCE
     * <br>
     * 7b - Set PT for all other cases
     */
    public ContinuousEffectBuilder withSetPower(DynamicValue power) {
        this.basePower = power;
        this.addLayer(Layer.PTChangingEffects_7);
        this.addSubLayer(affected == ContinuousAffected.SOURCE ? SubLayer.CharacteristicDefining_7a : SubLayer.SetPT_7b);
        return this;
    }

    /**
     * Add toughness to the affected objects.
     */
    public ContinuousEffectBuilder withAddToughness(int toughness) {
        return withAddToughness(StaticValue.get(toughness));
    }

    /**
     * Add toughness to the affected objects.
     */
    public ContinuousEffectBuilder withAddToughness(DynamicValue toughness) {
        setToughnessModifier(toughness);
        this.addLayer(Layer.PTChangingEffects_7);
        this.addSubLayer(SubLayer.ModifyPT_7c);
        return this;
    }

    /**
     * Set toughness to the affected objects. Sublayer is set based on {@link #affected}
     * <br>
     * 7a - Characteristic Defining Ability (CDA) if affected is SOURCE
     * <br>
     * 7b - Set PT for all other cases
     */
    public ContinuousEffectBuilder withSetToughness(int toughness) {
        return withSetToughness(StaticValue.get(toughness));
    }

    /**
     * Set toughness to the affected objects. Sublayer is set based on {@link #affected}
     * <br>
     * 7a - Characteristic Defining Ability (CDA) if affected is SOURCE
     * <br>
     * 7b - Set PT for all other cases
     */
    public ContinuousEffectBuilder withSetToughness(DynamicValue toughness) {
        this.baseToughness = toughness;
        this.addLayer(Layer.PTChangingEffects_7);
        this.addSubLayer(affected == ContinuousAffected.SOURCE ? SubLayer.CharacteristicDefining_7a : SubLayer.SetPT_7b);
        return this;
    }

    /**
     * Set power and toughness to the affected objects. Use for non-CDA effects only.
     */
    public ContinuousEffectBuilder withSetPowerAndToughness(int power, int toughness) {
        this.basePower = StaticValue.get(power);
        this.baseToughness = StaticValue.get(toughness);
        this.addLayer(Layer.PTChangingEffects_7);
        this.addSubLayer(SubLayer.SetPT_7b);
        return this;
    }

    private void setPowerModifier(DynamicValue powerModifier) {
        this.powerModifier = powerModifier;
        if (toughnessModifier == null) {
            this.toughnessModifier = StaticValue.get(0);
        }
    }

    private void setToughnessModifier(DynamicValue toughnessModifier) {
        this.toughnessModifier = toughnessModifier;
        if (powerModifier == null) {
            this.powerModifier = StaticValue.get(0);
        }
    }

    /**
     * Add card types to the affected objects and defaults to remove other card types.
     */
    public ContinuousEffectBuilder withAddedCardTypes(CardType... cardTypes) {
        return withAddedCardTypes(true, cardTypes);
    }

    /**
     * Add card types to the affected objects. {@link #removeOtherCardTypes} should be true in most cases.
     * If the effect says "in addition to its other types" or "still a [type, supertype, subtype]" then set false.
     * If the effect sets Artifact and Creature, it should also be false. This is handled by the function.
     * @param removeOtherCardTypes if true, removes other card types and related subtypes
     */
    public ContinuousEffectBuilder withAddedCardTypes(boolean removeOtherCardTypes, CardType... cardTypes) {
        addedCardTypes = Arrays.asList(cardTypes);
        this.addLayer(Layer.TypeChangingEffects_4);
        if (addedCardTypes.contains(CardType.CREATURE) && addedCardTypes.contains(CardType.ARTIFACT)) {
            // 205.1a. ...Some effects state that an object becomes an "artifact creature";
            // these effects also allow the object to retain all of its prior card types and subtypes.
            this.removeOtherCardTypes = false;
            this.removeOtherSubtypes = false;
        } else {
            this.removeOtherCardTypes = removeOtherCardTypes;
        }
        return this;
    }

    public ContinuousEffectBuilder withRemovedCardTypes(CardType... cardTypes) {
        removedCardTypes = Arrays.asList(cardTypes);
        this.addLayer(Layer.TypeChangingEffects_4);
        this.removeOtherCardTypes = true;
        return this;
    }

    public ContinuousEffectBuilder withAddedSuperTypes(SuperType... superTypes) {
        addedSuperTypes = Arrays.asList(superTypes);
        this.addLayer(Layer.TypeChangingEffects_4);
        return this;
    }

    public ContinuousEffectBuilder withRemovedSuperTypes(SuperType... superTypes) {
        removedSuperTypes = Arrays.asList(superTypes);
        this.addLayer(Layer.TypeChangingEffects_4);
        return this;
    }

    /**
     * Add subtypes to the affected objects. If the effect says "in addition to its other types" or
     * "still a [subtype]." then set removeOtherSubTypes to false.
     */
    public ContinuousEffectBuilder withAddedSubTypes(SubType... subTypes) {
        return withAddedSubTypes(true, subTypes);
    }

    /**
     * Add subtypes to the affected objects. If the effect says "in addition to its other types" or
     * "still a [subtype]." then set removeOtherSubTypes to false.
     * @param removeOtherSubTypes if true, removes other subtypes in the same SubTypeSet(s) as the added subtypes
     */
    public ContinuousEffectBuilder withAddedSubTypes(boolean removeOtherSubTypes, SubType... subTypes) {
        addedSubTypes = Arrays.asList(subTypes);
        this.addLayer(Layer.TypeChangingEffects_4);
        this.removeOtherSubtypes = removeOtherSubTypes;
        return this;
    }

    public ContinuousEffectBuilder withRemovedSubTypes(SubType... subTypes) {
        removedSubTypes = Arrays.asList(subTypes);
        this.addLayer(Layer.TypeChangingEffects_4);
        this.removeOtherSubtypes = true;
        return this;
    }

    public ContinuousEffectBuilder withRemovedSubTypeSets(SubTypeSet... subTypeSets) {
        removedSubTypeSets = Arrays.asList(subTypeSets);
        this.addLayer(Layer.TypeChangingEffects_4);
        return this;
    }

    public ContinuousEffectBuilder withGainChosenCreatureType(boolean removeOtherCardTypes) {
        this.removeOtherSubtypes = removeOtherCardTypes;
        this.addLayer(Layer.TypeChangingEffects_4);
        this.useChosenCreatureType = true;
        return this;
    }

    public ContinuousEffectBuilder setRemoveOtherSubtypes(boolean removeOtherSubtypes) {
        this.removeOtherSubtypes = removeOtherSubtypes;
        return this;
    }

    public ContinuousEffectBuilder withIsEveryCreatureType() {
        this.addLayer(Layer.TypeChangingEffects_4);
        this.useEveryCreatureType = true;
        return this;
    }

    public ContinuousEffectBuilder withIsEveryLandType() {
        this.addLayer(Layer.TypeChangingEffects_4);
        this.useEveryLandType = true;
        return this;
    }

    public ContinuousEffectBuilder withAddedColor(boolean removeOtherColors, ObjectColor color) {
        addedColor = new ObjectColor(color);
        this.addLayer(Layer.ColorChangingEffects_5);
        this.removeOtherColors = removeOtherColors;
        return this;
    }

    /**
     * Gain control of affected permanents
     */
    public ContinuousEffectBuilder withGainControl() {
        this.addLayer(Layer.ControlChangingEffects_2);
        return this;
    }

    /**
     * Switch power and toughness of affected permanents
     */
    public ContinuousEffectBuilder withSwitchPT() {
        this.addLayer(Layer.PTChangingEffects_7);
        this.addSubLayer(SubLayer.SwitchPT_e);
        return this;
    }

    public void addLayer(Layer layer) {
        if (this.layer == null) {
            this.layer = layer;
        } else {
            if (additionalLayers == null) {
                additionalLayers = new java.util.ArrayList<>();
            }
            if (!additionalLayers.contains(layer)) {
                additionalLayers.add(layer);
            }
        }
    }

    public void addSubLayer(SubLayer sublayer) {
        if (this.sublayer == null) {
            this.sublayer = sublayer;
        } else {
            if (additionalSublayers == null) {
                additionalSublayers = new java.util.ArrayList<>();
            }
            if (!additionalSublayers.contains(sublayer)) {
                additionalSublayers.add(sublayer);
            }
        }
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return this.layer == layer || (additionalLayers != null && additionalLayers.contains(layer));
    }

    @Override
    public boolean hasSubLayer(SubLayer sublayer) {
        return this.sublayer == sublayer || sublayer == SubLayer.NA || (additionalSublayers != null && additionalSublayers.contains(sublayer));
    }

    @Override
    public String getText(Mode mode) {
        String result = staticText;

        if (permanentFilter != null) {
            result = result.replace("{permFilter}", permanentFilter.getMessage());
        }
        if (cardFilter != null) {
            result = result.replace("{cardFilter}", cardFilter.getMessage());
        }
        if (affectedZones != null) {
            result = result.replace("{affectedZones}", affectedZones.stream()
                    .map(Zone::toString)
                    .collect(Collectors.joining(", ")));
        }
        if (gainedAbilities != null) {
            String abilitiesText = gainedAbilities.stream()
                    .map(Ability::getRule)
                    .collect(Collectors.joining(", "));
            result = result.replace("{gainedAbilitiesQuotes}", "\"" + abilitiesText + "\"")
                           .replace("{gainedAbilities}", abilitiesText);
        }
        if (powerModifier != null && toughnessModifier != null) {
            result = result.replace("{ptMod}", CardUtil.getBoostCountAsStr(powerModifier, toughnessModifier));
        }
        if (basePower != null && baseToughness != null) {
            result = result.replace("{basePT}", "base power and toughness " + basePower + "/" + baseToughness);
        }

        return result;
    }
}
