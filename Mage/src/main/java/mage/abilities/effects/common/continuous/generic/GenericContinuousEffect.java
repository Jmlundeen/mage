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
import mage.filter.FilterTyped;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.targetpointer.FixedTargets;
import mage.util.CardUtil;
import mage.util.ObjectQuery;

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
public class GenericContinuousEffect extends ContinuousEffectImpl {

    protected FilterTyped filter;
    protected ContinuousAffected affected;
    protected EnumSet<Zone> affectedZones;
    protected List<Ability> gainedAbilities;
    protected EnumSet<Layer> additionalLayers;
    protected EnumSet<SubLayer> additionalSublayers;
    protected DynamicValue powerModifier;
    protected DynamicValue toughnessModifier;
    protected DynamicValue basePower;
    protected DynamicValue baseToughness;
    protected EnumSet<CardType> addedCardTypes;
    protected EnumSet<CardType> removedCardTypes;
    protected EnumSet<SuperType> addedSuperTypes;
    protected EnumSet<SuperType> removedSuperTypes;
    protected EnumSet<SubType> addedSubTypes;
    protected EnumSet<SubType> removedSubTypes;
    protected EnumSet<SubTypeSet> removedSubTypeSets;
    protected ObjectColor addedColor;
    protected boolean removeOtherAbilities;
    protected boolean removeOtherCardTypes = true;
    protected boolean removeOtherSubtypes = true;
    protected boolean useChosenCreatureType;
    protected boolean useEveryCreatureType;
    protected boolean useEveryLandType;
    protected boolean removeOtherColors;
    protected MakeAbilityFunction makeAbilityFunction;
    protected Map<UUID, Ability> createdAbilities;
    protected int cachedAbilityZcc;
    protected List<Class<? extends Ability>> abilitiesToRemove;

    public GenericContinuousEffect(Duration duration, Outcome outcome, FilterTyped filter, Zone... affectedZones) {
        this(duration, outcome, filter, ContinuousAffected.STATIC_OR_DYNAMIC, affectedZones);
    }

    public GenericContinuousEffect(Outcome outcome, FilterTyped filter, Zone... affectedZones) {
        this(Duration.WhileOnBattlefield, outcome, filter, affectedZones);
    }

    public GenericContinuousEffect(Outcome outcome, FilterTyped filter, ContinuousAffected affected, Zone... affectedZones) {
        this(Duration.WhileOnBattlefield, outcome, filter, affected, affectedZones);
    }

    public GenericContinuousEffect(Duration duration, Outcome outcome, FilterTyped filter, ContinuousAffected affected, Zone... affectedZones) {
        super(duration, outcome);
        this.filter = filter;
        this.affected = affected;
        setAffectedZonesInternal(affectedZones == null || affectedZones.length == 0 ? new Zone[]{Zone.BATTLEFIELD} : affectedZones);
    }

    protected GenericContinuousEffect(final GenericContinuousEffect effect) {
        super(effect);
        this.filter = effect.filter == null ? null : effect.filter.copy();
        this.affected = effect.affected;
        this.affectedZones = effect.affectedZones == null ? null : EnumSet.copyOf(effect.affectedZones);
        this.gainedAbilities = effect.gainedAbilities == null ? null : new ArrayList<>(effect.gainedAbilities);
        this.additionalLayers = effect.additionalLayers == null ? null : EnumSet.copyOf(effect.additionalLayers);
        this.additionalSublayers = effect.additionalSublayers == null ? null : EnumSet.copyOf(effect.additionalSublayers);
        this.powerModifier = effect.powerModifier;
        this.toughnessModifier = effect.toughnessModifier;
        this.basePower = effect.basePower;
        this.baseToughness = effect.baseToughness;
        this.addedCardTypes = effect.addedCardTypes == null ? null : EnumSet.copyOf(effect.addedCardTypes);
        this.removedCardTypes = effect.removedCardTypes == null ? null : EnumSet.copyOf(effect.removedCardTypes);
        this.addedSuperTypes = effect.addedSuperTypes == null ? null : EnumSet.copyOf(effect.addedSuperTypes);
        this.removedSuperTypes = effect.removedSuperTypes == null ? null : EnumSet.copyOf(effect.removedSuperTypes);
        this.addedSubTypes = effect.addedSubTypes == null ? null : EnumSet.copyOf(effect.addedSubTypes);
        this.removedSubTypes = effect.removedSubTypes == null ? null : EnumSet.copyOf(effect.removedSubTypes);
        this.removedSubTypeSets = effect.removedSubTypeSets == null ? null : EnumSet.copyOf(effect.removedSubTypeSets);
        this.addedColor = effect.addedColor == null ? null : new ObjectColor(effect.addedColor);
        this.removeOtherAbilities = effect.removeOtherAbilities;
        this.removeOtherCardTypes = effect.removeOtherCardTypes;
        this.removeOtherSubtypes = effect.removeOtherSubtypes;
        this.useChosenCreatureType = effect.useChosenCreatureType;
        this.useEveryCreatureType = effect.useEveryCreatureType;
        this.useEveryLandType = effect.useEveryLandType;
        this.removeOtherColors = effect.removeOtherColors;
        this.makeAbilityFunction = effect.makeAbilityFunction;
        this.createdAbilities = effect.createdAbilities == null ? null : new HashMap<>(effect.createdAbilities);
        this.abilitiesToRemove = effect.abilitiesToRemove == null ? null : new ArrayList<>(effect.abilitiesToRemove);
        this.cachedAbilityZcc = effect.cachedAbilityZcc;
    }

