package mage.abilities.effects.common;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.FilterPermanent;
import mage.game.Game;
import mage.game.MoveCardsParameters;
import mage.players.Player;
import mage.util.CardUtil;

/**
 * @author LevelX2
 */
public class ExileAllEffect extends OneShotEffect {

    private final FilterPermanent filter;
    private final boolean forSource;

    public ExileAllEffect(FilterPermanent filter) {
        this(filter, false);
    }

    public ExileAllEffect(FilterPermanent filter, boolean forSource) {
        super(Outcome.Exile);
        this.filter = filter;
        this.forSource = forSource;
        this.staticText = "exile all " + filter.getMessage();
    }

    protected ExileAllEffect(final ExileAllEffect effect) {
        super(effect);
        this.filter = effect.filter.copy();
        this.forSource = effect.forSource;
    }

    @Override
    public ExileAllEffect copy() {
        return new ExileAllEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Cards cards = new CardsImpl();
        game.getBattlefield().getActivePermanents(
                filter, source.getControllerId(), source, game
        ).forEach(cards::add);
        if (forSource) {
            MoveCardsParameters parameters = new MoveCardsParameters(cards.getCards(game), Zone.EXILED)
                    .setExileId(CardUtil.getExileZoneId(game, source))
                    .setExileName(CardUtil.createObjectRelatedWindowTitle(source, game, null));
            return controller.moveCards(parameters, source, game);
        }
        return controller.moveCards(cards, Zone.EXILED, source, game);

    }

}
