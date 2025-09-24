package mage.cards.r;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Abilities;
import mage.abilities.AbilitiesImpl;
import mage.abilities.Ability;
import mage.abilities.ActivatedAbility;
import mage.abilities.common.ActivateAsSorceryActivatedAbility;
import mage.abilities.common.DealsCombatDamageToAPlayerTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.PayEnergyCost;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.MillCardsTargetEffect;
import mage.abilities.effects.common.counter.GetEnergyCountersControllerEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.FilterCard;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCardInGraveyard;

import java.util.List;
import java.util.UUID;

/**
 * @author Susucr
 */
public final class RexCyberHound extends CardImpl {

    public RexCyberHound(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{1}{W}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ROBOT);
        this.subtype.add(SubType.DOG);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Whenever Rex, Cyber-Hound deals combat damage to a player, they mill two cards and you get {E}{E}.
        Ability ability = new DealsCombatDamageToAPlayerTriggeredAbility(
                new MillCardsTargetEffect(2).setText("they mill two cards"), false, true);
        ability.addEffect(new GetEnergyCountersControllerEffect(2).concatBy("and"));
        this.addAbility(ability);

        // Pay {E}{E}: Choose target creature card in a graveyard. Exile it with a brain counter on it. Activate only as a sorcery.
        ability = new ActivateAsSorceryActivatedAbility(new RexCyberhoundTargetEffect(), new PayEnergyCost(2));
        ability.addTarget(new TargetCardInGraveyard(StaticFilters.FILTER_CARD_CREATURE));
        this.addAbility(ability);

        // Rex has all activated abilities of all cards in exile with brain counters on them.
        this.addAbility(new SimpleStaticAbility(new RexCyberhoundContinuousEffect()));
    }

    private RexCyberHound(final RexCyberHound card) {
        super(card);
    }

    @Override
    public RexCyberHound copy() {
        return new RexCyberHound(this);
    }
}

class RexCyberhoundTargetEffect extends OneShotEffect {

    RexCyberhoundTargetEffect() {
        super(Outcome.Exile);
        staticText = "Choose target creature card in a graveyard. Exile it with a brain counter on it";
    }

    private RexCyberhoundTargetEffect(final RexCyberhoundTargetEffect effect) {
        super(effect);
    }

    @Override
    public RexCyberhoundTargetEffect copy() {
        return new RexCyberhoundTargetEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        Player controller = game.getPlayer(source.getControllerId());
        if (card == null || controller == null) {
            return false;
        }
        controller.moveCards(card, Zone.EXILED, source, game);
        if (game.getState().getZone(card.getId()) == Zone.EXILED) {
            card.addCounters(CounterType.BRAIN.createInstance(), source, game);
        }
        return true;
    }

}

class RexCyberhoundContinuousEffect extends ContinuousEffectImpl {

    private static final FilterCard filter = new FilterCard();

    static {
        filter.add(CounterType.BRAIN.getPredicate());
    }

    RexCyberhoundContinuousEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "{this} has all activated abilities of all cards in exile with brain counters on them";
        addDependencyType(DependencyType.AddingAbility);
    }

    private RexCyberhoundContinuousEffect(final RexCyberhoundContinuousEffect effect) {
        super(effect);
    }

    @Override
    public RexCyberhoundContinuousEffect copy() {
        return new RexCyberhoundContinuousEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        Abilities<Ability> abilities = new AbilitiesImpl<>();
        for (Card card : game.getExile().getCardsOwned(filter, source.getControllerId(), source, game)) {
            for (Ability ability : card.getAbilities(game)) {
                if (ability.isActivatedAbility()) {
                    ActivatedAbility copyAbility = (ActivatedAbility) ability.copy();
                    abilities.add(copyAbility);
                }
            }
        }
        for (MageItem object : affectedObjects) {
            for (Ability ability : abilities) {
                ((Permanent) object).addAbility(ability, source.getSourceId(), game, true);
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent perm = game.getPermanent(source.getSourceId());
        if (perm != null) {
            affectedObjects.add(perm);
        }
        return !affectedObjects.isEmpty();
    }
}
