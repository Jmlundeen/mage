package mage.abilities.effects.common.continuous.generic;

import mage.MageItem;
import mage.MageObject;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.cards.Card;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.FilterPermanent;
import mage.filter.FilterStackObject;
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
 */
public class ContinuousEffectBuilder extends ContinuousEffectImpl {

    private FilterStackObject stackObjectFilter;
    private FilterPermanent permanentFilter;
    private FilterCard cardFilter;
    private TargetController targetController;
    private ContinuousAffected affected = ContinuousAffected.STATIC;
    private List<MageObjectReference> staticAffectedObjects;
    private List<Zone> affectedZones;
    private List<Ability> gainedAbilities;
    private List<Layer> additionalLayers;
    private List<SubLayer> additionalSublayers;
    private DynamicValue powerModifier;
    private DynamicValue toughnessModifier;
    private DynamicValue basePower;
    private DynamicValue baseToughness;
    private CardType[] addedCardTypes;
    private SuperType[] addedSuperTypes;
    private SubType[] addedSubTypes;
    private boolean removeOtherAbilities;
    private boolean removeOtherCardTypes;
    private boolean removeOtherSuperTypes;
    private boolean removeOtherSubTypes;
    private MakeAbilityFunction makeAbilityFunction;
    private Map<UUID, Ability> createdAbilities; // cache created abilities to reduce ability creation

    /**
     * Creates a new standard ContinuousEffectBuilder.
     * Zones need to be set separately using {@link #setAffectedZones(Zone...)} if not using targets
     * @param duration
     * @param outcome
     */
    public ContinuousEffectBuilder(Duration duration, Outcome outcome) {
        super(duration, outcome);
    }

    /**
     * Creates a new ContinuousEffectBuilder. Use this for effects that work on the source object, controller, or permanent source is attached to.
     * Zones need to be set separately using {@link #setAffectedZones(Zone...)} if not using targets
     * @param duration
     * @param outcome
     * @param affected
     */
    public ContinuousEffectBuilder(Duration duration, Outcome outcome, ContinuousAffected affected) {
        super(duration, outcome);
        this.affected = affected;
    }

    /**
     * Creates a new ContinuousEffectBuilder. Use this for effects that work on the source object, controller, or permanent source is attached to.
     * Zones need to be set separately using {@link #setAffectedZones(Zone...)} if not using targets
     * @param outcome
     * @param affected
     */
    public ContinuousEffectBuilder(Outcome outcome, ContinuousAffected affected) {
        this(Duration.EndOfGame, outcome, affected);
    }

    /**
     * Creates a new ContinuousEffectBuilder that applies to objects controlled by the specified controller.
     * Make sure to set the affected zones using {@link #setAffectedZones(Zone...)}
     * @param duration
     * @param outcome
     * @param objectController
     */
    public ContinuousEffectBuilder(Duration duration, Outcome outcome, TargetController objectController) {
        super(duration, outcome);
        this.targetController = objectController;
        this.affectedZones = Collections.singletonList(Zone.BATTLEFIELD);
    }

    /**
     * Creates a new ContinuousEffectBuilder that applies to objects controlled by the specified controller.
     * Make sure to set the affected zones using {@link #setAffectedZones(Zone...)}
     * @param outcome
     * @param objectController
     */
    public ContinuousEffectBuilder(Outcome outcome, TargetController objectController) {
        super(Duration.WhileOnBattlefield, outcome);
        this.targetController = objectController;
        this.affectedZones = Collections.singletonList(Zone.BATTLEFIELD);
    }

    /**
     * Creates a new ContinuousEffectBuilder that applies to permanents on the battlefield the controlling player controls
     * @param outcome
     * @param permanentFilter
     */
    public ContinuousEffectBuilder(Outcome outcome, FilterPermanent permanentFilter) {
        super(Duration.WhileOnBattlefield, outcome);
        this.permanentFilter = permanentFilter;
        this.targetController = TargetController.YOU;
        this.affectedZones = Collections.singletonList(Zone.BATTLEFIELD);
    }

