package mage.cards.a;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.common.delayed.ReflexiveTriggeredAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.AsThoughEffectImpl;
import mage.abilities.effects.AsThoughManaEffect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.layers.L6_Abilities.GainAbilitiesOfEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.filter.StaticFilters;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.ManaPoolItem;
import mage.players.Player;
import mage.target.common.TargetCardInGraveyard;
import mage.target.common.TargetControlledCreaturePermanent;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class AgathasSoulCauldron extends CardImpl {

    private static final FilterPermanent filter = new FilterControlledCreaturePermanent();

    static {
        filter.add(CounterType.P1P1.getPredicate());
    }

    public AgathasSoulCauldron(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{2}");

        this.supertype.add(SuperType.LEGENDARY);

        // You may spend mana as though it were mana of any color to activate abilities of creatures you control.
        this.addAbility(new SimpleStaticAbility(new AgathasSoulCauldronManaEffect()));

        // Creatures you control with +1/+1 counters on them have all activated abilities of all creature cards exiled with Agatha's Soul Cauldron.
        this.addAbility(new SimpleStaticAbility(new GainAbilitiesOfEffect(
                Duration.WhileOnBattlefield,
                ContinuousAffected.STATIC,
                StaticFilters.FILTER_ACTIVATED_ABILITY,
                "creatures you control with +1/+1 counters on them have all activated abilities of all creature cards exiled with {this}")
                .fromSourceExiled()
                .setCardWithAbilityFilter(StaticFilters.FILTER_CARD_CREATURE)
                .setAffectedZones(Zone.BATTLEFIELD)
                .setPermanentFilter(filter)
        ));

        // {T}: Exile target card from a graveyard. When a creature card is exiled this way, put a +1/+1 counter on target creature you control.
        Ability ability = new SimpleActivatedAbility(new AgathasSoulCauldronExileEffect(), new TapSourceCost());
        ability.addTarget(new TargetCardInGraveyard());
        this.addAbility(ability);
    }

    private AgathasSoulCauldron(final AgathasSoulCauldron card) {
        super(card);
    }

    @Override
    public AgathasSoulCauldron copy() {
        return new AgathasSoulCauldron(this);
    }
}

class AgathasSoulCauldronManaEffect extends AsThoughEffectImpl implements AsThoughManaEffect {

    AgathasSoulCauldronManaEffect() {
        super(AsThoughEffectType.SPEND_OTHER_MANA, Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "you may spend mana as though it were mana of any color to activate abilities of creatures you control";
    }

    private AgathasSoulCauldronManaEffect(final AgathasSoulCauldronManaEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public AgathasSoulCauldronManaEffect copy() {
        return new AgathasSoulCauldronManaEffect(this);
    }

    @Override
    public boolean applies(UUID objectId, Ability source, UUID affectedControllerId, Game game) {
        if (!source.isControlledBy(affectedControllerId)) {
            return false;
        }
        Permanent permanent = game.getPermanent(CardUtil.getMainCardId(game, objectId));
        return permanent != null && permanent.isControlledBy(source.getControllerId()) && permanent.isCreature(game);
    }

    @Override
    public ManaType getAsThoughManaType(ManaType manaType, ManaPoolItem mana, UUID affectedControllerId, Ability source, Game game) {
        return mana.getFirstAvailable();
    }
}

class AgathasSoulCauldronExileEffect extends OneShotEffect {

    AgathasSoulCauldronExileEffect() {
        super(Outcome.Benefit);
        staticText = "exile target card from a graveyard. When a creature card " +
                "is exiled this way, put a +1/+1 counter on target creature you control";
    }

    private AgathasSoulCauldronExileEffect(final AgathasSoulCauldronExileEffect effect) {
        super(effect);
    }

    @Override
    public AgathasSoulCauldronExileEffect copy() {
        return new AgathasSoulCauldronExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (player == null || card == null) {
            return false;
        }
        player.moveCardsToExile(
                card, source, game, true,
                CardUtil.getExileZoneId(game, source),
                CardUtil.getSourceName(game, source)
        );
        if (!card.isCreature(game)) {
            return true;
        }
        ReflexiveTriggeredAbility ability = new ReflexiveTriggeredAbility(
                new AddCountersTargetEffect(CounterType.P1P1.createInstance()), false
        );
        ability.addTarget(new TargetControlledCreaturePermanent());
        game.fireReflexiveTriggeredAbility(ability, source);
        return true;
    }
}
