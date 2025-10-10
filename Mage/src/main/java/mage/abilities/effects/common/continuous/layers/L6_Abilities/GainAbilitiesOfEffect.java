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
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.util.CardQuery;

import java.util.*;

/**
 * used for effects that grant abilities of other cards to itself or others
 * @author jmlundeen
 */
public class GainAbilitiesOfEffect extends ContinuousEffectBuilder {

    FilterAbility filter;
    Set<Zone> cardWithAbilityZones;
    TargetController cardController = TargetController.YOU;
    FilterCard cardWithAbilityFilter;
    FilterPermanent permanentWithAbilityFilter;
    ModifyAbilityFunction modifyAbilityFunction;
    boolean giveOwnAbilities;

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
     * Designate which player's cards to use. Supports YOU, OPPONENT, and EACH_PLAYER.
     * @param cardController {@link TargetController}
     */
    public GainAbilitiesOfEffect fromCardsControlledBy(TargetController cardController) {
        this.cardController = cardController;
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

    private GainAbilitiesOfEffect(final GainAbilitiesOfEffect effect) {
        super(effect);
        this.filter = effect.filter;
        this.cardWithAbilityZones = effect.cardWithAbilityZones;
        this.cardController = effect.cardController;
        this.cardWithAbilityFilter = effect.cardWithAbilityFilter;
        this.permanentWithAbilityFilter = effect.permanentWithAbilityFilter;
        this.modifyAbilityFunction = effect.modifyAbilityFunction;
        this.giveOwnAbilities = effect.giveOwnAbilities;
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
                    modifyAbilityFunction.apply(game, source, newAbility);
                }
            }
        }
    }

    private List<Ability> getAbilities(Ability source, Game game) {
        List<Ability> abilities = new ArrayList<>();
        if (giveOwnAbilities) {
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
        Player controller = game.getPlayer(source.getControllerId());
        List<MageObject> objects = new ArrayList<>();
        CardQuery cardQuery = new CardQuery(cardController, cardWithAbilityFilter, stackObjectFilter, permanentWithAbilityFilter);
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
}