    /**
     * Creates a new ContinuousEffectBuilder that applies to permanents on the battlefield
     * @param duration
     * @param outcome
     * @param targetController the controller whose objects are affected. Use {@link TargetController#YOU}, {@link TargetController#OPPONENT}, {@link TargetController#EACH_PLAYER}
     * @param permanentFilter
     */
    public ContinuousEffectBuilder(Duration duration, Outcome outcome, TargetController targetController, FilterPermanent permanentFilter) {
        super(duration, outcome);
        this.permanentFilter = permanentFilter;
        this.targetController = targetController;
        this.affectedZones = Collections.singletonList(Zone.BATTLEFIELD);
    }

    /**
     * Creates a new ContinuousEffectBuilder that applies to Cards in the specified zones
     * @param duration
     * @param outcome
     * @param targetController
     * @param cardFilter
     * @param affectedZones
     */
    public ContinuousEffectBuilder(Duration duration, Outcome outcome, TargetController targetController,
                                   FilterCard cardFilter, Zone... affectedZones) {
        super(duration, outcome);
        this.cardFilter = cardFilter;
        this.targetController = targetController;
        this.affectedZones = new ArrayList<>();
        Collections.addAll(this.affectedZones, affectedZones);
    }

    protected ContinuousEffectBuilder(final ContinuousEffectBuilder effect) {
        super(effect);
        this.stackObjectFilter = effect.stackObjectFilter;
        this.permanentFilter = effect.permanentFilter;
        this.cardFilter = effect.cardFilter;
        this.targetController = effect.targetController;
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
        this.addedSuperTypes = effect.addedSuperTypes;
        this.addedSubTypes = effect.addedSubTypes;
        this.removeOtherAbilities = effect.removeOtherAbilities;
        this.removeOtherCardTypes = effect.removeOtherCardTypes;
        this.removeOtherSuperTypes = effect.removeOtherSuperTypes;
        this.removeOtherSubTypes = effect.removeOtherSubTypes;
        this.makeAbilityFunction = effect.makeAbilityFunction;
        this.createdAbilities = effect.createdAbilities == null ? null : new HashMap<>(effect.createdAbilities);
    }

