package mage.abilities.effects;

import mage.ApprovingObject;
import mage.MageItem;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.MageSingleton;
import mage.abilities.StaticAbility;
import mage.abilities.effects.common.continuous.BecomesFaceDownCreatureEffect;
import mage.abilities.effects.common.continuous.CommanderReplacementEffect;
import mage.cards.*;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.predicate.Predicate;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.CardIdPredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.permanent.Permanent;
import mage.game.permanent.PermanentCard;
import mage.game.stack.Spell;
import mage.players.ManaPoolItem;
import mage.players.Player;
import mage.target.common.TargetCardInHand;
import org.apache.log4j.Logger;
import org.jgrapht.alg.cycle.SzwarcfiterLauerSimpleCycles;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class ContinuousEffects implements Serializable {

    private static final Logger logger = Logger.getLogger(ContinuousEffects.class);

    private long order = 0;

    //transient Continuous effects
    private ContinuousEffectsList<ContinuousEffect> layeredEffects = new ContinuousEffectsList<>();
    private ContinuousEffectsList<ContinuousRuleModifyingEffect> continuousRuleModifyingEffects = new ContinuousEffectsList<>();
    private ContinuousEffectsList<ReplacementEffect> replacementEffects = new ContinuousEffectsList<>();
    private ContinuousEffectsList<PreventionEffect> preventionEffects = new ContinuousEffectsList<>();
    private ContinuousEffectsList<RequirementEffect> requirementEffects = new ContinuousEffectsList<>();
    private ContinuousEffectsList<RestrictionEffect> restrictionEffects = new ContinuousEffectsList<>();
    private ContinuousEffectsList<RestrictionUntapNotMoreThanEffect> restrictionUntapNotMoreThanEffects = new ContinuousEffectsList<>();
    private ContinuousEffectsList<CostModificationEffect> costModificationEffects = new ContinuousEffectsList<>();
    private ContinuousEffectsList<SpliceCardEffect> spliceCardEffects = new ContinuousEffectsList<>();

    private final Map<AsThoughEffectType, ContinuousEffectsList<AsThoughEffect>> asThoughEffectsMap = new EnumMap<>(AsThoughEffectType.class);
    public final List<ContinuousEffectsList<?>> allEffectsLists = new ArrayList<>(); // contains refs to real effect's list
    private final ApplyStatusEffect applyStatus;
    private final AuraReplacementEffect auraReplacementEffect;

    private final List<ContinuousEffect> lastEffectList = new ArrayList<>();
    private final Set<UUID> appliedEffects = new HashSet<>();
    private final Map<Layer, DefaultDirectedGraph<ContinuousEffect, DefaultEdge>> layerDependencies = new EnumMap<>(Layer.class);


    public ContinuousEffects() {
        applyStatus = new ApplyStatusEffect();
        auraReplacementEffect = new AuraReplacementEffect();
        collectAllEffects();
    }

    protected ContinuousEffects(final ContinuousEffects effect) {
        applyStatus = effect.applyStatus.copy();
        auraReplacementEffect = effect.auraReplacementEffect.copy();
        layeredEffects = effect.layeredEffects.copy();
        continuousRuleModifyingEffects = effect.continuousRuleModifyingEffects.copy();
        replacementEffects = effect.replacementEffects.copy();
        preventionEffects = effect.preventionEffects.copy();
        requirementEffects = effect.requirementEffects.copy();
        restrictionEffects = effect.restrictionEffects.copy();
        restrictionUntapNotMoreThanEffects = effect.restrictionUntapNotMoreThanEffects.copy();
        for (Map.Entry<AsThoughEffectType, ContinuousEffectsList<AsThoughEffect>> entry : effect.asThoughEffectsMap.entrySet()) {
            asThoughEffectsMap.put(entry.getKey(), entry.getValue().copy());
        }

        costModificationEffects = effect.costModificationEffects.copy();
        spliceCardEffects = effect.spliceCardEffects.copy();
        lastEffectList.addAll(effect.lastEffectList);
        collectAllEffects();
        order = effect.order;
    }

    private synchronized void collectAllEffects() {
        allEffectsLists.add(layeredEffects);
        allEffectsLists.add(continuousRuleModifyingEffects);
        allEffectsLists.add(replacementEffects);
        allEffectsLists.add(preventionEffects);
        allEffectsLists.add(requirementEffects);
        allEffectsLists.add(restrictionEffects);
        allEffectsLists.add(restrictionUntapNotMoreThanEffects);
        allEffectsLists.add(costModificationEffects);
        allEffectsLists.add(spliceCardEffects);
        for (ContinuousEffectsList continuousEffectsList : asThoughEffectsMap.values()) {
            allEffectsLists.add(continuousEffectsList);
        }
    }

    public ContinuousEffects copy() {
        return new ContinuousEffects(this);
    }

    public List<RequirementEffect> getRequirementEffects() {
        return requirementEffects;
    }

    public List<RestrictionEffect> getRestrictionEffects() {
        return restrictionEffects;
    }

    public synchronized void removeEndOfCombatEffects() {
        layeredEffects.removeEndOfCombatEffects();
        continuousRuleModifyingEffects.removeEndOfCombatEffects();
        replacementEffects.removeEndOfCombatEffects();
        preventionEffects.removeEndOfCombatEffects();
        requirementEffects.removeEndOfCombatEffects();
        restrictionEffects.removeEndOfCombatEffects();
        for (ContinuousEffectsList asThoughlist : asThoughEffectsMap.values()) {
            asThoughlist.removeEndOfCombatEffects();
        }
        costModificationEffects.removeEndOfCombatEffects();
        spliceCardEffects.removeEndOfCombatEffects();
    }

    public synchronized void removeEndOfTurnEffects(Game game) {
        layeredEffects.removeEndOfTurnEffects(game);
        continuousRuleModifyingEffects.removeEndOfTurnEffects(game);
        replacementEffects.removeEndOfTurnEffects(game);
        preventionEffects.removeEndOfTurnEffects(game);
        requirementEffects.removeEndOfTurnEffects(game);
        restrictionEffects.removeEndOfTurnEffects(game);
        for (ContinuousEffectsList asThoughlist : asThoughEffectsMap.values()) {
            asThoughlist.removeEndOfTurnEffects(game);
        }
        costModificationEffects.removeEndOfTurnEffects(game);
        spliceCardEffects.removeEndOfTurnEffects(game);
    }

    public synchronized void removeBeginningOfEndStepEffects(Game game) {
        layeredEffects.removeBeginningOfEndStepEffects(game);
        continuousRuleModifyingEffects.removeBeginningOfEndStepEffects(game);
        replacementEffects.removeBeginningOfEndStepEffects(game);
        preventionEffects.removeBeginningOfEndStepEffects(game);
        requirementEffects.removeBeginningOfEndStepEffects(game);
        restrictionEffects.removeBeginningOfEndStepEffects(game);
        for (ContinuousEffectsList asThoughlist : asThoughEffectsMap.values()) {
            asThoughlist.removeBeginningOfEndStepEffects(game);
        }
        costModificationEffects.removeBeginningOfEndStepEffects(game);
        spliceCardEffects.removeBeginningOfEndStepEffects(game);
    }

    public synchronized void removeInactiveEffects(Game game) {
        layeredEffects.removeInactiveEffects(game);
        continuousRuleModifyingEffects.removeInactiveEffects(game);
        replacementEffects.removeInactiveEffects(game);
        preventionEffects.removeInactiveEffects(game);
        requirementEffects.removeInactiveEffects(game);
        restrictionEffects.removeInactiveEffects(game);
        restrictionUntapNotMoreThanEffects.removeInactiveEffects(game);
        for (ContinuousEffectsList asThoughtlist : asThoughEffectsMap.values()) {
            asThoughtlist.removeInactiveEffects(game);
        }
        costModificationEffects.removeInactiveEffects(game);
        spliceCardEffects.removeInactiveEffects(game);
    }

    public synchronized List<ContinuousEffect> getLayeredEffects(Game game) {
        return getLayeredEffects(game, false);
    }

    /**
     * Return effects list ordered by timestamps (timestamps are automaticity
     * generates from new/old lists on same layer)
     *
     * @param game
     * @param clearPrevious clears the {@link #lastEffectList} before adding effects. This is used at the start
     *                      of applying to determine actually active effects.
     * @return effects list ordered by timestamp
     */
    public synchronized List<ContinuousEffect> getLayeredEffects(Game game, boolean clearPrevious) {
        List<ContinuousEffect> layerEffects = new ArrayList<>();
        for (ContinuousEffect effect : layeredEffects) {
            if (this.appliedEffects.contains(effect.getId())) {
                // 613.6  If an effect should be applied in different layers and/or sublayers,
                // the parts of the effect each apply in their appropriate ones.
                // If an effect starts to apply in one layer and/or sublayer,
                // it will continue to be applied to the same set of objects in each other applicable layer and/or sublayer,
                // even if the ability generating the effect is removed during this process.
                layerEffects.add(effect);
                continue;
            }
            Set<Ability> abilities = layeredEffects.getAbility(effect.getId());
            if (!abilities.isEmpty()) {
                for (Ability ability : abilities) {
                    if (clearPrevious) {
                        // reset ability affected objects on first call
                        ability.getAffectedObjects().clear();
                    }
                    switch (effect.getDuration()) {
                        case WhileOnBattlefield:
                        case WhileControlled:
                        case WhileOnStack:
                        case WhileInGraveyard:
                            // If e.g. triggerd abilities (non static) created the effect, the ability must not be in usable zone (e.g. Unearth giving Haste effect)
                            if (!(ability instanceof StaticAbility) || ability.isInUseableZone(game, null, null)) {
                                layerEffects.add(effect);
                                if (!lastEffectList.contains(effect)) {
                                    effect.init(ability, game);
                                }
                                break;
                            }
                            break;
                        default:
                            layerEffects.add(effect);
                    }
                }
            }
            else {
                    logger.error("No abilities for continuous effect: " + effect);
            }
        }

        updateTimestamps(clearPrevious, layerEffects);
        layerEffects.sort(Comparator.comparingLong(ContinuousEffect::getOrder));
        /* debug effects apply order:
        if (game.getStep() != null) System.out.println("layr - " + game.getTurnNum() + "." + game.getTurnStepType() + ": layers " + layerEffects.size()
                + " - " + layerEffects.stream().map(l -> l.getClass().getSimpleName()).collect(Collectors.joining(", "))
                + " - " + callName);
        //*/

        return layerEffects;
    }

    /**
     * Initially effect timestamp is set when game starts in game.loadCard
     * method. After that timestamp should be updated whenever effect becomes
     * "actual" meaning it becomes turned on that is defined by
     * Ability.#isInUseableZone(Game, boolean) method in
     * #getLayeredEffects(Game).
     * <p>
     * It must be called with different timestamp group name (otherwise sort
     * order will be changed for add/remove effects, see Urborg and Bloodmoon
     * test)
     *
     * @param layerEffects
     */
    private synchronized void updateTimestamps(boolean clearEffects, List<ContinuousEffect> layerEffects) {
        List<ContinuousEffect> toAdd = new ArrayList<>();
        for (ContinuousEffect continuousEffect : layerEffects) {
            // check if it's new, then set order
            boolean found = false;
            for (ContinuousEffect lastEffect : lastEffectList) {
                if (lastEffect.getId() == continuousEffect.getId()) {
                    found = true;
                    break;
                }
            }
            if (found) {
                continue;
            }
            setOrder(continuousEffect);
            toAdd.add(continuousEffect);
        }
        lastEffectList.addAll(toAdd);
        if (clearEffects) {
            if (layerEffects.size() != lastEffectList.size()) {
                layerDependencies.clear();
            }
            lastEffectList.clear();
            lastEffectList.addAll(layerEffects);
        }

    }

    public void setOrder(ContinuousEffect effect) {
        effect.setOrder(order++);
    }

    public void resetDependencies() {
        layerDependencies.clear();
    }

    private List<ContinuousEffect> filterLayeredEffects(List<ContinuousEffect> effects, Layer layer, SubLayer subLayer) {
        return effects.stream()
                .filter(effect -> effect.hasLayer(layer))
                .filter(effect -> effect.hasSubLayer(subLayer))
                .collect(Collectors.toList());
    }

    public Map<RequirementEffect, Set<Ability>> getApplicableRequirementEffects(Permanent permanent, boolean playerRelated, Game game) {
        Map<RequirementEffect, Set<Ability>> effects = new HashMap<>();
        for (RequirementEffect effect : requirementEffects) {
            if (playerRelated == effect.isPlayerRelated()) {
                Set<Ability> abilities = requirementEffects.getAbility(effect.getId());
                Set<Ability> applicableAbilities = new HashSet<>();
                for (Ability ability : abilities) {
                    if (!(ability instanceof StaticAbility) || ability.isInUseableZone(game, ability instanceof MageSingleton ? permanent : null, null)) {
                        if (effect.applies(permanent, ability, game)) {
                            applicableAbilities.add(ability);
                        }
                    }
                }
                if (!applicableAbilities.isEmpty()) {
                    effects.put(effect, abilities);
                }
            }
        }
        return effects;
    }

    public Map<RestrictionEffect, Set<Ability>> getApplicableRestrictionEffects(Permanent permanent, Game game) {
        Map<RestrictionEffect, Set<Ability>> effects = new HashMap<>();
        for (RestrictionEffect effect : restrictionEffects) {
            Set<Ability> abilities = restrictionEffects.getAbility(effect.getId());
            Set<Ability> applicableAbilities = new HashSet<>();
            for (Ability ability : abilities) {
                if (!(ability instanceof StaticAbility) || ability.isInUseableZone(game, ability instanceof MageSingleton ? permanent : null, null)) {
                    if (effect.applies(permanent, ability, game)) {
                        applicableAbilities.add(ability);
                    }
                }
            }
            if (!applicableAbilities.isEmpty()) {
                effects.put(effect, abilities);
            }
        }
        return effects;
    }

    public Map<RestrictionUntapNotMoreThanEffect, Set<Ability>> getApplicableRestrictionUntapNotMoreThanEffects(Player player, Game game) {
        Map<RestrictionUntapNotMoreThanEffect, Set<Ability>> effects = new HashMap<>();
        for (RestrictionUntapNotMoreThanEffect effect : restrictionUntapNotMoreThanEffects) {
            Set<Ability> abilities = restrictionUntapNotMoreThanEffects.getAbility(effect.getId());
            Set<Ability> applicableAbilities = new HashSet<>();
            for (Ability ability : abilities) {
                if (!(ability instanceof StaticAbility) || ability.isInUseableZone(game, null, null)) {
                    if (effect.applies(player, ability, game)) {
                        applicableAbilities.add(ability);
                    }
                }
            }
            if (!applicableAbilities.isEmpty()) {
                effects.put(effect, abilities);
            }
        }
        return effects;
    }

    public boolean checkIfThereArePayCostToAttackBlockEffects(GameEvent event, Game game) {
        for (ReplacementEffect effect : replacementEffects) {
            if (!effect.checksEventType(event, game)) {
                continue;
            }
            if (effect instanceof PayCostToAttackBlockEffect) {
                Set<Ability> abilities = replacementEffects.getAbility(effect.getId());
                for (Ability ability : abilities) {
                    // for replacement effects of static abilities do not use LKI to check if to apply
                    if (ability.getAbilityType() != AbilityType.STATIC || ability.isInUseableZone(game, null, event)) {
                        if (effect.getDuration() != Duration.OneUse || !effect.isUsed()) {
                            if (!game.getScopeRelevant() || effect.hasSelfScope() || !event.getTargetId().equals(ability.getSourceId())) {
                                if (effect.applies(event, ability, game)
                                        && !((PayCostToAttackBlockEffect) effect).isCostless(event, ability, game)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param event
     * @param game
     * @return a list of all {@link ReplacementEffect} that apply to the current
     * event
     */
    private Map<ReplacementEffect, Set<Ability>> getApplicableReplacementEffects(GameEvent event, Game game) {
        Map<ReplacementEffect, Set<Ability>> replaceEffects = new LinkedHashMap<>();
        if (auraReplacementEffect.checksEventType(event, game) && auraReplacementEffect.applies(event, null, game)) {
            replaceEffects.put(auraReplacementEffect, null);
        }
        // boolean checkLKI = event.getType().equals(EventType.ZONE_CHANGE) || event.getType().equals(EventType.DESTROYED_PERMANENT);
        //get all applicable transient Replacement effects
        for (Iterator<ReplacementEffect> iterator = replacementEffects.iterator(); iterator.hasNext(); ) {
            ReplacementEffect effect = iterator.next();
            if (!effect.checksEventType(event, game)) {
                continue;
            }
            if (event.getAppliedEffects() != null && event.getAppliedEffects().contains(effect.getId())) {
                if (!(effect instanceof CommanderReplacementEffect)) { // 903.9.
                    // Effect already applied to this event, ignore it
                    // TODO: Handle also gained effect that are connected to different abilities.
                    continue;
                }
            }
            Set<Ability> abilities = replacementEffects.getAbility(effect.getId());
            Set<Ability> applicableAbilities = new HashSet<>();
            for (Ability ability : abilities) {
                // for replacement effects of static abilities do not use LKI to check if to apply
                if (ability.getAbilityType() != AbilityType.STATIC || ability.isInUseableZone(game, null, event)) {
                    if (!effect.isUsed()) {
                        if (!game.getScopeRelevant()
                                || effect.hasSelfScope()
                                || !event.getTargetId().equals(ability.getSourceId())) {
                            if (effect.applies(event, ability, game)) {
                                applicableAbilities.add(ability);
                            }
                        }
                    }
                }
            }
            if (!applicableAbilities.isEmpty()) {
                replaceEffects.put(effect, applicableAbilities);
            }
        }

        for (Iterator<PreventionEffect> iterator = preventionEffects.iterator(); iterator.hasNext(); ) {
            PreventionEffect effect = iterator.next();
            if (!effect.checksEventType(event, game)) {
                continue;
            }
            if (event.getAppliedEffects() != null && event.getAppliedEffects().contains(effect.getId())) {
                // Effect already applied to this event, ignore it
                // TODO: Handle also gained effect that are connected to different abilities.
                continue;
            }
            Set<Ability> abilities = preventionEffects.getAbility(effect.getId());
            Set<Ability> applicableAbilities = new HashSet<>();
            for (Ability ability : abilities) {
                if (ability.getAbilityType() != AbilityType.STATIC || ability.isInUseableZone(game, null, event)) {
                    if (effect.getDuration() != Duration.OneUse || !effect.isUsed()) {
                        if (effect.applies(event, ability, game)) {
                            applicableAbilities.add(ability);
                        }
                    }
                }
            }
            if (!applicableAbilities.isEmpty()) {
                replaceEffects.put(effect, applicableAbilities);
            }
        }

        return replaceEffects;
    }

    private boolean checkAbilityStillExists(Ability ability, ContinuousEffect effect, GameEvent event, Game game) {
        switch (effect.getDuration()) { // effects with fixed duration don't need an object with the source ability (e.g. a silence cast with isochronic Scepter has no more a card object
            case EndOfCombat:
            case EndOfGame:
            case EndOfStep:
            case EndOfTurn:
            case OneUse:
            case Custom:  // custom duration means the effect ends itself if needed
                return true;
        }
        if (ability.getSourceId() == null) { // commander replacement effect
            return true;
        }
        MageObject object;
        if (event.getType() == GameEvent.EventType.ZONE_CHANGE
                && ((ZoneChangeEvent) event).getFromZone() == Zone.BATTLEFIELD
                && event.getTargetId().equals(ability.getSourceId())) {
            object = ((ZoneChangeEvent) event).getTarget();
        } else {
            object = game.getObject(ability.getSourceId());
        }
        if (object == null) {
            return false;
        }
        boolean exists = true;
        if (!object.hasAbility(ability, game)) {
            exists = false;
            if (object instanceof PermanentCard) {
                PermanentCard permanent = (PermanentCard) object;
                if (permanent.isTransformable() && event.getType() == GameEvent.EventType.TRANSFORMED) {
                    exists = permanent.getCard().hasAbility(ability, game);
                }
            }
        } else if (object instanceof PermanentCard) {
            PermanentCard permanent = (PermanentCard) object;
            if (permanent.isFaceDown() && !ability.getWorksFaceDown()) {
                return false;
            }
        } else if (object instanceof Spell) {
            Spell spell = (Spell) object;
            if (spell.isFaceDown() && !ability.getWorksFaceDown()) {
                return false;
            }
        }
        return exists;
    }

    /**
     * Filters out cost modification effects that are not active.
     *
     * @param game
     * @return
     */
    private List<CostModificationEffect> getApplicableCostModificationEffects(Game game) {
        return getApplicableCostModificationEffects(game, null);
    }

    /**
     * Filters out cost modification effects that are not active.
     *
     * @param game
     * @param sourceObject
     * @return
     */
    private List<CostModificationEffect> getApplicableCostModificationEffects(Game game, MageObject sourceObject) {
        List<CostModificationEffect> costEffects = new ArrayList<>();

        for (CostModificationEffect effect : costModificationEffects) {
            Set<Ability> abilities = costModificationEffects.getAbility(effect.getId());
            for (Ability ability : abilities) {
                if (!(ability instanceof StaticAbility) || ability.isInUseableZone(game, sourceObject, null)) {
                    if (effect.getDuration() != Duration.OneUse || !effect.isUsed()) {
                        costEffects.add(effect);
                        break;
                    }
                }
            }
        }

        return costEffects;
    }

    /**
     * Filters out splice effects that are not active.
     *
     * @param game
     * @return
     */
    private List<SpliceCardEffect> getApplicableSpliceCardEffects(Game game, UUID playerId) {
        List<SpliceCardEffect> spliceEffects = new ArrayList<>();

        for (SpliceCardEffect effect : spliceCardEffects) {
            Set<Ability> abilities = spliceCardEffects.getAbility(effect.getId());
            for (Ability ability : abilities) {
                if (ability.isControlledBy(playerId) && (!(ability instanceof StaticAbility) || ability.isInUseableZone(game, null, null))) {
                    if (effect.getDuration() != Duration.OneUse || !effect.isUsed()) {
                        spliceEffects.add(effect);
                        break;
                    }
                }
            }
        }

        return spliceEffects;
    }

    /**
     * @param objectId        object to check
     * @param type
     * @param affectedAbility null if check full object or ability if check only one ability from that object
     * @param controllerId
     * @param game
     * @return Set of all the ApprovingObject related to that asThough.
     */
    public Set<ApprovingObject> asThough(UUID objectId, AsThoughEffectType type, Ability affectedAbility, UUID controllerId, Game game) {
        Set<ApprovingObject> possibleApprovingObjects = new HashSet<>();

        // usage check: effect must apply for specific ability only, not to full object (example: PLAY_FROM_NOT_OWN_HAND_ZONE)
        if (type.needAffectedAbility() && affectedAbility == null) {
            throw new IllegalArgumentException("Wrong code usage: you can't call asThough check to whole object, call it with affected ability instead: " + type);
        }

        // usage check: effect must apply to full object, not specific ability (example: ATTACK_AS_HASTE)
        // P.S. In theory a same AsThough effect can be applied to object or to ability, so if you really, really
        // need it then disable that check or add extra param to AsThoughEffectType like needAffectedAbilityOrFullObject
        if (!type.needAffectedAbility() && affectedAbility != null) {
            throw new IllegalArgumentException("Wrong code usage: you can't call AsThough check to affected ability, call it with empty affected ability instead: " + type);
        }

        List<AsThoughEffect> asThoughEffectsList = getApplicableAsThoughEffects(type, game);
        if (!asThoughEffectsList.isEmpty()) {
            MageObject objectToCheck;
            if (affectedAbility != null) {
                objectToCheck = affectedAbility.getSourceObject(game);
            } else {
                objectToCheck = game.getCard(objectId);
            }

            UUID idToCheck;
            if (!type.needPlayCardAbility() && (objectToCheck instanceof CardWithParts || objectToCheck instanceof CardPart)) {
                // each split side uses own characteristics to check for playing, all other cases must use main card
                // rules:
                // 708.4. In every zone except the stack, the characteristics of a split card are those of its two halves combined.
                idToCheck = ((Card) objectToCheck).getMainCard().getId();
            } else {
                idToCheck = objectId;
            }

            for (AsThoughEffect effect : asThoughEffectsList) {
                Set<Ability> abilities = asThoughEffectsMap.get(type).getAbility(effect.getId());
                for (Ability ability : abilities) {
                    if (affectedAbility == null) {
                        // applies to full object (one effect can be used in multiple abilities)
                        if (effect.applies(idToCheck, ability, controllerId, game)) {
                            possibleApprovingObjects.add(new ApprovingObject(ability, game));
                        }
                    } else {
                        // applies to one affected ability

                        // filter play abilities (no need to check it in every effect's code)
                        if (type.needPlayCardAbility() && !affectedAbility.getAbilityType().isPlayCardAbility()) {
                            continue;
                        }

                        if (effect.applies(idToCheck, affectedAbility, ability, game, controllerId)) {
                            possibleApprovingObjects.add(new ApprovingObject(ability, game));
                        }
                    }
                }
            }
        }
        return possibleApprovingObjects;
    }

    /**
     * Fit paying mana type with current mana pool (if asThoughMana affected)
     * <p>
     * Example:
     * - you need to pay {R} as cost
     * - asThoughMana effect allows to use {G} as any color;
     * - asThoughMana effect must change/fit paying mana type from {R} to {G}
     * - after that you can pay {G} as cost
     *
     * @param manaType        paying mana type
     * @param mana            checking pool item
     * @param objectId        paying ability's source object
     * @param affectedAbility paying ability
     * @param controllerId    controller who pay
     * @param game
     * @return corrected paying mana type (same if no asThough effects and different on applied asThough effect)
     */
    public ManaType asThoughMana(ManaType manaType, ManaPoolItem mana, UUID objectId, Ability affectedAbility, UUID controllerId, Game game) {
        // First check existing only effects
        List<AsThoughEffect> asThoughEffectsList = getApplicableAsThoughEffects(AsThoughEffectType.SPEND_ONLY_MANA, game);
        for (AsThoughEffect effect : asThoughEffectsList) {
            Set<Ability> abilities = asThoughEffectsMap.get(AsThoughEffectType.SPEND_ONLY_MANA).getAbility(effect.getId());
            for (Ability ability : abilities) {
                if ((affectedAbility == null && effect.applies(objectId, ability, controllerId, game))
                        || effect.applies(objectId, affectedAbility, ability, game, controllerId)) {
                    if (((AsThoughManaEffect) effect).getAsThoughManaType(manaType, mana, controllerId, ability, game) == null) {
                        return null;
                    }
                }
            }
        }
        // then check effects that allow to use other mana types to pay the current mana type to pay
        asThoughEffectsList = getApplicableAsThoughEffects(AsThoughEffectType.SPEND_OTHER_MANA, game);
        for (AsThoughEffect effect : asThoughEffectsList) {
            Set<Ability> abilities = asThoughEffectsMap.get(AsThoughEffectType.SPEND_OTHER_MANA).getAbility(effect.getId());
            for (Ability ability : abilities) {
                if ((affectedAbility == null && effect.applies(objectId, ability, controllerId, game))
                        || effect.applies(objectId, affectedAbility, ability, game, controllerId)) {
                    ManaType usableManaType = ((AsThoughManaEffect) effect).getAsThoughManaType(manaType, mana, controllerId, ability, game);
                    if (usableManaType != null) {
                        return usableManaType;
                    }
                }
            }
        }
        return manaType;
    }

    /**
     * Filters out asThough effects that are not active.
     *
     * @param type type
     * @param game
     * @return
     */
    public List<AsThoughEffect> getApplicableAsThoughEffects(AsThoughEffectType type, Game game) {
        List<AsThoughEffect> asThoughEffectsList = new ArrayList<>();
        if (asThoughEffectsMap.containsKey(type)) {
            for (AsThoughEffect effect : asThoughEffectsMap.get(type)) {
                Set<Ability> abilities = asThoughEffectsMap.get(type).getAbility(effect.getId());
                for (Ability ability : abilities) {
                    if (!(ability instanceof StaticAbility) || ability.isInUseableZone(game, null, null)) {
                        if (effect.getDuration() != Duration.OneUse && !effect.isUsed()) {
                            asThoughEffectsList.add(effect);
                            break;
                        }
                    }
                }
            }
        }
        return asThoughEffectsList;
    }

    public Set<Ability> getAsThoughEffectsAbility(AsThoughEffect effect) {
        return asThoughEffectsMap.get(effect.getAsThoughEffectType()).getAbility(effect.getId());
    }


    public void costModification(Ability abilityToModify, Game game) {
        costModification(abilityToModify, game, null);
    }

    /**
     * 601.2e The player determines the total cost of the spell. Usually this is
     * just the mana cost. Some spells have additional or alternative costs.
     * Some effects may increase or reduce the cost to pay, or may provide other
     * alternative costs. Costs may include paying mana, tapping permanents,
     * sacrificing permanents, discarding cards, and so on. The total cost is
     * the mana cost or alternative cost (as determined in rule 601.2b), plus
     * all additional costs and cost increases, and minus all cost reductions.
     * If the mana component of the total cost is reduced to nothing by cost
     * reduction effects, it is considered to be {0}. It can't be reduced to
     * less than {0}. Once the total cost is determined, any effects that
     * directly affect the total cost are applied. Then the resulting total cost
     * becomes “locked in.” If effects would change the total cost after this
     * time, they have no effect.
     */
    /**
     * Inspects all {@link Permanent permanent's} {@link Ability abilities} on
     * the battlefield for
     * {@link CostModificationEffect cost modification effects} and applies them
     * if necessary.
     * <p>
     * Warning, don't forget to call ability.adjustX before any cost modifications
     *
     * @param abilityToModify
     * @param game
     */
    public void costModification(Ability abilityToModify, Game game, MageObject sourceObject) {
        List<CostModificationEffect> costEffects = getApplicableCostModificationEffects(game, sourceObject);

        // add dynamic costs from X and other places
        abilityToModify.adjustCostsPrepare(game);

        abilityToModify.adjustCostsModify(game, CostModificationType.INCREASE_COST);
        for (CostModificationEffect effect : costEffects) {
            if (effect.getModificationType() == CostModificationType.INCREASE_COST) {
                Set<Ability> abilities = costModificationEffects.getAbility(effect.getId());
                for (Ability ability : abilities) {
                    if (effect.applies(abilityToModify, ability, game)) {
                        effect.apply(game, ability, abilityToModify);
                    }
                }
            }
        }

        abilityToModify.adjustCostsModify(game, CostModificationType.REDUCE_COST);
        for (CostModificationEffect effect : costEffects) {
            if (effect.getModificationType() == CostModificationType.REDUCE_COST) {
                Set<Ability> abilities = costModificationEffects.getAbility(effect.getId());
                for (Ability ability : abilities) {
                    if (effect.applies(abilityToModify, ability, game)) {
                        effect.apply(game, ability, abilityToModify);
                    }
                }
            }
        }

        abilityToModify.adjustCostsModify(game, CostModificationType.SET_COST);
        for (CostModificationEffect effect : costEffects) {
            if (effect.getModificationType() == CostModificationType.SET_COST) {
                Set<Ability> abilities = costModificationEffects.getAbility(effect.getId());
                for (Ability ability : abilities) {
                    if (effect.applies(abilityToModify, ability, game)) {
                        effect.apply(game, ability, abilityToModify);
                    }
                }
            }
        }
    }

    /**
     * Checks all available splice effects to be applied.
     *
     * @param abilityToModify
     * @param game
     */
    public void applySpliceEffects(Ability abilityToModify, Game game) {
        // add effects from splice card to spell ability on activate/cast

        List<SpliceCardEffect> spliceEffects = getApplicableSpliceCardEffects(game, abilityToModify.getControllerId());
        // get the applyable splice abilities
        List<Ability> spliceAbilities = new ArrayList<>();
        for (SpliceCardEffect effect : spliceEffects) {
            Set<Ability> abilities = spliceCardEffects.getAbility(effect.getId());
            for (Ability ability : abilities) {
                if (effect.applies(abilityToModify, ability, game)) {
                    spliceAbilities.add(ability);
                }
            }
        }
        // check if player wants to use splice

        if (!spliceAbilities.isEmpty()) {
            Player controller = game.getPlayer(abilityToModify.getControllerId());
            if (controller.chooseUse(Outcome.Benefit, "Splice a card?", abilityToModify, game)) {
                Cards cardsToReveal = new CardsImpl();
                do {
                    FilterCard filter = new FilterCard("a card to splice");
                    List<Predicate<MageObject>> idPredicates = new ArrayList<>();
                    for (Ability ability : spliceAbilities) {
                        idPredicates.add(new CardIdPredicate((ability.getSourceId())));
                    }
                    filter.add(Predicates.or(idPredicates));
                    TargetCardInHand target = new TargetCardInHand(filter);
                    controller.chooseTarget(Outcome.Benefit, target, abilityToModify, game);
                    UUID cardId = target.getFirstTarget();
                    if (cardId != null) {
                        Ability selectedAbility = null;
                        for (Ability ability : spliceAbilities) {
                            if (ability.getSourceId().equals(cardId)) {
                                selectedAbility = ability;
                                break;
                            }
                        }
                        if (selectedAbility != null) {
                            SpliceCardEffect spliceEffect = (SpliceCardEffect) selectedAbility.getEffects().get(0);
                            spliceEffect.apply(game, selectedAbility, abilityToModify);
                            cardsToReveal.add(game.getCard(cardId));
                            spliceAbilities.remove(selectedAbility);
                        }
                    }
                } while (!spliceAbilities.isEmpty() && controller.chooseUse(Outcome.Benefit, "Splice another card?", abilityToModify, game));
                controller.revealCards("Spliced cards", cardsToReveal, game);
            }
        }
    }

    /**
     * Checks if an event won't happen because of a rule modifying effect
     *
     * @param event
     * @param targetAbility ability the event is attached to. can be null.
     * @param game
     * @param silentMode    true if the event does not really happen but it's
     *                      checked if the event would be replaced
     * @return
     */
    public boolean preventedByRuleModification(GameEvent event, Ability targetAbility, Game game, boolean silentMode) {
        for (ContinuousRuleModifyingEffect effect : continuousRuleModifyingEffects) {
            if (!effect.checksEventType(event, game)) {
                continue;
            }
            for (Ability sourceAbility : continuousRuleModifyingEffects.getAbility(effect.getId())) {
                if (!(sourceAbility instanceof StaticAbility) || sourceAbility.isInUseableZone(game, null, event)) {
                    if (checkAbilityStillExists(sourceAbility, effect, event, game)) {
                        if (effect.getDuration() != Duration.OneUse || !effect.isUsed()) {
                            effect.setValue("targetAbility", targetAbility);
                            if (effect.applies(event, sourceAbility, game)) {
                                if (!game.inCheckPlayableState() && !silentMode) {
                                    MageObject sourceObject = sourceAbility.getSourceObject(game);
                                    String message = effect.getInfoMessage(sourceAbility, event, game);
                                    if (sourceObject != null) {
                                        message = sourceObject.getIdName() + ": " + message;
                                    }
                                    if (message != null && !message.isEmpty()) {
                                        if (effect.sendMessageToUser()) {
                                            Player player = game.getPlayer(event.getPlayerId());
                                            if (player != null && !game.isSimulation()) {
                                                game.informPlayer(player, message);
                                            }
                                        }
                                        if (effect.sendMessageToGameLog() && !game.isSimulation()) {
                                            game.informPlayers(message);
                                        }
                                    }
                                }
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean replaceEvent(GameEvent event, Game game) {
        boolean caught = false;
        Map<UUID, Set<UUID>> consumed = new HashMap<>();
        do {
            Map<ReplacementEffect, Set<Ability>> rEffects = getApplicableReplacementEffects(event, game);
            // Remove all consumed effects (ability dependant)
            for (Iterator<ReplacementEffect> it1 = rEffects.keySet().iterator(); it1.hasNext(); ) {
                ReplacementEffect entry = it1.next();
                if (consumed.containsKey(entry.getId()) /*&& !(entry instanceof CommanderReplacementEffect) */) { // 903.9.
                    Set<UUID> consumedAbilitiesIds = consumed.get(entry.getId());
                    if (rEffects.get(entry) == null || consumedAbilitiesIds.size() == rEffects.get(entry).size()) {
                        it1.remove();
                    } else {
                        Iterator it = rEffects.get(entry).iterator();
                        while (it.hasNext()) {
                            Ability ability = (Ability) it.next();
                            if (consumedAbilitiesIds.contains(ability.getId())) {
                                it.remove();
                            }
                        }
                    }
                }
            }
            // no effects left, quit
            if (rEffects.isEmpty()) {
                break;
            }
            int index;
            boolean onlyOne = false;
            if (rEffects.size() == 1) {
                ReplacementEffect effect = rEffects.keySet().iterator().next();
                Set<Ability> abilities;
                if (effect.getEffectType() == EffectType.REPLACEMENT) {
                    abilities = replacementEffects.getAbility(effect.getId());
                } else {
                    abilities = preventionEffects.getAbility(effect.getId());
                }
                if (abilities == null || abilities.size() == 1) {
                    onlyOne = true;
                }
            }
            if (onlyOne) {
                index = 0;
            } else {
                //20100716 - 616.1c
                Player player = game.getPlayer(event.getPlayerId());
                Map<String, String> effectsMap = new LinkedHashMap<>();
                Map<String, MageObject> objectsMap = new LinkedHashMap<>();
                prepareReplacementEffectMaps(rEffects, game, effectsMap, objectsMap);
                index = player.chooseReplacementEffect(effectsMap, objectsMap, game);
            }
            // get the selected effect
            int checked = 0;
            ReplacementEffect rEffect = null;
            Ability rAbility = null;
            for (Map.Entry<ReplacementEffect, Set<Ability>> entry : rEffects.entrySet()) {
                if (entry.getValue() == null) {
                    if (checked == index) {
                        rEffect = entry.getKey();
                        break;
                    } else {
                        checked++;
                    }
                } else {
                    Set<Ability> abilities = entry.getValue();
                    int size = abilities.size();
                    if (index > (checked + size - 1)) {
                        checked += size;
                    } else {
                        rEffect = entry.getKey();
                        Iterator it = abilities.iterator();
                        while (it.hasNext() && rAbility == null) {
                            if (checked == index) {
                                rAbility = (Ability) it.next();
                            } else {
                                it.next();
                                checked++;
                            }
                        }
                        break;
                    }
                }
            }

            if (rEffect != null) {
                event.getAppliedEffects().add(rEffect.getId());
                caught = rEffect.replaceEvent(event, rAbility, game);
                if (Duration.OneUse.equals(rEffect.getDuration())) {
                    rEffect.discard();
                }
            }
            if (caught) { // Event was completely replaced -> stop applying effects to it
                break;
            }

            // add the applied effect to the consumed effects
            if (rEffect != null) {
                if (consumed.containsKey(rEffect.getId())) {
                    Set<UUID> set = consumed.get(rEffect.getId());
                    if (rAbility != null) {
                        set.add(rAbility.getId());

                    }
                } else {
                    Set<UUID> set = new HashSet<>();
                    if (rAbility != null) { // in case of AuraReplacementEffect or PlaneswalkerReplacementEffect there is no Ability
                        set.add(rAbility.getId());
                    }
                    consumed.put(rEffect.getId(), set);
                }
            }
            // Must be called here for some effects to be able to work correctly
            // For example: Vesuva copying a Dark Depth (VesuvaTest:testDarkDepth)
            // This call should be removed if possible as replacement effects of EntersTheBattlefield events
            // do no longer work correctly because the entering permanents are not yet on the battlefield (before they were).
            // game.applyEffects();
        } while (true);
        return caught;
    }

    //20091005 - 613
    public synchronized void apply(Game game) {
        removeInactiveEffects(game);
        this.appliedEffects.clear();
        List<ContinuousEffect> activeLayerEffects = getLayeredEffects(game, true); // main call

        applyLayer(activeLayerEffects, Layer.CopyEffects_1, SubLayer.CopyEffects_1a, game);
        activeLayerEffects = getLayeredEffects(game);
        applyLayer(activeLayerEffects, Layer.CopyEffects_1, SubLayer.FaceDownEffects_1b, game);
        activeLayerEffects = getLayeredEffects(game);

        List<ContinuousEffect> layer = filterLayeredEffects(activeLayerEffects, Layer.ControlChangingEffects_2, SubLayer.NA);
        // apply control changing effects multiple times if it's needed
        // for cases when control over permanents with change control abilities is changed
        // e.g. Mind Control is controlled by Steal Enchantment
        while (true) {
            for (ContinuousEffect effect : layer) {
                Set<Ability> abilities = layeredEffects.getAbility(effect.getId());
                for (Ability ability : abilities) {
                    effect.apply(Layer.ControlChangingEffects_2, SubLayer.NA, ability, game);
                }
            }
            // if control over all permanent has not changed, we can no longer reapply control changing effects
            if (!game.getBattlefield().fireControlChangeEvents(game)) {
                break;
            }
            // reset control before reapplying control changing effects
            game.getBattlefield().resetPermanentsControl();
        }

        applyLayer(activeLayerEffects, Layer.TextChangingEffects_3, SubLayer.NA, game);
        activeLayerEffects = getLayeredEffects(game);
        applyLayer(activeLayerEffects, Layer.TypeChangingEffects_4, SubLayer.NA, game);
        activeLayerEffects = getLayeredEffects(game);
        applyLayer(activeLayerEffects, Layer.ColorChangingEffects_5, SubLayer.NA, game);
        activeLayerEffects = getLayeredEffects(game);
        applyStatus.apply(Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, null, game);
        applyLayer(activeLayerEffects, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, game);
        activeLayerEffects = getLayeredEffects(game);
        applyLayer(activeLayerEffects, Layer.PTChangingEffects_7, SubLayer.CharacteristicDefining_7a, game);
        activeLayerEffects = getLayeredEffects(game);
        applyLayer(activeLayerEffects, Layer.PTChangingEffects_7, SubLayer.SetPT_7b, game);
        activeLayerEffects = getLayeredEffects(game);
        applyLayer(activeLayerEffects, Layer.PTChangingEffects_7, SubLayer.ModifyPT_7c, game);
        activeLayerEffects = getLayeredEffects(game);
        applyStatus.apply(Layer.PTChangingEffects_7, SubLayer.Counters_7d, null, game);
        applyLayer(activeLayerEffects, Layer.PTChangingEffects_7, SubLayer.Counters_7d, game);
        activeLayerEffects = getLayeredEffects(game);
        applyLayer(activeLayerEffects, Layer.PTChangingEffects_7, SubLayer.SwitchPT_e, game);
        activeLayerEffects = getLayeredEffects(game);
        applyLayer(activeLayerEffects, Layer.PlayerEffects, SubLayer.NA, game);
        activeLayerEffects = getLayeredEffects(game);
        applyLayer(activeLayerEffects, Layer.RulesEffects, SubLayer.NA, game);
    }

    /**
     * Applies all effects for the current layer/sublayer in the correct order.
     */
    private void applyLayer(List<ContinuousEffect> activeLayerEffects, Layer currentLayer, SubLayer subLayer, Game game) {
        List<ContinuousEffect> filteredLayeredEffects = filterLayeredEffects(activeLayerEffects, currentLayer, subLayer);
        Set<UUID> appliedLayerEffects = new HashSet<>();
        DefaultDirectedGraph<ContinuousEffect, DefaultEdge> dependencyGraph = layerDependencies.get(currentLayer);
        if (dependencyGraph == null && shouldCheckLayerForDependencies(currentLayer, subLayer)) {
            dependencyGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        }
        // workaround for copy effects adding abilities with more copy effects
        // store the initial size and ignore after processing them
        int initialSize = filteredLayeredEffects.size();
        while (!filteredLayeredEffects.isEmpty() && (currentLayer != Layer.CopyEffects_1 || appliedLayerEffects.size() < initialSize)) {
            ContinuousEffect effect = filteredLayeredEffects.get(0);
            if (filteredLayeredEffects.size() > 1) {
                effect = getNextEffectToApply(filteredLayeredEffects, currentLayer, subLayer, game, dependencyGraph);
            }

            applyContinuousEffect(effect, currentLayer, subLayer, game);
            appliedLayerEffects.add(effect.getId());
            this.appliedEffects.add(effect.getId());
            filteredLayeredEffects = filterLayeredEffects(getLayeredEffects(game), currentLayer, subLayer)
                    .stream()
                    .filter(continuousEffect -> !appliedLayerEffects.contains(continuousEffect.getId()))
            .collect(Collectors.toList());
        }
        layerDependencies.put(currentLayer, dependencyGraph);
    }

    private boolean shouldCheckLayerForDependencies(Layer currentLayer, SubLayer subLayer) {
        boolean check = false;
        switch (currentLayer) {
            case CopyEffects_1:
            case ControlChangingEffects_2:
            case TextChangingEffects_3:
            case TypeChangingEffects_4:
            case AbilityAddingRemovingEffects_6:
                check = true;
                break;
            case PTChangingEffects_7:
                if (subLayer == SubLayer.CharacteristicDefining_7a) {
                    check = true;
                }
                break;
        }
        return check;
    }

    /**
     * Test continuos effects for the layer and sublayer looking for any dependencies. The returned effect is correct
     * dependencies/timestamp
     */
    private ContinuousEffect getNextEffectToApply(List<ContinuousEffect> filteredLayeredEffects, Layer currentLayer, SubLayer subLayer, Game game,
                                                  DefaultDirectedGraph<ContinuousEffect, DefaultEdge> dependencyGraph) {
        if (game.isSimulation() || dependencyGraph == null) {
            // skip dependency calculation for AI simulations
            return filteredLayeredEffects.stream()
                    .min(Comparator.comparingInt(effect -> (int) effect.getOrder()))
                    .orElse(null);
        }
        // if any of the layer effects are not in dependency graph, recalculate
        if (filteredLayeredEffects.stream().anyMatch(effect -> !dependencyGraph.containsVertex(effect))) {
            calculateDependencies(filteredLayeredEffects, currentLayer, subLayer, game, dependencyGraph);
            // 613.8b. If several dependent effects form a dependency loop, then this rule is ignored
            // remove cycles
            List<List<ContinuousEffect>> cycles = new SzwarcfiterLauerSimpleCycles<>(dependencyGraph).findSimpleCycles();
            for (List<ContinuousEffect> cycle : cycles) {
                for (int i = 0; i < cycle.size() - 1; i++) {
                    dependencyGraph.removeEdge(cycle.get(i), cycle.get(i + 1));
                }
                dependencyGraph.removeEdge(cycle.get(cycle.size() - 1), cycle.get(0));
            }
        }

        // remove applied effects
        DefaultDirectedGraph<ContinuousEffect, DefaultEdge> dependencyGraphCopy = (DefaultDirectedGraph<ContinuousEffect, DefaultEdge>) dependencyGraph.clone();
        dependencyGraphCopy.removeAllVertices(dependencyGraph.vertexSet().stream()
                .filter(effect -> !filteredLayeredEffects.contains(effect))
                .collect(Collectors.toList()));
        // earliest independent effect should be the correct one to apply
        List<ContinuousEffect> orderedEffects = dependencyGraphCopy.vertexSet().stream()
                .sorted(Comparator.comparingInt(dependencyGraphCopy::outDegreeOf)
                        .thenComparingInt(effect -> (int) effect.getOrder()))
                .collect(Collectors.toList());
        return orderedEffects.stream().findFirst().orElse(null);
    }

    private void calculateDependencies(List<ContinuousEffect> filteredLayeredEffects, Layer currentLayer, SubLayer subLayer, Game game, DefaultDirectedGraph<ContinuousEffect, DefaultEdge> dependencyGraph) {
        Game gameSim = game.createSimulationForAI();
        EffectStateTracker stateTracker = new EffectStateTracker();

        for (ContinuousEffect effect : filteredLayeredEffects) {
            dependencyGraph.addVertex(effect);

            // Calculate initial state without any other effects
            int resultBefore = 0;
            List<MageItem> affectedBefore = new ArrayList<>();
            for (Ability ability : getLayeredEffectAbilities(effect)) {
                resultBefore = applySimulatedEffectWithTracking(currentLayer, subLayer, effect, ability, gameSim, affectedBefore, resultBefore, stateTracker);
            }

            // Revert changes from applying the effect
            stateTracker.revertChanges(gameSim);

            for (ContinuousEffect otherEffect : filteredLayeredEffects) {
                if (otherEffect == effect) {
                    continue;
                }

                // Apply the other effect first
                for (Ability ability : getLayeredEffectAbilities(otherEffect)) {
                    List<MageItem> otherAffectedObjects = new ArrayList<>();
                    applySimulatedEffectWithTracking(currentLayer, subLayer, otherEffect, ability, gameSim, otherAffectedObjects, 0, stateTracker);
                }

                boolean dependency = false;
                //613.8a An effect is said to “depend on” another if (a) it’s applied in the same layer (and, if applicable, sublayer) as the other effect;
                // (b) applying the other would change the text or the existence of the first effect
                List<MageItem> affectedAfter = new ArrayList<>();
                int resultAfter = 0;
                for (Ability ability : getLayeredEffectAbilities(effect)) {
                    dependency |= !isAbilityStillExists(gameSim, ability, effect);
                    if (!dependency) {
                        resultAfter = applySimulatedEffectWithTracking(currentLayer, subLayer, effect, ability, gameSim, affectedAfter, resultAfter, stateTracker);
                    }
                }
                // what it applies to,
                if (!dependency) {
                    Set<UUID> uuidsBefore = affectedBefore.stream()
                        .map(MageItem::getId)
                        .collect(Collectors.toSet());
                    Set<UUID> uuidsAfter = affectedAfter.stream()
                        .map(MageItem::getId)
                        .collect(Collectors.toSet());

                    dependency = !uuidsBefore.equals(uuidsAfter);
                }
                // or what it does to any of the things it applies to;
                if (!dependency) {
                    dependency = resultAfter != resultBefore;
                }
                // Otherwise, the effect is considered to be independent of the other effect.
                if (dependency) {
                    dependencyGraph.addVertex(otherEffect);
                    dependencyGraph.addEdge(effect, otherEffect);
                }

                // Revert all changes before testing next effect
                stateTracker.revertChanges(gameSim);
            }
        }
    }

    /**
     * Applies a continuous effect with ability copy for simulation
     */
    private int applySimulatedEffectWithTracking(Layer currentLayer, SubLayer subLayer, ContinuousEffect effect, Ability ability,
                                                Game gameSim, List<MageItem> affectedObjects, int result, EffectStateTracker stateTracker) {
        Ability abilityCopy = ability.copy();
        if (effect.queryAffectedObjects(currentLayer, abilityCopy, gameSim, affectedObjects)) {
            // Record state of objects that will be modified
            for (MageItem item : affectedObjects) {
                stateTracker.recordMageItemState(item, gameSim);
            }

            effect.applyToObjects(currentLayer, subLayer, abilityCopy, gameSim, affectedObjects);
            result += effect.calculateResult(gameSim, ability, affectedObjects);
        }
        return result;
    }

    /**
     * Applies one continuous effect for the current layer/sublayer
     */
    private void applyContinuousEffect(ContinuousEffect effect, Layer currentLayer, SubLayer subLayer, Game game) {
        Set<Ability> abilities = layeredEffects.getAbility(effect.getId());
        for (Ability ability : abilities) {
            if (subLayer == SubLayer.CharacteristicDefining_7a && !abilityActive(ability, game)) {
                continue;
            }
            if (isAbilityStillExists(game, ability, effect) || currentLayer == Layer.CopyEffects_1 || currentLayer == Layer.ControlChangingEffects_2) {
                effect.apply(currentLayer, subLayer, ability, game);
            }
        }
    }

    private boolean isAbilityStillExists(final Game game, final Ability ability, ContinuousEffect effect) {
        switch (effect.getDuration()) { // effects with fixed duration don't need an object with the source ability (e.g. a silence cast with isochronic Scepter has no more a card object
            case EndOfCombat:
            case EndOfGame:
            case EndOfStep:
            case EndOfTurn:
            case OneUse:
            case Custom:  // custom duration means the effect ends itself if needed
                return true;
        }
        Card card = game.getPermanentOrLKIBattlefield(ability.getSourceId());
        if (card == null) {
            // if no permanent check the card/card state
            card = game.getCard(ability.getSourceId());
            return card == null || card.hasAbility(ability, game);
        }
        return effect instanceof BecomesFaceDownCreatureEffect || this.appliedEffects.contains(effect.getId())
                || card.hasAbility(ability, game) || ability instanceof DelayedTriggeredAbility;
    }

    private boolean abilityActive(Ability ability, Game game) {
        MageObject object = game.getObject(ability.getSourceId());
        return object != null && object.hasAbility(ability, game);
    }

    public Set<Ability> getLayeredEffectAbilities(ContinuousEffect effect) {
        return layeredEffects.getAbility(effect.getId());
    }

    /**
     * Adds a continuous ability with a reference to a sourceId. It's used for
     * effects that cease to exist again So this effects were removed again
     * before each applyEffecs
     *
     * @param effect
     * @param sourceId
     * @param source
     */
    public synchronized void addEffect(ContinuousEffect effect, UUID sourceId, Ability source) {
        if (!(source instanceof MageSingleton)) { // because MageSingletons may never be removed by removing the temporary effecs they are not added to the temporaryEffects to prevent this
            effect.setTemporary(true);
        }
        addEffect(effect, source);
    }

    public synchronized void addEffect(ContinuousEffect effect, Ability source) {
        if (effect == null || source == null) {
            // addEffect(effect, source) need a non-null source
            throw new IllegalArgumentException("Wrong code usage. Effect and source can't be null here: "
                    + source + "; " + effect);
        }

        switch (effect.getEffectType()) {
            case REPLACEMENT:
            case REDIRECTION:
                replacementEffects.addEffect((ReplacementEffect) effect, source);
                break;
            case PREVENTION:
                preventionEffects.addEffect((PreventionEffect) effect, source);
                break;
            case RESTRICTION:
                restrictionEffects.addEffect((RestrictionEffect) effect, source);
                break;
            case RESTRICTION_UNTAP_NOT_MORE_THAN:
                restrictionUntapNotMoreThanEffects.addEffect((RestrictionUntapNotMoreThanEffect) effect, source);
                break;
            case REQUIREMENT:
                requirementEffects.addEffect((RequirementEffect) effect, source);
                break;
            case ASTHOUGH:
                AsThoughEffect newAsThoughEffect = (AsThoughEffect) effect;
                asThoughEffectsMap.computeIfAbsent(newAsThoughEffect.getAsThoughEffectType(), x -> {
                    ContinuousEffectsList<AsThoughEffect> list = new ContinuousEffectsList<>();
                    allEffectsLists.add(list);
                    return list;
                }).addEffect(newAsThoughEffect, source);
                break;
            case COSTMODIFICATION:
                costModificationEffects.addEffect((CostModificationEffect) effect, source);
                break;
            case SPLICE:
                spliceCardEffects.addEffect((SpliceCardEffect) effect, source);
                break;
            case CONTINUOUS_RULE_MODIFICATION:
                continuousRuleModifyingEffects.addEffect((ContinuousRuleModifyingEffect) effect, source);
                break;
            case CONTINUOUS:
            case ONESHOT:
                layeredEffects.addEffect(effect, source);
                break;
            default:
                throw new IllegalArgumentException("Unknown effect type: " + effect.getEffectType());
        }
    }

    public synchronized void setController(UUID cardId, UUID controllerId) {
        for (ContinuousEffectsList effectsList : allEffectsLists) {
            setControllerForEffect(effectsList, cardId, controllerId);
        }
    }

    private void setControllerForEffect(ContinuousEffectsList<?> effects, UUID sourceId, UUID controllerId) {
        for (ContinuousEffect effect : effects) {
            if (!effect.getDuration().isFixedController()) {
                Set<Ability> abilities = effects.getAbility(effect.getId());
                for (Ability ability : abilities) {
                    if (ability.getSourceId() != null) {
                        if (ability.getSourceId().equals(sourceId)) {
                            ability.setControllerId(controllerId);
                        }
                    } else if (ability.getZone() != Zone.COMMAND) {
                        logger.fatal("Continuous effect for ability with no sourceId Ability: " + ability);
                    }
                }
            }
        }
    }

    public synchronized void clear() {
        for (ContinuousEffectsList effectsList : allEffectsLists) {
            effectsList.clear();
        }
    }

    public synchronized void removeAllTemporaryEffects() {
        for (ContinuousEffectsList effectsList : allEffectsLists) {
            effectsList.removeTemporaryEffects();
        }
    }

    public void prepareReplacementEffectMaps(Map<ReplacementEffect, Set<Ability>> rEffects, Game game,
                                             Map<String, String> effectsMap, Map<String, MageObject> objectsMap) {
        // warning, autoSelectReplacementEffects uses [object id] in texts as different settings,
        // so if you change keys or texts logic then don't forget to change auto-choose too
        if (!(effectsMap instanceof LinkedHashMap) || !(objectsMap instanceof LinkedHashMap)) {
            throw new IllegalArgumentException("Wrong code usage: must use LinkedHashMap only");
        }
        effectsMap.clear();
        objectsMap.clear();
        for (Map.Entry<ReplacementEffect, Set<Ability>> entry : rEffects.entrySet()) {
            if (entry.getValue() != null) {
                for (Ability ability : entry.getValue()) {
                    MageObject object = game.getObject(ability.getSourceId());
                    String key = ability.getId().toString() + '_' + entry.getKey().getId().toString();
                    if (object != null) {
                        effectsMap.put(key, object.getIdName() + ": " + ability.getRule(object.getName()));
                        objectsMap.put(key, object);
                    } else {
                        effectsMap.put(key, entry.getKey().getText(ability.getModes().getMode()));
                        objectsMap.put(key, null);
                    }
                }
            } else {
                if (!(entry.getKey() instanceof AuraReplacementEffect)) {
                    logger.error("Replacement effect without ability: " + entry.getKey().toString());
                }
            }
        }
    }

    public boolean existRequirementEffects() {
        return !requirementEffects.isEmpty();
    }

    public UUID getControllerOfSourceId(UUID sourceId) {
        UUID controllerFound = null;
        for (PreventionEffect effect : preventionEffects) {
            Set<Ability> abilities = preventionEffects.getAbility(effect.getId());
            for (Ability ability : abilities) {
                if (ability.getSourceId().equals(sourceId)) {
                    if (controllerFound == null || controllerFound.equals(ability.getControllerId())) {
                        controllerFound = ability.getControllerId();
                    } else {
                        // not unique controller - No solution yet
                        return null;
                    }
                }
            }
        }
        for (ReplacementEffect effect : replacementEffects) {
            Set<Ability> abilities = replacementEffects.getAbility(effect.getId());
            for (Ability ability : abilities) {
                if (ability.getSourceId() != null) {
                    if (ability.getSourceId().equals(sourceId)) {
                        if (controllerFound == null || controllerFound.equals(ability.getControllerId())) {
                            controllerFound = ability.getControllerId();
                        } else {
                            // not unique controller - No solution yet
                            return null;
                        }
                    }
                } else {
                    if (!(effect instanceof CommanderReplacementEffect)) {
                        logger.warn("Ability without sourceId:" + ability.getRule());
                    }
                }
            }
        }
        return controllerFound;
    }

    public int getTotalEffectsCount() {
        return allEffectsLists.stream().mapToInt(ContinuousEffectsList::size).sum();
    }

    @Override
    public String toString() {
        return "Effects: " + getTotalEffectsCount();
    }
}
