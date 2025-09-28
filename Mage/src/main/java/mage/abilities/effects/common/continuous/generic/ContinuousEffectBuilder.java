package mage.abilities.effects.common.continuous.generic;

import mage.MageItem;
import mage.MageObject;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ContinuousEffectBuilder extends ContinuousEffectImpl {

    private FilterStackObject stackObjectFilter;
    private FilterPermanent permanentFilter;
    private FilterCard cardFilter;
    private TargetController targetController;
    private ContinuousAffected affected;
    private List<Zone> affectedZones;
    private List<Ability> gainedAbilities;
    private List<Layer> additionalLayers;
    private List<SubLayer> additionalSublayers;
    private DynamicValue powerModifier;
    private DynamicValue toughnessModifier;
    private DynamicValue basePower;
    private DynamicValue baseToughness;

    /**
     * Creates a new ContinuousEffectBuilder. Use this for effects that work on the source object, players, or permanent source is attached to.
     * Zones need to be set separately using {@link #setAffectedZones(List)} or {@link #setAffectedZones(Zone...)}
     * @param duration
     * @param outcome
     * @param affected
     */
    public ContinuousEffectBuilder(Duration duration, Outcome outcome, ContinuousAffected affected) {
        super(duration, outcome);
        this.affected = affected;
    }

    /**
     * Creates a new ContinuousEffectBuilder. Use this for effects that work on the source object, players, or permanent source is attached to.
     * Zones need to be set separately using {@link #setAffectedZones(List)} or {@link #setAffectedZones(Zone...)}
     * @param outcome
     * @param affected
     */
    public ContinuousEffectBuilder(Outcome outcome, ContinuousAffected affected) {
        this(Duration.EndOfGame, outcome, affected);
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
        this.affected = effect.affected;
        this.affectedZones = effect.affectedZones;
        this.gainedAbilities = effect.gainedAbilities;
        this.additionalLayers = effect.additionalLayers;
        this.additionalSublayers = effect.additionalSublayers;
        this.powerModifier = effect.powerModifier;
        this.toughnessModifier = effect.toughnessModifier;
        this.basePower = effect.basePower;
        this.baseToughness = effect.baseToughness;
    }

    @Override
    public ContinuousEffectBuilder copy() {
        return new ContinuousEffectBuilder(this);
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
                    if (gainedAbilities != null) {
                        for (Ability abilityToAdd : gainedAbilities) {
                            if (mageObject instanceof Permanent) {
                                ((Permanent) mageObject).addAbility(abilityToAdd, source.getSourceId(), game);
                            } else if (mageObject instanceof Card) {
                                game.getState().addOtherAbility((Card) mageObject, abilityToAdd);
                            }
                        }
                    }
                    break;
                case ColorChangingEffects_5:
                    // TODO: implement
                    break;
                case AbilityAddingRemovingEffects_6:
                    // TODO: implement
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
                            permanent.getPower().setModifiedBaseValue(basePower.calculate(game, source, this));
                            permanent.getToughness().setModifiedBaseValue(baseToughness.calculate(game, source, this));
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
        if (affectedZones == null || affectedZones.isEmpty()) {
            return false;
        }
        for (Zone zone : affectedZones) {
            getObjectsFromZone(game, zone, controller, source, affectedObjects);
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
                                : cardFilter == null || (stackObject instanceof Spell && cardFilter.match(((Spell) stackObject).getCard(), player.getId(), source, game)))
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
     * Set the zones the effect applies to.
     * @param affectedZones
     */
    public ContinuousEffectBuilder setAffectedZones(List<Zone> affectedZones) {
        this.affectedZones = affectedZones;
        return this;
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
     * Add abilities to the affected objects
     * @param gainedAbilities
     */
    public ContinuousEffectBuilder withGainedAbilities(Ability... gainedAbilities) {
        this.gainedAbilities = new ArrayList<>();
        Collections.addAll(this.gainedAbilities, gainedAbilities);
        this.addLayer(Layer.TypeChangingEffects_4);
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

    public void setPowerModifier(DynamicValue powerModifier) {
        this.powerModifier = powerModifier;
        if (toughnessModifier == null) {
            this.toughnessModifier = StaticValue.get(0);
        }
    }

    public void setToughnessModifier(DynamicValue toughnessModifier) {
        this.toughnessModifier = toughnessModifier;
        if (powerModifier == null) {
            this.powerModifier = StaticValue.get(0);
        }
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
