package mage.abilities.effects.common.cast;

import mage.ApprovingObject;
import mage.MageItem;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.ActivatedAbility;
import mage.abilities.PlayLandAbility;
import mage.abilities.SpellAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.Costs;
import mage.abilities.costs.CostsImpl;
import mage.abilities.costs.mana.ManaCost;
import mage.abilities.costs.mana.ManaCosts;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ChooseACardNameEffect;
import mage.cards.Card;
import mage.cards.CardWithParts;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.cards.repository.CardCriteria;
import mage.cards.repository.CardInfo;
import mage.cards.repository.CardRepository;
import mage.constants.Outcome;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.FilterCard;
import mage.filter.FilterTyped;
import mage.game.Game;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.targetpointer.FixedTarget;
import mage.target.targetpointer.RememberedTargets;
import mage.util.CardUtil;
import mage.util.ObjectQuery;
import mage.util.RandomUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * One-shot effect that lets a player cast spells (and optionally play lands)
 * matching a given filter from a given set of zones. Supports multiple casts,
 * alternate costs, optional casting, card copying, mana value limits, and
 * delegation of who casts and who controls the resulting spells.
 * <p>
 * Cards to cast are resolved through one of three mechanisms:
 * <ol>
 *   <li><b>Filter</b> — queries cards in specified zones matching a
 *       {@link FilterTyped}.</li>
 *   <li><b>Card criteria</b> — randomly selects cards from the entire
 *       card repository using a {@link CardCriteria} (used only in
 *       non-simulation games).</li>
 *   <li><b>Chosen name</b> — picks the card whose name was previously
 *       stored by {@link ChooseACardNameEffect}.</li>
 * </ol>
 * <p>
 * Configuration is entirely fluent via setter methods that return
 * {@code this}. Typical usage:
 * <pre>{@code
 * new PlayEffect(filter).setOptional(true).setRememberPlayed(true);
 * }</pre>
 *
 * @author jmlundeen
 */
public class PlayEffect extends OneShotEffect {

    /** Produces alternative non-mana costs for a card being cast. */
    @FunctionalInterface
    public interface AltCostProducer {
        Costs<Cost> apply(Ability source, Game game, Card card);
    }

    /** Produces alternative mana costs for a card being cast. */
    @FunctionalInterface
    public interface AltManaCostProducer {
        ManaCosts<ManaCost> apply(Ability source, Game game, Card card);
    }

    /** Filter that cards (or card components) must satisfy. */
    FilterTyped filter;
    /** Zones to search when resolving cards via {@link #filter}. */
    Set<Zone> zones;
    /** Whether to allow playing lands in addition to casting spells. */
    boolean playLand;
    /** Maximum number of spells/lands to cast. */
    int amount;
    /** Whether the player must pay mana costs (false = free cast). */
    boolean payCosts;
    /** Whether the player may decline to cast each card. */
    boolean optional;
    /** If true, remove cast cards from the remembered-objects list. */
    boolean forgetPlayed;
    /** If true, add cast cards to the remembered-objects list. */
    boolean rememberPlayed;
    /** If true, resolve the card to cast from a previously chosen name. */
    boolean fromChosenName;
    /** If true, cast a copy of the card instead of the card itself. */
    boolean copyCard;
    /**
     * If true, the same physical card can be cast multiple times
     * (used together with {@link #copyCard}).
     */
    boolean allowRepeats;
    /** Maximum total mana value across all spells cast in one batch. */
    int manaValueLimit = Integer.MAX_VALUE;
    /** Producer for alternative non-mana costs. */
    AltCostProducer altCostProducer;
    /** Producer for alternative mana costs. */
    AltManaCostProducer altManaCostProducer;
    /** Who performs the cast action. */
    TargetController castingPlayer = TargetController.YOU;
    /** Who controls the casting player during spell cast. */
    TargetController controllingPlayer;
    /** Repository query used when no filter is set. */
    CardCriteria cardCriteria;
    /** Effects to apply after the card is successfully cast. */
    List<Effect> additionalEffects;
    /** Cached result of {@link CardRepository#findCards} for simulation. */
    transient List<CardInfo> cardQueryCache;
    /** Cards temporarily flipped face-up for casting, to be restored afterward. */
    transient Set<UUID> temporaryFaceUpCards;

