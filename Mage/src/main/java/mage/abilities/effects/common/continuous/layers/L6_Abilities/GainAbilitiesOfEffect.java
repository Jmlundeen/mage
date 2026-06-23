package mage.abilities.effects.common.continuous.layers.L6_Abilities;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.effects.common.continuous.generic.GenericContinuousEffect;
import mage.cards.Card;
import mage.constants.*;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.ability.type.AbilityPredicate;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.util.CardUtil;
import mage.util.ObjectQuery;
import org.jspecify.annotations.NonNull;

import java.util.*;

/**
 * used for effects that grant abilities of other cards to itself or others
 * @author jmlundeen
 */
public class GainAbilitiesOfEffect extends GenericContinuousEffect {

    FilterTyped abilityFilter = new FilterTyped("ability")
            .add(AbilityPredicate.instance);
    EnumSet<Zone> cardWithAbilityZones;
    ModifyAbilityFunction modifyAbilityFunction;
    boolean fromSource;
    boolean fromSourceExiled;
    boolean fromSourceImprinted;

    public GainAbilitiesOfEffect() {
        this(Duration.WhileOnBattlefield);
    }

    public GainAbilitiesOfEffect(Duration duration) {
        this(duration, null, ContinuousAffected.SOURCE);
    }

    public GainAbilitiesOfEffect(Duration duration, FilterTyped giveAbilitiesToFilter) {
        this(duration, giveAbilitiesToFilter, ContinuousAffected.STATIC_OR_DYNAMIC, Zone.BATTLEFIELD);
    }

    public GainAbilitiesOfEffect(Duration duration, FilterTyped giveAbilitiesToFilter, ContinuousAffected giveAbilitiesTo, Zone... zones) {
        super(duration, Outcome.AddAbility);
        this.filter = giveAbilitiesToFilter;
        this.affected = giveAbilitiesTo;
        this.layer = Layer.AbilityAddingRemovingEffects_6;
        this.affectedZones = zones.length == 0 ? EnumSet.noneOf(Zone.class) : EnumSet.copyOf(Arrays.asList(zones));
    }

    /**
     * Set the filter for the objects with abilities. Filters on both the object and it's abilities.
     */
    public GainAbilitiesOfEffect setAbilityFilter(@NonNull FilterTyped abilityFilter, Zone... zones) {
        this.abilityFilter = abilityFilter;
        this.cardWithAbilityZones = zones.length == 0 ? EnumSet.noneOf(Zone.class) : EnumSet.copyOf(Arrays.asList(zones));
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
        this.abilityFilter = effect.abilityFilter;
        this.cardWithAbilityZones = effect.cardWithAbilityZones;
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
                    if (abilityFilter.match(ability, source.getControllerId(), source, game)) {
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
                        .filter(card -> abilityFilter.match(card, source.getControllerId(), source, game))
                        .forEach(card -> {
                            for (Ability ability : card.getAbilities(game)) {
                                if (abilityFilter.match(card, source.getControllerId(), source, game)) {
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
                    if (card != null && (abilityFilter.match(card, source.getControllerId(), source, game))) {
                        for (Ability ability : card.getAbilities(game)) {
                            if (abilityFilter.match(card, source.getControllerId(), source, game)) {
                                abilities.add(ability);
                            }
                        }
                    }
                }
            }
            return abilities;
        }
        Player controller = game.getPlayer(source.getControllerId());
        if (controller != null) {
            abilities.addAll(ObjectQuery.queryAbilities(game, controller, source, cardWithAbilityZones, abilityFilter));
        }
        return abilities;
    }

    @Override
    public int calculateResult(Game game, Ability source, List<MageItem> affectedObjects) {
        int result = 0;
        for (MageItem object : affectedObjects) {
            if (object instanceof Permanent permanent) {
                result += (int) permanent.getAbilities().stream().filter(ability -> abilityFilter.match(ability, source.getControllerId(), source, game)).count();
            }
        }
        return result;
    }
}
