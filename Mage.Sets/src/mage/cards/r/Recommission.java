package mage.cards.r;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.FilterCard;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.ManaValuePredicate;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.UUID;

/**
 *
 * @author weirddan455
 */
public final class Recommission extends CardImpl {

    private static final FilterCard filter
            = new FilterCard("artifact or creature card with mana value 3 or less from your graveyard");

    static {
        filter.add(Predicates.or(CardType.ARTIFACT.getPredicate(), CardType.CREATURE.getPredicate()));
        filter.add(new ManaValuePredicate(ComparisonType.FEWER_THAN, 4));
    }

    public Recommission(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{1}{W}");

        // Return target artifact or creature card with mana value 3 or less from your graveyard to the battlefield. If a creature enters the battlefield this way, it enters with an additional +1/+1 counter on it.
        this.getSpellAbility().addEffect(new RecommissionEffect());
        this.getSpellAbility().addTarget(new TargetCardInYourGraveyard(filter));
    }

    private Recommission(final Recommission card) {
        super(card);
    }

    @Override
    public Recommission copy() {
        return new Recommission(this);
    }
}

class RecommissionEffect extends OneShotEffect {

    RecommissionEffect() {
        super(Outcome.PutCardInPlay);
        this.staticText = "Return target artifact or creature card with mana value 3 or less from your graveyard to the battlefield. If a creature enters the battlefield this way, it enters with an additional +1/+1 counter on it.";
    }

    private RecommissionEffect(final RecommissionEffect effect) {
        super(effect);
    }

    @Override
    public RecommissionEffect copy() {
        return new RecommissionEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (controller == null || card == null) {
            return false;
        }
        game.addEffect(new EntersWithCountersEffect(
                Duration.OneUse, ContinuousAffected.STATIC_OR_DYNAMIC,
                CounterType.P1P1.createInstance()),
                source);
        controller.moveCards(card, Zone.BATTLEFIELD, source, game);
        return true;
    }
}