    /**
     * Creates a PlayEffect that casts one card matching the given filter
     * from any zone, paying mana costs.
     */
    public PlayEffect(FilterTyped filter) {
        this(filter, Set.of(Zone.ALL));
    }

    /**
     * Creates a PlayEffect that casts one card matching the filter from the
     * specified zones, paying mana costs.
     */
    public PlayEffect(FilterTyped filter, Set<Zone> zones) {
        this(filter, zones, false);
    }

    /**
     * Creates a PlayEffect that casts one card (or plays one land) from the
     * specified zones, paying mana costs.
     *
     * @param playLand if true, the player may also play lands
     */
    public PlayEffect(FilterTyped filter, Set<Zone> zones, boolean playLand) {
        this(filter, zones, playLand, 1);
    }

    /**
     * Creates a PlayEffect that casts up to {@code amount} cards from the
     * specified zones, paying mana costs.
     */
    public PlayEffect(FilterTyped filter, Set<Zone> zones, boolean playLand, int amount) {
        this(filter, zones, playLand, amount, false);
    }

    /**
     * Creates a fully-configured PlayEffect.
     *
     * @param payCosts if true the player pays mana costs; if false the
     *                 spell is cast without paying its mana cost
     */
    public PlayEffect(FilterTyped filter, Set<Zone> zones, boolean playLand, int amount, boolean payCosts) {
        super(payCosts ? Outcome.Benefit : Outcome.PlayForFree);
        this.filter = filter;
        this.zones = zones;
        this.playLand = playLand;
        this.amount = amount;
        this.payCosts = payCosts;
    }

    /**
     * If true, the player may choose not to cast each individual card.
     * A confirmation dialog is shown before each cast.
     */
    public PlayEffect setOptional(boolean optional) {
        this.optional = optional;
        return this;
    }

    /**
     * If true, successfully cast cards are added to the effect's
     * {@link RememberedTargets} for use by subsequent linked effects.
     */
    public PlayEffect setRememberPlayed(boolean rememberPlayed) {
        this.rememberPlayed = rememberPlayed;
        return this;
    }

    /**
     * If true, cast cards are removed from the remembered-objects list
     * (e.g. to prevent linked effects from using ).
     */
    public PlayEffect setForgetPlayed(boolean forgetPlayed) {
        this.forgetPlayed = forgetPlayed;
        return this;
    }

    /**
     * If true, the card to cast is resolved from a previously chosen card
     * name stored by {@link ChooseACardNameEffect}.
     */
    public PlayEffect setFromChosenName(boolean fromChosenName) {
        this.fromChosenName = fromChosenName;
        return this;
    }

    /**
     * If true, a copy of the card is cast instead of the card itself
     * (the original stays in its zone).
     */
    public PlayEffect setCopyCard(boolean copyCard) {
        this.copyCard = copyCard;
        return this;
    }

    /**
     * If true, the same card can be selected and cast again in
     * subsequent iterations of a multi-cast effect. This will copy
     * cards.
     */
    public PlayEffect setAllowRepeats(boolean allowRepeats) {
        this.allowRepeats = allowRepeats;
        return this;
    }

    /**
     * Limits the total mana value of all spells cast in one batch to the
     * given value. Casting stops when the cumulative total reaches this limit.
     */
    public PlayEffect setManaValueLimit(int manaValueLimit) {
        this.manaValueLimit = manaValueLimit;
        return this;
    }

    /**
     * Sets a single alternative cost that replaces the card's normal costs.
     * Accepts both mana and non-mana costs.
     */
    public PlayEffect setAltCost(Cost cost) {
        if (cost instanceof ManaCost manaCost) {
            this.altManaCostProducer = (source, game, card) -> {
                ManaCosts<ManaCost> manaCosts = new ManaCostsImpl<>();
                manaCosts.add(manaCost);
                return manaCosts;
            };
        } else {
            this.altCostProducer = (source, game, card) -> {
                Costs<Cost> costs = new CostsImpl<>();
                costs.add(cost);
                return costs;
            };
        }
        return this;
    }