    public GenericContinuousEffect(Duration duration, Outcome outcome) {
        super(duration, outcome);
        this.affected = ContinuousAffected.SOURCE;
    }

    @Override
    public GenericContinuousEffect copy() {
        return new GenericContinuousEffect(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        if (getAffectedObjectsSet() && affected == ContinuousAffected.STATIC_OR_DYNAMIC && getTargetPointer().getTargets(game, source).isEmpty()) {
            List<MageItem> affectedObjects = new ArrayList<>();
            queryAffectedObjects(layer, source, game, affectedObjects);
            this.setTargetPointer(new FixedTargets(affectedObjects.stream()
                    .filter(MageObject.class::isInstance)
                    .map(MageObject.class::cast)
                    .map(mageObject -> new MageObjectReference(mageObject, game))
                    .collect(Collectors.toList()))
            );
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
                    break;
                default:
                    break;
            }
        }
    }

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

        createdAbilities.entrySet().removeIf(entry -> !currentAffectedIds.contains(entry.getKey()));
    }

    private void addPowerAndToughness(Ability source, Game game, MageObject mageObject) {
        if (powerModifier == null || toughnessModifier == null) {
            throw new IllegalArgumentException("Power and/or toughness modifier not set in GenericContinuousEffect");
        }
        if (mageObject instanceof Permanent permanent) {
            permanent.addPower(powerModifier.calculate(game, source, this, permanent));
            permanent.addToughness(toughnessModifier.calculate(game, source, this, permanent));
        }
    }

    private void setBasePowerAndToughness(Ability source, Game game, MageObject mageObject) {
        if (basePower == null && baseToughness == null) {
            throw new IllegalArgumentException("Power and toughness not set in GenericContinuousEffect");
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
        if (makeAbilityFunction != null && mageObject instanceof Card card) {
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
                    : makeAbilityFunction.makeAbility(card, source, game);

            if (abilityToAdd != null) {
                createdAbilities.put(mageObject.getId(), abilityToAdd);
                addAbilityToObject(source, game, mageObject, abilityToAdd);
            }
        }
    }

    protected void addAbilityToObject(Ability source, Game game, MageObject mageObject, Ability abilityToAdd) {
        if (mageObject instanceof Permanent permanent) {
            permanent.addAbility(abilityToAdd, source.getSourceId(), game);
        } else if (mageObject instanceof Card card) {
            game.getState().addOtherAbility(card, abilityToAdd);
        }
    }

    private void removeAbilities(Ability source, Game game, MageObject mageObject) {
        if (removeOtherAbilities) {
            if (mageObject instanceof Permanent permanent) {
                permanent.removeAllAbilities(source.getSourceId(), game);
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
                if (mageObject instanceof Permanent permanent) {
                    permanent.removeAbility(ability, source.getSourceId(), game);
                } else if (mageObject instanceof Card) {
                    Iterator<Ability> iterator = game.getState().getCardState(mageObject.getId()).getAbilities().iterator();
                    while (iterator.hasNext()) {
                        if (iterator.next().equals(ability)) {
                            iterator.remove();
                        }
                    }
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
        if (removedSubTypeSets != null) {
            for (SubTypeSet subTypeSet : removedSubTypeSets) {
                if (subTypeSet == SubTypeSet.CreatureType) {
                    mageObject.setIsAllCreatureTypes(game, false);
                }
                if (subTypeSet == SubTypeSet.NonBasicLandType) {
                    mageObject.setIsAllNonbasicLandTypes(game, false);
                }
                mageObject.removeAllSubTypes(game, subTypeSet);
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
            EnumSet<SubType> addedBasicLandTypes = EnumSet.noneOf(SubType.class);
            Set<SubTypeSet> setsToRemove = new HashSet<>();
            for (SubType subType : addedSubTypes) {
                setsToRemove.add(subType.getSubTypeSet());
            }
            boolean artifactCreatureCondition = addedCardTypes != null
                    && addedCardTypes.contains(CardType.ARTIFACT)
                    && addedCardTypes.contains(CardType.CREATURE);
            for (SubTypeSet set : setsToRemove) {
                if (removeOtherSubtypes && !(set == SubTypeSet.CreatureType && artifactCreatureCondition)) {
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

    private static void checkManaAbilities(Ability source, Game game, MageObject mageObject, EnumSet<SubType> addedBasicLandTypes) {
        if (!(mageObject instanceof Permanent permanent)) {
            return;
        }

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
                    default:
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
        EnumSet<CardType> toRemove = EnumSet.noneOf(CardType.class);

        if (removedCardTypes != null) {
            toRemove.addAll(removedCardTypes);
        }
        if (addedCardTypes != null) {
            if (removeOtherCardTypes) {
                mageObject.getCardType(game).stream()
                        .filter(cardType -> cardType != CardType.INSTANT && cardType != CardType.SORCERY)
                        .forEach(toRemove::add);
            }
            mageObject.addCardType(game, addedCardTypes.toArray(new CardType[0]));
        }
        for (CardType cardType : toRemove) {
            mageObject.removeCardType(game, cardType);
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
            affectedObjects.addAll(source.getAffectedObjects());
            return true;
        }
        if (!getTargetPointer().getTargets(game, source).isEmpty()) {
            for (UUID uuid : getTargetPointer().getTargets(game, source)) {
                MageItem object = game.getObject(uuid);
                if (object != null) {
                    affectedObjects.add(object);
                }
            }
            if (affectedObjects.isEmpty()) {
                discard();
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
            ObjectQuery.getObjectsFromZone(game, zone, controller, source, affectedObjects, filter);
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
                            .filter(card -> filter == null || filter.match(card, controller.getId(), source, game))
                            .toList());
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
                if (topCard != null && (filter == null || filter.match(topCard, controller.getId(), source, game))) {
                    affectedObjects.add(topCard);
                }
                break;
            default:
                return false;
        }
        return !affectedObjects.isEmpty();
    }

    void addLayerInternal(Layer layer) {
        if (this.layer == null) {
            this.layer = layer;
        } else {
            if (additionalLayers == null) {
                additionalLayers = EnumSet.noneOf(Layer.class);
            }
            additionalLayers.add(layer);
        }
    }

    void addSubLayerInternal(SubLayer sublayer) {
        if (this.sublayer == null) {
            this.sublayer = sublayer;
        } else {
            if (additionalSublayers == null) {
                additionalSublayers = EnumSet.noneOf(SubLayer.class);
            }
            additionalSublayers.add(sublayer);
        }
    }

    private void setAffectedZonesInternal(Zone... zones) {
        this.affectedZones = EnumSet.noneOf(Zone.class);
        if (zones != null) {
            Collections.addAll(this.affectedZones, zones);
        }
    }

    /**
     * Set the filter for this effect, restricting which objects are affected.
     */
    public GenericContinuousEffect setFilter(FilterTyped filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Set which objects are affected by this effect. The default is {@link ContinuousAffected#STATIC_OR_DYNAMIC}
     */
    public GenericContinuousEffect setAffected(ContinuousAffected affected) {
        this.affected = affected;
        return this;
    }

    /**
     * Set the zones in which this effect applies. Only necessary if affected is set to {@link ContinuousAffected#STATIC_OR_DYNAMIC}.
     * supports {@link Zone#ALL}
     */
    public GenericContinuousEffect setAffectedZones(Zone... affectedZones) {
        setAffectedZonesInternal(affectedZones);
        return this;
    }

    /**
     * Set the abilities to be gained by the affected objects.
     */
    public GenericContinuousEffect withGainedAbilities(Ability... gainedAbilities) {
        this.gainedAbilities = new ArrayList<>();
        Collections.addAll(this.gainedAbilities, gainedAbilities);
        addLayerInternal(Layer.AbilityAddingRemovingEffects_6);
        return this;
    }

    /**
     * Used to add abilities that require runtime modifications, like Flashback where cost is equal to the card's mana cost.
     * <br>
     * e.g. (card) -> new FlashbackAbility(card, card.getManaCost())
     * <br>
     * See SnapCaster Mage for an example.
     */
    public GenericContinuousEffect withGainedAbility(MakeAbilityFunction function) {
        this.makeAbilityFunction = function;
        addLayerInternal(Layer.AbilityAddingRemovingEffects_6);
        return this;
    }

    /**
     * Remove all other abilities, not gained from this effect, from the affected objects
     */
    public GenericContinuousEffect withRemoveOtherAbilities() {
        this.removeOtherAbilities = true;
        addLayerInternal(Layer.AbilityAddingRemovingEffects_6);
        return this;
    }

    /**
     * Remove the specified abilities from the affected objects with the matching classes
     */
    @SafeVarargs
    public final GenericContinuousEffect withRemoveAbilities(Class<? extends Ability>... abilitiesToRemove) {
        if (this.abilitiesToRemove == null) {
            this.abilitiesToRemove = new ArrayList<>();
        }
        Collections.addAll(this.abilitiesToRemove, abilitiesToRemove);
        addLayerInternal(Layer.AbilityAddingRemovingEffects_6);
        return this;
    }

    /**
     * Add power to the affected objects.
     */
    public GenericContinuousEffect withAddPower(int power) {
        return withAddPower(StaticValue.get(power));
    }

    /**
     * Add power to the affected objects.
     */
    public GenericContinuousEffect withAddPower(DynamicValue power) {
        this.powerModifier = power;
        if (this.toughnessModifier == null) {
            this.toughnessModifier = StaticValue.get(0);
        }
        addLayerInternal(Layer.PTChangingEffects_7);
        addSubLayerInternal(SubLayer.ModifyPT_7c);
        return this;
    }

    /**
     * Set power to the affected objects. Sublayer is set based on {@link #affected}
     * <br>
     * 7a - Characteristic Defining Ability (CDA) if affected is SOURCE
     * <br>
     * 7b - Set PT for all other cases
     */
    public GenericContinuousEffect withSetPower(int power) {
        return withSetPower(StaticValue.get(power));
    }

    /**
     * Set power to the affected objects. Sublayer is set based on {@link #affected}
     * <br>
     * 7a - Characteristic Defining Ability (CDA) if affected is SOURCE
     * <br>
     * 7b - Set PT for all other cases
     */
    public GenericContinuousEffect withSetPower(DynamicValue power) {
        this.basePower = power;
        addLayerInternal(Layer.PTChangingEffects_7);
        addSubLayerInternal(this.affected == ContinuousAffected.SOURCE
                ? SubLayer.CharacteristicDefining_7a
                : SubLayer.SetPT_7b);
        return this;
    }

    /**
     * Add toughness to the affected objects.
     */
    public GenericContinuousEffect withAddToughness(int toughness) {
        return withAddToughness(StaticValue.get(toughness));
    }

    /**
     * Add toughness to the affected objects.
     */
    public GenericContinuousEffect withAddToughness(DynamicValue toughness) {
        this.toughnessModifier = toughness;
        if (this.powerModifier == null) {
            this.powerModifier = StaticValue.get(0);
        }
        addLayerInternal(Layer.PTChangingEffects_7);
        addSubLayerInternal(SubLayer.ModifyPT_7c);
        return this;
    }

    /**
     * Set toughness to the affected objects. Sublayer is set based on {@link #affected}
     * <br>
     * 7a - Characteristic Defining Ability (CDA) if affected is SOURCE
     * <br>
     * 7b - Set PT for all other cases
     */
    public GenericContinuousEffect withSetToughness(int toughness) {
        return withSetToughness(StaticValue.get(toughness));
    }

    /**
     * Set toughness to the affected objects. Sublayer is set based on {@link #affected}
     * <br>
     * 7a - Characteristic Defining Ability (CDA) if affected is SOURCE
     * <br>
     * 7b - Set PT for all other cases
     */
    public GenericContinuousEffect withSetToughness(DynamicValue toughness) {
        this.baseToughness = toughness;
        addLayerInternal(Layer.PTChangingEffects_7);
        addSubLayerInternal(this.affected == ContinuousAffected.SOURCE
                ? SubLayer.CharacteristicDefining_7a
                : SubLayer.SetPT_7b);
        return this;
    }

    /**
     * Set power and toughness to the affected objects. Use for non-CDA effects only.
     */
    public GenericContinuousEffect withSetPowerAndToughness(int power, int toughness) {
        this.basePower = StaticValue.get(power);
        this.baseToughness = StaticValue.get(toughness);
        addLayerInternal(Layer.PTChangingEffects_7);
        addSubLayerInternal(SubLayer.SetPT_7b);
        return this;
    }

    /**
     * Add card types to the affected objects and defaults to remove other card types.
     */
    public GenericContinuousEffect withAddedCardTypes(CardType... cardTypes) {
        return withAddedCardTypes(true, cardTypes);
    }

    /**
     * Add card types to the affected objects. {@link #removeOtherCardTypes} should be true in most cases.
     * If the effect says "in addition to its other types" or "still a [type, supertype, subtype]" then set false.
     * If the effect sets Artifact and Creature, it should also be false. This is handled by the function.
     * @param removeOtherCardTypes if true, removes other card types and related subtypes
     */
    public GenericContinuousEffect withAddedCardTypes(boolean removeOtherCardTypes, CardType... cardTypes) {
        this.addedCardTypes = EnumSet.noneOf(CardType.class);
        Collections.addAll(this.addedCardTypes, cardTypes);
        addLayerInternal(Layer.TypeChangingEffects_4);
        if (this.addedCardTypes.contains(CardType.CREATURE) && this.addedCardTypes.contains(CardType.ARTIFACT)) {
            this.removeOtherCardTypes = false;
            this.removeOtherSubtypes = false;
        } else {
            this.removeOtherCardTypes = removeOtherCardTypes;
        }
        return this;
    }

    /**
     * Remove card types from the affected objects.
     */
    public GenericContinuousEffect withRemovedCardTypes(CardType... cardTypes) {
        this.removedCardTypes = EnumSet.noneOf(CardType.class);
        Collections.addAll(this.removedCardTypes, cardTypes);
        addLayerInternal(Layer.TypeChangingEffects_4);
        return this;
    }

    /**
     * Add supertypes to the affected objects.
     */
    public GenericContinuousEffect withAddedSuperTypes(SuperType... superTypes) {
        this.addedSuperTypes = EnumSet.noneOf(SuperType.class);
        Collections.addAll(this.addedSuperTypes, superTypes);
        addLayerInternal(Layer.TypeChangingEffects_4);
        return this;
    }

    /**
     * Remove supertypes from the affected objects.
     */
    public GenericContinuousEffect withRemovedSuperTypes(SuperType... superTypes) {
        this.removedSuperTypes = EnumSet.noneOf(SuperType.class);
        Collections.addAll(this.removedSuperTypes, superTypes);
        addLayerInternal(Layer.TypeChangingEffects_4);
        return this;
    }

    /**
     * Add subtypes to the affected objects. If the effect says "in addition to its other types" or
     * "still a [subtype]." then set removeOtherSubTypes to false.
     */
    public GenericContinuousEffect withAddedSubTypes(SubType... subTypes) {
        return withAddedSubTypes(true, subTypes);
    }

    /**
     * Add subtypes to the affected objects. If the effect says "in addition to its other types" or
     * "still a [subtype]." then set removeOtherSubTypes to false.
     * @param removeOtherSubTypes if true, removes other subtypes in the same SubTypeSet(s) as the added subtypes
     */
    public GenericContinuousEffect withAddedSubTypes(boolean removeOtherSubTypes, SubType... subTypes) {
        this.addedSubTypes = EnumSet.noneOf(SubType.class);
        Collections.addAll(this.addedSubTypes, subTypes);
        addLayerInternal(Layer.TypeChangingEffects_4);
        this.removeOtherSubtypes = removeOtherSubTypes;
        return this;
    }

    /**
     * Remove subtypes from the affected objects.
     */
    public GenericContinuousEffect withRemovedSubTypes(SubType... subTypes) {
        this.removedSubTypes = EnumSet.noneOf(SubType.class);
        Collections.addAll(this.removedSubTypes, subTypes);
        addLayerInternal(Layer.TypeChangingEffects_4);
        return this;
    }

    /**
     * Remove subtypes from the affected objects based on SubTypeSets
     */
    public GenericContinuousEffect withRemovedSubTypeSets(SubTypeSet... subTypeSets) {
        this.removedSubTypeSets = EnumSet.noneOf(SubTypeSet.class);
        Collections.addAll(this.removedSubTypeSets, subTypeSets);
        addLayerInternal(Layer.TypeChangingEffects_4);
        return this;
    }

    /**
     * Adds the chosen creature type from {@link mage.abilities.effects.common.ChooseCreatureTypeEffect}
     */
    public GenericContinuousEffect withGainChosenCreatureType(boolean removeOtherSubTypes) {
        this.removeOtherSubtypes = removeOtherSubTypes;
        this.useChosenCreatureType = true;
        addLayerInternal(Layer.TypeChangingEffects_4);
        return this;
    }

    /**
     * Sets to other subtypes from affected objects
     */
    public GenericContinuousEffect setRemoveOtherSubtypes(boolean removeOtherSubtypes) {
        this.removeOtherSubtypes = removeOtherSubtypes;
        return this;
    }

    /**
     * Gives affected objects the all creature types flag
     */
    public GenericContinuousEffect withIsEveryCreatureType() {
        this.useEveryCreatureType = true;
        addLayerInternal(Layer.TypeChangingEffects_4);
        return this;
    }

    /**
     * Gives affected objects the all land types flag
     */
    public GenericContinuousEffect withIsEveryLandType() {
        this.useEveryLandType = true;
        addLayerInternal(Layer.TypeChangingEffects_4);
        return this;
    }

    /**
     * Add color to the affected objects. If the effect says "in addition to its other colors" then set removeOtherColors to false.
     */
    public GenericContinuousEffect withAddedColor(boolean removeOtherColors, ObjectColor color) {
        this.addedColor = new ObjectColor(color);
        this.removeOtherColors = removeOtherColors;
        addLayerInternal(Layer.ColorChangingEffects_5);
        return this;
    }

    /**
     * Gain control of the affected objects
     */
    public GenericContinuousEffect withGainControl() {
        addLayerInternal(Layer.ControlChangingEffects_2);
        return this;
    }

    /**
     * Switch power and toughness of the affected permanents
     */
    public GenericContinuousEffect withSwitchPT() {
        addLayerInternal(Layer.PTChangingEffects_7);
        addSubLayerInternal(SubLayer.SwitchPT_e);
        return this;
    }

    /**
     * Adds a layer to this effect.
     */
    public GenericContinuousEffect addLayer(Layer layer) {
        addLayerInternal(layer);
        return this;
    }

    /**
     * Adds a sublayer to this effect.
     */
    public GenericContinuousEffect addSubLayer(SubLayer sublayer) {
        addSubLayerInternal(sublayer);
        return this;
    }

    /**
     * Set the static text for this effect.
     */
    public GenericContinuousEffect setText(String staticText) {
        this.staticText = staticText;
        return this;
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

        if (filter != null) {
            result = result.replace("{filter}", filter.getMessage())
                    .replace("{permFilter}", filter.getMessage())
                    .replace("{cardFilter}", filter.getMessage());
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
            result = result.replace("{gainedAbilitiesQuotes}", '"' + abilitiesText + '"')
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



