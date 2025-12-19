package mage.abilities.effects.common.continuous.layers.L6_Abilities;

import mage.MageItem;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.cards.Card;
import mage.constants.*;
import mage.filter.FilterAbility;
import mage.filter.FilterCard;
import mage.filter.FilterPermanent;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.util.CardQuery;
import mage.util.CardUtil;

import java.util.*;

/**
 * used for effects that grant abilities of other cards to itself or others
 * @author jmlundeen
 */
public class GainAbilitiesOfEffect extends ContinuousEffectBuilder {

    FilterAbility filter;
    Set<Zone> cardWithAbilityZones;
    TargetController cardWithAbilityController = TargetController.YOU;
    FilterCard cardWithAbilityFilter;
    FilterPermanent permanentWithAbilityFilter;
    ModifyAbilityFunction modifyAbilityFunction;
    boolean fromSource;
    boolean fromSourceExiled;
    boolean fromSourceImprinted;

    public GainAbilitiesOfEffect(FilterAbility filter, String text) {
        this(Duration.WhileOnBattlefield, ContinuousAffected.SOURCE, filter, text);
    }

    public GainAbilitiesOfEffect(Duration duration, ContinuousAffected affected, FilterAbility filter, String text) {
        super(duration, Outcome.AddAbility, affected);
        this.staticText = text;
        this.filter = filter;
        addLayer(Layer.AbilityAddingRemovingEffects_6);
    }

    public GainAbilitiesOfEffect fromCardsInZones(FilterCard cardWithAbilityFilter, Zone... zones) {
        if (cardWithAbilityZones == null) {
            this.cardWithAbilityZones = new HashSet<>(Arrays.asList(zones));
        } else {
            this.cardWithAbilityZones.addAll(Arrays.asList(zones));
        }
        this.cardWithAbilityFilter = cardWithAbilityFilter;
        return this;
    }

    public GainAbilitiesOfEffect fromPermanents(FilterPermanent permanentWithAbilityFilter) {
        if (cardWithAbilityZones == null) {
            this.cardWithAbilityZones = new HashSet<>(Collections.singletonList(Zone.BATTLEFIELD));
        } else {
            this.cardWithAbilityZones.add(Zone.BATTLEFIELD);
        }
        this.permanentWithAbilityFilter = permanentWithAbilityFilter;
        return this;
    }

    /**
     * Sets the filter for cards when not using {@link #fromCardsInZones(FilterCard, Zone...)}
     */
    public GainAbilitiesOfEffect setCardWithAbilityFilter(FilterCard cardWithAbilityFilter) {
        this.cardWithAbilityFilter = cardWithAbilityFilter;
        return this;
    }

    /**
     * Sets the filter for permanents when not using {@link #fromPermanents(FilterPermanent)}
     */
    public GainAbilitiesOfEffect setPermanentWithAbilityFilter(FilterPermanent permanentWithAbilityFilter) {
        this.permanentWithAbilityFilter = permanentWithAbilityFilter;
        return this;
    }

    /**
     * Designate which player's cards to use. Supports YOU, OPPONENT, and EACH_PLAYER.
     * @param cardController {@link TargetController}
     */
    public GainAbilitiesOfEffect fromCardsControlledBy(TargetController cardController) {
        this.cardWithAbilityController = cardController;
        return this;
    }

    /**
     * Use when you need to perform actions on the ability after it is added to the object.
     * Drana and Linvala or Mairsil, the Pretender are examples of cards that need this.
     */
    public GainAbilitiesOfEffect modifyAbilities(ModifyAbilityFunction modifyAbilityFunction) {
        this.modifyAbilityFunction = modifyAbilityFunction;
        return this;
    }

    /**
     * Designate which player's cards to gain abilities from. Supports YOU, OPPONENT, and EACH_PLAYER.
     * @param cardWithAbilityController {@link TargetController}
     */
    public GainAbilitiesOfEffect setCardWithAbilityController(TargetController cardWithAbilityController) {
        this.cardWithAbilityController = cardWithAbilityController;
        return this;
    }

    /**
     * Use this when you want abilities from cards exiled by the source
     */
    public GainAbilitiesOfEffect fromSourceExiled() {
        this.fromSourceExiled = true;
        return this;
    }

    /**
     * Use this when you want abilities from cards imprinted on the source
     */
    public GainAbilitiesOfEffect fromSourceImprinted() {
        this.fromSourceImprinted = true;
        return this;
    }