    /**
     * Sets alternative costs. Mana costs and non-mana costs
     * are collected separately and both are applied when casting.
     */
    public PlayEffect setAltCost(Cost... costs) {
        ManaCostsImpl<ManaCost> manaCosts = new ManaCostsImpl<>();
        CostsImpl<Cost> otherCosts = new CostsImpl<>();
        for (Cost cost : costs) {
            if (cost instanceof ManaCost manaCost) {
                manaCosts.add(manaCost);
            } else {
                otherCosts.add(cost);
            }
        }
        if (!manaCosts.isEmpty()) {
            this.altManaCostProducer = (source, game, card) -> manaCosts.copy();
        }
        if (!otherCosts.isEmpty()) {
            this.altCostProducer = (source, game, card) -> otherCosts.copy();
        }
        return this;
    }

    /**
     * Sets a functional producer for alternative non-mana costs. The producer
     * receives the source ability, game state, and the card being cast,
     * allowing context-dependent cost generation.
     */
    public PlayEffect setAltCostProducer(AltCostProducer altCostProducer) {
        this.altCostProducer = altCostProducer;
        return this;
    }

    /**
     * Sets a functional producer for alternative mana costs. The producer
     * receives the source ability, game state, and the card being cast,
     * allowing context-dependent mana cost generation.
     */
    public PlayEffect setAltManaCostProducer(AltManaCostProducer altManaCostProducer) {
        this.altManaCostProducer = altManaCostProducer;
        return this;
    }

    /**
     * Sets who performs the cast action. Options:
     * <ul>
     *   <li>{@link TargetController#YOU} — the effect's controller</li>
     *   <li>{@link TargetController#SOURCE_TARGETS} — the source's first target</li>
     *   <li>{@link TargetController#OWNER} — the card's owner</li>
     * </ul>
     */
    public PlayEffect setCastingPlayer(TargetController castingPlayer) {
        this.castingPlayer = castingPlayer;
        return this;
    }

    /**
     * Sets who controls the casting player while casting a spell. Same
     * options as {@link #setCastingPlayer}.
     */
    public PlayEffect setControllingPlayer(TargetController controllingPlayer) {
        this.controllingPlayer = controllingPlayer;
        return this;
    }

    /**
     * Sets a {@link CardCriteria} used to look up cards from the repository
     * when no filter-based zone search is used.
     */
    public PlayEffect setCardCriteria(CardCriteria cardCriteria) {
        this.cardCriteria = cardCriteria;
        return this;
    }

    /**
     * Sets a list of effects to apply to each card after a successful cast.
     */
    public PlayEffect setAdditionalEffects(List<Effect> additionalEffects) {
        this.additionalEffects = additionalEffects;
        return this;
    }

    /**
     * Adds a single effect to the list of effects to apply to each card after a successful cast.
     */
    public PlayEffect addAdditionalEffect(Effect effect) {
        if (this.additionalEffects == null) {
            this.additionalEffects = new ArrayList<>();
        }
        this.additionalEffects.add(effect);
        return this;
    }

    protected PlayEffect(final PlayEffect effect) {
        super(effect);
        this.filter = effect.filter.copy();
        this.zones = effect.zones == null ? null : new HashSet<>(effect.zones);
        this.playLand = effect.playLand;
        this.amount = effect.amount;
        this.payCosts = effect.payCosts;
        this.optional = effect.optional;
        this.forgetPlayed = effect.forgetPlayed;
        this.rememberPlayed = effect.rememberPlayed;
        this.fromChosenName = effect.fromChosenName;
        this.copyCard = effect.copyCard;
        this.allowRepeats = effect.allowRepeats;
        this.manaValueLimit = effect.manaValueLimit;
        this.altCostProducer = effect.altCostProducer;
        this.altManaCostProducer = effect.altManaCostProducer;
        this.castingPlayer = effect.castingPlayer;
        this.controllingPlayer = effect.controllingPlayer;
        this.cardCriteria = effect.cardCriteria;
        this.additionalEffects = effect.additionalEffects == null ? null : new ArrayList<>(effect.additionalEffects);
    }