    @Override
    public ContinuousEffectBuilder copy() {
        return new ContinuousEffectBuilder(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        if (getAffectedObjectsSet() && affected == ContinuousAffected.STATIC && getTargetPointer().getTargets(game, source).isEmpty()) {
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
        for (MageItem object : affectedObjects) {
            MageObject mageObject = (MageObject) object;
            switch (layer) {
                case TextChangingEffects_3:
                    // TODO: implement
                    break;
                case TypeChangingEffects_4:
                    if (addedCardTypes != null) {
                        if (removeOtherCardTypes) {
                            mageObject.removeAllCardTypes(game);
                        }
                        mageObject.addCardType(game, addedCardTypes);
                    }
                    if (addedSuperTypes != null) {
                        if (removeOtherSuperTypes) {
                            mageObject.removeAllSuperTypes(game);
                        }
                        for (SuperType superType : addedSuperTypes) {
                            mageObject.addSuperType(superType);
                        }
                    }
                    if (addedSubTypes != null) {
                        if (removeOtherSubTypes) {
                            mageObject.removeAllSubTypes(game);
                        }
                        mageObject.addSubType(game, addedSubTypes);
                    }
                    break;
                case ColorChangingEffects_5:
                    // TODO: implement
                    break;
                case AbilityAddingRemovingEffects_6:
                    if (gainedAbilities != null) {
                        for (Ability abilityToAdd : gainedAbilities) {
                            if (mageObject instanceof Permanent) {
                                if (removeOtherAbilities) {
                                    ((Permanent) mageObject).removeAllAbilities(source.getSourceId(), game);
                                }
                                ((Permanent) mageObject).addAbility(abilityToAdd, source.getSourceId(), game);
                            } else if (mageObject instanceof Card) {
                                if (removeOtherAbilities) {
                                    game.getState().getCardState(mageObject.getId()).clearAbilities();
                                }
                                game.getState().addOtherAbility((Card) mageObject, abilityToAdd);
                            }
                        }
                    }
                    if (makeAbilityFunction != null && mageObject instanceof Card) {
                        Ability abilityToAdd;
                        if (createdAbilities != null && createdAbilities.containsKey(mageObject.getId())) {
                            abilityToAdd = createdAbilities.get(mageObject.getId());
                        } else {
                            abilityToAdd = makeAbilityFunction.makeAbility((Card) mageObject, source, game);
                            if (createdAbilities == null) {
                                createdAbilities = new HashMap<>();
                            }
                            createdAbilities.put(mageObject.getId(), abilityToAdd);
                        }
                        if (abilityToAdd != null) {
                            if (mageObject instanceof Permanent) {
                                if (removeOtherAbilities) {
                                    ((Permanent) mageObject).removeAllAbilities(source.getSourceId(), game);
                                }
                                ((Permanent) mageObject).addAbility(abilityToAdd, source.getSourceId(), game);
                            } else {
                                if (removeOtherAbilities) {
                                    game.getState().getCardState(mageObject.getId()).clearAbilities();
                                }
                                game.getState().addOtherAbility((Card) mageObject, abilityToAdd);
                            }
                        }
                    }
                    break;
                case PTChangingEffects_7:
                    if (sublayer == SubLayer.CharacteristicDefining_7a) {
                        if (basePower == null && baseToughness == null) {
                            throw new IllegalArgumentException("Power and toughness not set in ContinuousEffectBuilder");
                        }
                        if (basePower != null) {
                            mageObject.getPower().setModifiedBaseValue(basePower.calculate(game, source, this));
                        }
                        if (baseToughness != null) {
                            mageObject.getToughness().setModifiedBaseValue(baseToughness.calculate(game, source, this));
                        }
                        break;
                    }
                    if (sublayer == SubLayer.SetPT_7b) {
                        if (basePower == null || baseToughness == null) {
                            throw new IllegalArgumentException("Power and/or toughness not set in ContinuousEffectBuilder");
                        }
                        if (mageObject instanceof Permanent) {
                            Permanent permanent = (Permanent) mageObject;
                            permanent.getPower().setModifiedBaseValue(basePower.calculate(game, source, this, permanent));
                            permanent.getToughness().setModifiedBaseValue(baseToughness.calculate(game, source, this, permanent));
                        }
                        break;
                    }
                    if (sublayer == SubLayer.ModifyPT_7c) {
                        if (powerModifier == null || toughnessModifier == null) {
                            throw new IllegalArgumentException("Power and/or toughness modifier not set in ContinuousEffectBuilder");
                        }
                        if (mageObject instanceof Permanent) {
                            Permanent permanent = (Permanent) mageObject;
                            permanent.addPower(powerModifier.calculate(game, source, this));
                            permanent.addToughness(toughnessModifier.calculate(game, source, this));
                        }
                        break;
                    }
                    if (sublayer == SubLayer.Counters_7d) {
                        // TODO: implement
                        break;
                    }
                    if (sublayer == SubLayer.SwitchPT_e) {
                        // TODO: implement
                        break;
                    }
            }
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
        if (affected == ContinuousAffected.SOURCE) {
            MageObject sourceObject = game.getObject(source.getSourceId());
            if (sourceObject != null) {
                affectedObjects.add(sourceObject);
            }
            return !affectedObjects.isEmpty();
        }
        if (affected == ContinuousAffected.ATTACHED_TO) {
            Permanent sourcePermanent = game.getPermanent(source.getSourceId());
            if (sourcePermanent == null || sourcePermanent.getAttachedTo() == null) {
                return false;
            }
            Permanent attachedTo = game.getPermanent(sourcePermanent.getAttachedTo());
            if (attachedTo != null) {
                affectedObjects.add(attachedTo);
            }
            return !affectedObjects.isEmpty();
        }
        if (affected == ContinuousAffected.TOP_OF_LIBRARY) {
            Card topCard = controller.getLibrary().getFromTop(game);
            if (topCard != null && (cardFilter == null || cardFilter.match(topCard, controller.getId(), source, game))) {
                affectedObjects.add(topCard);
            }
            return !affectedObjects.isEmpty();
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

    private void getObjectsFromZone(Game game, Zone zone, Player controller, Ability source, List<MageItem> affectedObjects) {
        if (this.targetController.equals(TargetController.YOU)) {
            getPlayersObjectsFromZone(game, zone, controller, source, affectedObjects);
            return;
        }

        for (UUID playerId : game.getOpponents(controller.getId(), true)) {
            Player opponent = game.getPlayer(playerId);
            if (opponent == null) {
                continue;
            }
            getPlayersObjectsFromZone(game, zone, controller, source, affectedObjects);
        }
        if (this.targetController.equals(TargetController.EACH_PLAYER)) {
            getPlayersObjectsFromZone(game, zone, controller, source, affectedObjects);
        }
    }

    private void getPlayersObjectsFromZone(Game game, Zone zone, Player player, Ability source, List<MageItem> affectedObjects) {

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
                        if (card != null && (cardFilter == null || cardFilter.match(card, player.getId(), source, game))) {
                            affectedObjects.add(card);
                        }
                    }
                }
                break;
            case STACK:
                affectedObjects.addAll(game.getStack().stream()
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
                affectedObjects.addAll(game.getBattlefield().getActivePermanents(permanentFilter, player.getId(), source, game));
                break;
        }
    }

    /**
     * Get the static affected objects, removing any that are no longer valid.
     * @param game
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
     * Set the zones the effect applies to.
     * @param affectedZones
     */
    public ContinuousEffectBuilder setAffectedZones(Zone... affectedZones) {
        this.affectedZones = new ArrayList<>();
        Collections.addAll(this.affectedZones, affectedZones);
        return this;
    }

    /**
     * Set the filter for stack objects (spells and abilities on the stack)
     * @param stackObjectFilter
     */
    public ContinuousEffectBuilder setStackObjectFilter(FilterStackObject stackObjectFilter) {
        this.stackObjectFilter = stackObjectFilter;
        return this;
    }

    /**
     * Set the filter for permanents on the battlefield
     * @param permanentFilter
     */
    public ContinuousEffectBuilder setPermanentFilter(FilterPermanent permanentFilter) {
        this.permanentFilter = permanentFilter;
        return this;
    }

    /**
     * Set the filter for cards in zones other than the battlefield
     * @param cardFilter
     */
    public ContinuousEffectBuilder setCardFilter(FilterCard cardFilter) {
        this.cardFilter = cardFilter;
        return this;
    }

    /**
     * Add abilities to the affected objects
     * @param gainedAbilities
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
     * @param function
     * @return
     */
    public ContinuousEffectBuilder withGainedAbility(MakeAbilityFunction function) {
        this.makeAbilityFunction = function;
        this.addLayer(Layer.AbilityAddingRemovingEffects_6);
        return this;
    }

    /**
     * Add power to the affected objects.
     * @param power
     */
    public ContinuousEffectBuilder withAddPower(int power) {
        setPowerModifier(StaticValue.get(power));
        this.addLayer(Layer.PTChangingEffects_7);
        this.addSubLayer(SubLayer.ModifyPT_7c);
        return this;
    }

    /**
     * Add power to the affected objects.
     * @param power
     */
    public ContinuousEffectBuilder withAddPower(DynamicValue power) {
        setPowerModifier(power);
        this.addLayer(Layer.PTChangingEffects_7);
        this.addSubLayer(SubLayer.ModifyPT_7c);
        return this;
    }

    /**
     * Set power to the affected objects. Use for Characteristic Defining Abilities (CDA) only.
     * @param power
     */
    public ContinuousEffectBuilder withSetPower(int power) {
        this.basePower = StaticValue.get(power);
        this.addLayer(Layer.PTChangingEffects_7);
        this.addSubLayer(SubLayer.CharacteristicDefining_7a);
        return this;
    }

    /**
     * Set power to the affected objects. Use for Characteristic Defining Abilities (CDA) only.
     * @param power
     */
    public ContinuousEffectBuilder withSetPower(DynamicValue power) {
        this.basePower = power;
        this.addLayer(Layer.PTChangingEffects_7);
        this.addSubLayer(SubLayer.CharacteristicDefining_7a);
        return this;
    }

    /**
     * Add toughness to the affected objects.
     * @param toughness
     */
    public ContinuousEffectBuilder withAddToughness(int toughness) {
        setToughnessModifier(StaticValue.get(toughness));
        this.addLayer(Layer.PTChangingEffects_7);
        this.addSubLayer(SubLayer.ModifyPT_7c);
        return this;
    }

    /**
     * Add toughness to the affected objects.
     * @param toughness
     */
    public ContinuousEffectBuilder withAddToughness(DynamicValue toughness) {
        setToughnessModifier(toughness);
        this.addLayer(Layer.PTChangingEffects_7);
        this.addSubLayer(SubLayer.ModifyPT_7c);
        return this;
    }

    /**
     * Set toughness to the affected objects. Use for Characteristic Defining Abilities (CDA) only.
     * @param toughness
     */
    public ContinuousEffectBuilder withSetToughness(int toughness) {
        this.baseToughness = StaticValue.get(toughness);
        this.addLayer(Layer.PTChangingEffects_7);
        this.addSubLayer(SubLayer.CharacteristicDefining_7a);
        return this;
    }

    /**
     * Set toughness to the affected objects. Use for Characteristic Defining Abilities (CDA) only.
     * @param toughness
     */
    public ContinuousEffectBuilder withSetToughness(DynamicValue toughness) {
        this.baseToughness = toughness;
        this.addLayer(Layer.PTChangingEffects_7);
        this.addSubLayer(SubLayer.CharacteristicDefining_7a);
        return this;
    }

    /**
     * Set power and toughness to the affected objects. Use for non-CDA effects only.
     * @param power
     * @param toughness
     */
    public ContinuousEffectBuilder withSetPowerAndToughness(int power, int toughness) {
        this.basePower = StaticValue.get(power);
        this.baseToughness = StaticValue.get(toughness);
        this.addLayer(Layer.PTChangingEffects_7);
        this.addSubLayer(SubLayer.SetPT_7b);
        return this;
    }

    /**
     * Set power and toughness to the affected objects. Use for non-CDA effects only.
     * @param power
     * @param toughness
     */
    public ContinuousEffectBuilder withSetPowerAndToughness(DynamicValue power, DynamicValue toughness) {
        this.basePower = power;
        this.baseToughness = toughness;
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

    public ContinuousEffectBuilder withAddedCardTypes(boolean removeOtherCardTypes, CardType... cardTypes) {
        if (addedCardTypes == null) {
            addedCardTypes = cardTypes;
        }
        this.addLayer(Layer.TypeChangingEffects_4);
        this.removeOtherCardTypes = removeOtherCardTypes;
        return this;
    }

    public ContinuousEffectBuilder withAddedSuperTypes(boolean removeOtherSuperTypes, SuperType... superTypes) {
        if (addedSuperTypes == null) {
            addedSuperTypes = superTypes;
        }
        this.addLayer(Layer.TypeChangingEffects_4);
        this.removeOtherSuperTypes = removeOtherSuperTypes;
        return this;
    }

    public ContinuousEffectBuilder withAddedSubTypes(boolean removeOtherSubTypes, SubType... subTypes) {
        if (addedSubTypes == null) {
            addedSubTypes = subTypes;
        }
        this.addLayer(Layer.TypeChangingEffects_4);
        this.removeOtherSubTypes = removeOtherSubTypes;
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