    /**
     * Use this when you want abilities from the source itself
     */
    public GainAbilitiesOfEffect fromSource() {
        this.fromSource = true;
        return this;
    }

    private GainAbilitiesOfEffect(final GainAbilitiesOfEffect effect) {
        super(effect);
        this.filter = effect.filter;
        this.cardWithAbilityZones = effect.cardWithAbilityZones;
        this.cardWithAbilityController = effect.cardWithAbilityController;
        this.cardWithAbilityFilter = effect.cardWithAbilityFilter;
        this.permanentWithAbilityFilter = effect.permanentWithAbilityFilter;
        this.modifyAbilityFunction = effect.modifyAbilityFunction;
        this.fromSource = effect.fromSource;
        this.fromSourceExiled = effect.fromSourceExiled;
        this.fromSourceImprinted = effect.fromSourceImprinted;
    }

    @Override
    public GainAbilitiesOfEffect copy() {
        return new GainAbilitiesOfEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        List<Ability> abilities = getAbilities(source, game);
        for (MageItem object : affectedObjects) {
            for (Ability ability : abilities) {
                Ability newAbility = ((Permanent) object).addAbility(ability, source.getSourceId(), game, true);
                if (newAbility != null && modifyAbilityFunction != null) {
                    modifyAbilityFunction.apply(newAbility);
                }
            }
        }
    }

    private List<Ability> getAbilities(Ability source, Game game) {
        List<Ability> abilities = new ArrayList<>();
        if (fromSource) {
            Permanent permanent = source.getSourcePermanentIfItStillExists(game);
            if (permanent != null) {
                for (Ability ability : permanent.getAbilities(game)) {
                    if (filter.match(ability, game)) {
                        abilities.add(ability);
                    }
                }
            }
            return abilities;
        }
        if (fromSourceExiled) {
            ExileZone exileZone = game.getExile().getExileZone(CardUtil.getExileZoneId(
                    game, source.getSourceId(), game.getState().getZoneChangeCounter(source.getSourceId())
            ));
            if (exileZone != null && !exileZone.isEmpty()) {
                exileZone.getCards(game).stream()
                        .filter(card -> cardFilter == null || cardFilter.match(card, source.getControllerId(), source, game))
                        .forEach(card -> {
                            for (Ability ability : card.getAbilities(game)) {
                                if (filter.match(ability, game)) {
                                    abilities.add(ability);
                                }
                            }
                        });
            }
            return abilities;
        }
        if (fromSourceImprinted) {
            Permanent permanent = source.getSourcePermanentIfItStillExists(game);
            if (permanent != null) {
                for (UUID imprintedId : permanent.getImprinted()) {
                    Card card = game.getCard(imprintedId);
                    if (card != null && (cardWithAbilityFilter == null || cardWithAbilityFilter.match(card, source.getControllerId(), source, game))) {
                        for (Ability ability : card.getAbilities(game)) {
                            if (filter.match(ability, game)) {
                                abilities.add(ability);
                            }
                        }
                    }
                }
            }
            return abilities;
        }
        Player controller = game.getPlayer(source.getControllerId());
        List<MageObject> objects = new ArrayList<>();
        CardQuery cardQuery = new CardQuery(cardWithAbilityController, cardWithAbilityFilter, stackObjectFilter, permanentWithAbilityFilter);
        if (controller != null && cardWithAbilityZones != null) {
            for (Zone zone : cardWithAbilityZones) {
                cardQuery.getObjectsFromZone(game, zone, controller, source, objects);
            }
        }
        for (MageObject object : objects) {
            if (object instanceof Card) {
                for (Ability ability : ((Card) object).getAbilities(game)) {
                    if (filter.match(ability, game)) {
                        abilities.add(ability);
                    }
                }
            } else {
                for (Ability ability : object.getAbilities()) {
                    if (filter.match(ability, game)) {
                        abilities.add(ability);
                    }
                }
            }
        }
        return abilities;
    }

    @Override
    public int calculateResult(Game game, Ability source, List<MageItem> affectedObjects) {
        int result = 0;
        for (MageItem object : affectedObjects) {
            if (object instanceof Permanent) {
                Permanent permanent = (Permanent) object;
                result += (int) permanent.getAbilities().stream().filter(ability -> filter.match(ability, game)).count();
            }
        }
        return result;
    }
}