    @Override
    public void applyToObjects(Ability source, Game game, List<MageItem> affectedObjects) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return;
        }

        Cards cards = new CardsImpl();
        for (MageItem item : affectedObjects) {
            if (item instanceof Card card) {
                if (card.isFaceDown()) {
                    card.setFaceDown(false);
                    if (temporaryFaceUpCards == null) {
                        temporaryFaceUpCards = new HashSet<>();
                    }
                    temporaryFaceUpCards.add(card.getId());
                }
                cards.add(card);
            }
        }
        if (cards.isEmpty()) {
            return;
        }
        List<MageItem> rememberedObjects = new ArrayList<>();
        if (rememberPlayed || forgetPlayed) {
            rememberedObjects.addAll(affectedObjects);
        }
        castMultipleWithAttributes(controller, source, game, cards, filter, amount, rememberedObjects, playLand, payCosts);
        if (!rememberedObjects.isEmpty()) {
            RememberedTargets rememberedTargets = new RememberedTargets(rememberedObjects, game);
            source.getEffects().setTargetPointer(rememberedTargets);
        }
        if (temporaryFaceUpCards != null) {
            temporaryFaceUpCards.forEach(cardId -> {
                Card card = game.getCard(cardId);
                if (card != null) {
                    card.setFaceDown(true);
                }
            });
        }
    }

    @Override
    public boolean queryAffectedObjects(Ability source, Game game, List<MageItem> affectedObjects) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) return false;

        for (UUID target : getTargetPointer().getTargets(game, source)) {
            Card card = game.getCard(target);
            if (card != null) {
                affectedObjects.add(card);
            }
        }

        if (filter != null) {
            affectedObjects.removeIf(item -> !filter.match(item, controller.getId(), source, game));
            affectedObjects.addAll(ObjectQuery.queryCards(game, controller, source, zones, filter));
        } else if (cardCriteria != null && !game.isSimulation()) {
            if (cardQueryCache == null) {
                cardQueryCache = new ArrayList<>();
                cardQueryCache.addAll(CardRepository.instance.findCards(cardCriteria));
                if (cardQueryCache.size() == 1) {
                    affectedObjects.add(cardQueryCache.getFirst().createCard());
                } else {
                    for (int i = 0; i < amount; i++) {
                        CardInfo info = RandomUtil.randomFromCollection(cardQueryCache);
                        if (info != null) {
                            affectedObjects.add(info.createCard());
                        }
                    }
                }
            }
        } else if (fromChosenName) {
            String chosenName = (String) game.getState().getValue(source.getSourceId() + ChooseACardNameEffect.INFO_KEY);
            if (chosenName != null) {
                CardInfo cardInfo = CardRepository.instance.findCard(chosenName);
                if (cardInfo != null) {
                    affectedObjects.add(cardInfo.createCard());
                }
            }
        }

        if (manaValueLimit < Integer.MAX_VALUE) {
            affectedObjects.removeIf(item -> {
                if (item instanceof Card) {
                    return ((Card) item).getManaValue() > manaValueLimit;
                }
                return false;
            });
        }

        return !affectedObjects.isEmpty();
    }

    @Override
    public boolean apply(Game game, Ability source) {
        List<MageItem> affectedObjects = new ArrayList<>(source.getAffectedObjects());
        if (queryAffectedObjects(source, game, affectedObjects)) {
            applyToObjects(source, game, affectedObjects);
            return true;
        }
        return false;
    }

    @Override
    public OneShotEffect copy() {
        return new PlayEffect(this);
    }

    /**
     * Resolves the player who will perform the cast action, based on
     * {@link #castingPlayer}.
     */
    private Player getCastingPlayer(Card card, Ability source, Game game) {
        return switch (castingPlayer) {
            case YOU -> game.getPlayer(source.getControllerId());
            case SOURCE_TARGETS -> game.getPlayer(source.getFirstTarget());
            case OWNER -> game.getPlayer(card.getOwnerId());
            default -> null;
        };
    }

    /**
     * Resolves the player who will control the casting player,
     * based on {@link #controllingPlayer}.
     */
    private Player getControllingPlayer(Card card, Ability source, Game game) {
        return switch (controllingPlayer) {
            case YOU -> game.getPlayer(source.getControllerId());
            case SOURCE_TARGETS -> game.getPlayer(source.getFirstTarget());
            case OWNER -> game.getPlayer(card.getOwnerId());
            default -> null;
        };
    }

    /**
     * Casts up to {@code maxSpells} cards from the given collection.
     * Tracks cast count and cumulative mana value; stops when either
     * limit is reached or no castable cards remain. For human players
     * with optional casting, asks whether to continue between casts.
     */
    public void castMultipleWithAttributes(Player player, Ability source, Game game, Cards cards, FilterTyped filter, int maxSpells, List<MageItem> rememberedObjects, boolean playLand, boolean payCosts) {
        if (maxSpells == 1) {
            castSpellWithAttributes(player, source, game, cards, filter, rememberedObjects, playLand, payCosts);
            return;
        }

        int castCount = 0;
        int totalManaValue = 0;

        if (!allowRepeats) {
            cards.removeZone(Zone.STACK, game);
        }
        if (cardsHaveNoCastableParts(cards, filter, source, player, game, playLand)) {
            return;
        }

        while (player.canRespond() && castCount < maxSpells && totalManaValue < manaValueLimit) {
            Card cardCast = castSpellWithAttributes(player, source, game, cards, filter, rememberedObjects, playLand, payCosts);

            if (!allowRepeats) {
                cards.removeZone(Zone.STACK, game);
            }

            if (cards.isEmpty() || cardsHaveNoCastableParts(cards, filter, source, player, game, playLand)) {
                break;
            }

            if (cardCast != null) {
                castCount++;
                totalManaValue += cardCast.getManaValue();
                if (castCount >= maxSpells || totalManaValue >= manaValueLimit) {
                    break;
                }
                // remove cards whose mana value would now exceed total
                for (Card card : cards.getCards(game)) {
                    if (card.getManaValue() + totalManaValue > manaValueLimit) {
                        cards.remove(card);
                    }
                }
            } else if (player.isComputer()) {
                break;
            } else if (!player.chooseUse(getOutcome(payCosts), getContinueMessage(payCosts, playLand), source, game)) {
                break;
            }
        }
    }

    /**
     * Casts a single card chosen from the given collection. Handles
     * player choice when multiple cards are available, casts with
     * optional control-change, and updates the remembered-objects
     * list on success.
     *
     * @return the cast card, or null if the player declined or no card
     *         was available
     */
    public Card castSpellWithAttributes(Player player, Ability source, Game game, Cards cards, FilterTyped filter, List<MageItem> rememberedObjects, boolean playLand, boolean payCosts) {
        Map<UUID, List<Card>> cardMap = new HashMap<>();
        for (Card card : cards.getCards(game)) {
            List<Card> castableComponents = getCastableComponents(card, filter, source, player, game, playLand);
            if (!castableComponents.isEmpty()) {
                cardMap.put(card.getId(), castableComponents);
            } else {
                cards.remove(card);
            }
        }

        Card cardToCast;
        switch (cardMap.size()) {
            case 0:
                return null;
            case 1:
                cardToCast = cards.get(cardMap.keySet().stream().findFirst().orElse(null), game);
                break;
            default:
                Cards castableCards = new CardsImpl(cardMap.keySet());
                TargetCard target = new TargetCard(0, 1, Zone.ALL, new FilterCard("card to cast"));
                target.withNotTarget(true);
                player.choose(getOutcome(payCosts), castableCards, target, source, game);
                cardToCast = castableCards.get(target.getFirstTarget(), game);
        }
        if (cardToCast == null) {
            return null;
        }

        Player castingPlayer = getCastingPlayer(cardToCast, source, game);
        if (castingPlayer == null) {
            return null;
        }
        Player controllingPlayer = getControllingPlayer(cardToCast, source, game);

        boolean controlActive = false;
        if (controllingPlayer != null && !controllingPlayer.equals(castingPlayer)) {
            CardUtil.takeControlUnderPlayerStart(game, source, controllingPlayer, castingPlayer, true);
            controlActive = true;
        }

        List<Card> partsToCast = cardMap.get(cardToCast.getId());
        // re-check castable components with castingPlayer
        List<Card> castableForCaster = CardUtil.getCastableComponents(cardToCast, null, source, castingPlayer, game, null, playLand);
        if (filter != null) {
            castableForCaster.removeIf(c -> !filter.match(c, castingPlayer.getId(), source, game));
        }
        if (!castableForCaster.isEmpty()) {
            partsToCast = castableForCaster;
        }
        String partsInfo = partsToCast
                .stream()
                .map(MageObject::getLogName)
                .collect(Collectors.joining(" or "));
        if (partsToCast.isEmpty()
                || (optional && !player.chooseUse(getOutcome(payCosts), getChoiceMessage(payCosts, playLand, partsInfo), source, game))) {
            if (controlActive) {
                CardUtil.takeControlUnderPlayerEnd(game, source, controllingPlayer, castingPlayer);
            }
            return null;
        }

        UUID originalCardId;
        if (copyCard || allowRepeats) {
            originalCardId = cardToCast.getId();
            cardToCast = game.copyCard(cardToCast, source, castingPlayer.getId());
        } else {
            originalCardId = null;
        }

        boolean result = castChosenCard(castingPlayer, source, game, cardToCast, partsToCast, playLand, payCosts);

        if (controlActive) {
            CardUtil.takeControlUnderPlayerEnd(game, source, controllingPlayer, castingPlayer);
        }

        if (result) {
            if (rememberPlayed) {
                rememberedObjects.add(cardToCast);
            }
            if (forgetPlayed) {
                rememberedObjects.remove(cardToCast);
                if (originalCardId != null) {
                    rememberedObjects.removeIf(item -> item.getId().equals(originalCardId));
                }
            }
        }

        if (player.isComputer() && !result) {
            cards.remove(cardToCast);
        }
        return result ? cardToCast : null;
    }

    /**
     * Performs the actual cast of a chosen card. Sets the
     * {@code PlayFromNotOwnHandZone} state values, resolves the ability to
     * cast (spell ability or land play), applies alternative costs if set,
     * and handles additional effects on success.
     */
    private boolean castChosenCard(Player player, Ability source, Game game, Card cardToCast, List<Card> partsToCast, boolean playLand, boolean payCosts) {
        boolean noMana = !payCosts;
        partsToCast.forEach(card -> game.getState().setValue("PlayFromNotOwnHandZone" + card.getId(), Boolean.TRUE));
        ActivatedAbility chosenAbility;
        if (playLand) {
            chosenAbility = player.chooseLandOrSpellAbility(cardToCast, game, noMana);
        } else {
            chosenAbility = player.chooseAbilityForCast(cardToCast, game, noMana);
        }

        boolean result;
        if (chosenAbility instanceof SpellAbility) {
            ManaCosts<ManaCost> altMana = altManaCostProducer == null ? null : altManaCostProducer.apply(source, game, cardToCast);
            Costs<Cost> altCosts = altCostProducer == null ? null : altCostProducer.apply(source, game, cardToCast);
            if (altMana != null || altCosts != null) {
                // copy spell ability for cast with alt costs replaced
                SpellAbility spellAbility = (SpellAbility) chosenAbility.copy();
                spellAbility.clearManaCosts();
                spellAbility.clearManaCostsToPay();
                spellAbility.clearCosts();
                if (altMana != null) {
                    spellAbility.addCost(altMana);
                }
                if (altCosts != null) {
                    spellAbility.addCost(altCosts);
                }
                chosenAbility = spellAbility;
            }
            result = player.cast((SpellAbility) chosenAbility, game, noMana, new ApprovingObject(source, game));
        } else if (playLand && chosenAbility instanceof PlayLandAbility) {
            Card land = game.getCard(chosenAbility.getSourceId());
            result = land != null && player.playLand(land, game, true);
        } else {
            result = false;
        }

        partsToCast.forEach(card -> game.getState().setValue("PlayFromNotOwnHandZone" + card.getId(), null));
        clearMainCardPermission(game, cardToCast);
        if (additionalEffects != null) {
            FixedTarget fixedTarget = new FixedTarget(cardToCast, game);
            for (Effect effect : additionalEffects) {
                effect.setTargetPointer(fixedTarget.copy());
                if (effect instanceof OneShotEffect) {
                    effect.apply(game, source);
                } else {
                    game.addEffect((ContinuousEffect) effect, source);
                }
            }
        }
        return result;
    }

    /**
     * Clears the {@code PlayFromNotOwnHandZone} state values for the cast
     * card, its main card, and both halves if it's a split/double-faced card.
     */
    private void clearMainCardPermission(Game game, Card cardToCast) {
        if (cardToCast == null) {
            return;
        }
        game.getState().setValue("PlayFromNotOwnHandZone" + cardToCast.getId(), null);
        Card mainCard = cardToCast.getMainCard();
        if (mainCard != null) {
            game.getState().setValue("PlayFromNotOwnHandZone" + mainCard.getId(), null);
        }
        if (cardToCast instanceof CardWithParts) {
            Card leftHalfCard = ((CardWithParts) cardToCast).getLeftHalfCard();
            Card rightHalfCard = ((CardWithParts) cardToCast).getRightHalfCard();
            if (leftHalfCard != null) {
                game.getState().setValue("PlayFromNotOwnHandZone" + leftHalfCard.getId(), null);
            }
            if (rightHalfCard != null) {
                game.getState().setValue("PlayFromNotOwnHandZone" + rightHalfCard.getId(), null);
            }
        }
    }

    /**
     * Returns true if none of the cards in the collection have any
     * castable components (spell abilities or land plays).
     */
    private boolean cardsHaveNoCastableParts(Cards cards, FilterTyped filter, Ability source, Player player, Game game, boolean playLand) {
        return cards.getCards(game).stream()
                .filter(Objects::nonNull)
                .allMatch(card -> getCastableComponents(card, filter, source, player, game, playLand).isEmpty());
    }

    /**
     * Returns the list of castable components (spell abilities or land
     * plays) for the given card that also match the optional filter.
     */
    private List<Card> getCastableComponents(Card card, FilterTyped filter, Ability source, Player player, Game game, boolean playLand) {
        List<Card> castableComponents = CardUtil.getCastableComponents(card, null, source, player, game, null, playLand);
        if (filter != null) {
            castableComponents.removeIf(component -> !filter.match(component, player.getId(), source, game));
        }
        return castableComponents;
    }

    private Outcome getOutcome(boolean payCosts) {
        return payCosts ? Outcome.Benefit : Outcome.PlayForFree;
    }

    /**
     * Returns the prompt shown to the player when asked whether to cast
     * a specific card (used when {@link #optional} is true).
     */
    private String getChoiceMessage(boolean payCosts, boolean playLand, String partsInfo) {
        String action = playLand ? "Cast spell or play land" : "Cast spell";
        return payCosts
                ? action + " (" + partsInfo + ") by paying its costs?"
                : action + " without paying its mana cost (" + partsInfo + ")?";
    }

    /**
     * Returns the prompt shown between casts when casting multiple cards,
     * asking if the player wants to continue.
     */
    private String getContinueMessage(boolean payCosts, boolean playLand) {
        if (playLand) {
            return payCosts ? "Continue casting spells or playing lands?" : "Continue casting spells for free or playing lands?";
        }
        return payCosts ? "Continue casting spells?" : "Continue casting spells for free?";
    }
}
