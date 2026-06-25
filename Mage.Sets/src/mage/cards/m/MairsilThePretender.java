package mage.cards.m;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.ActivatedAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.layers.L6_Abilities.GainAbilitiesOfEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.FilterCard;
import mage.filter.FilterTyped;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.typed.ability.type.ActivatedAbilityPredicate;
import mage.filter.predicate.typed.card.CardPredicate;
import mage.filter.predicate.typed.card.ICardPredicate;
import mage.game.Game;
import mage.players.Player;
import mage.target.Target;
import mage.target.common.TargetCardInHand;
import mage.target.common.TargetCardInYourGraveyard;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class MairsilThePretender extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("activated abilities of cards you own in exile with cage counters on them")
            .addAll(
                    CardPredicate.instance,
                    ICardPredicate.getOSPPredicate(CounterType.CAGE.getPredicate())
            )
            .add(ActivatedAbilityPredicate.instance);

    public MairsilThePretender(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}{B}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.WIZARD);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // When Mairsil, the Pretender enters the battlefield, you may exile an artifact or creature card from your hand
        // or graveyard and put a cage counter on it.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new MairsilThePretenderExileEffect(), true));

        // Mairsil, the Pretender has all activated abilities of all cards you own in exile with cage counters on them. 
        // You may activate each of those abilities only once each turn.
        this.addAbility(new SimpleStaticAbility(new GainAbilitiesOfEffect()
                .setAbilityFilter(filter, Zone.EXILED)
                .modifyAbilities((newAbility) -> ((ActivatedAbility) newAbility).setMaxActivationsPerTurn(1))
                .setText("{this} has all activated abilities of all cards you own in exile with cage counters on them. You may activate each of those abilities only once each turn")
        ));
    }

    private MairsilThePretender(final MairsilThePretender card) {
        super(card);
    }

    @Override
    public MairsilThePretender copy() {
        return new MairsilThePretender(this);
    }
}

class MairsilThePretenderExileEffect extends OneShotEffect {

    private static final FilterCard filter = new FilterCard();

    static {
        filter.add(Predicates.or(CardType.ARTIFACT.getPredicate(), CardType.CREATURE.getPredicate()));
    }

    MairsilThePretenderExileEffect() {
        super(Outcome.Benefit);
        this.staticText = "you may exile an artifact or creature card from your hand or graveyard and put a cage counter on it.";
    }

    private MairsilThePretenderExileEffect(final MairsilThePretenderExileEffect effect) {
        super(effect);
    }

    @Override
    public MairsilThePretenderExileEffect copy() {
        return new MairsilThePretenderExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }

        // Outcome.Detriment - AI must exile from grave only, not hand
        Target target;
        if (controller.chooseUse(Outcome.Detriment, "Exile a card from your hand? (No = from graveyard)", source, game)) {
            // from hand
            target = new TargetCardInHand(0, 1, filter);
            controller.choose(outcome, target, source, game);
        } else {
            // from graveyard
            target = new TargetCardInYourGraveyard(0, 1, filter);
            target.choose(outcome, source.getControllerId(), source.getSourceId(), source, game);
        }

        Card card = controller.getHand().get(target.getFirstTarget(), game);
        if (card != null) {
            return CardUtil.moveCardWithCounter(game, source, controller, card, Zone.EXILED, CounterType.CAGE.createInstance());
        }
        card = controller.getGraveyard().get(target.getFirstTarget(), game);
        if (card != null) {
            return CardUtil.moveCardWithCounter(game, source, controller, card, Zone.EXILED, CounterType.CAGE.createInstance());
        }
        return false;
    }
}
