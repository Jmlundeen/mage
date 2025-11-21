package mage.cards.p;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.choices.ChooseFriendsAndFoes;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.MoveCardsParameters;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCardInLibrary;
import mage.target.common.TargetSacrifice;
import mage.util.CardUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author TheElk801
 */
public final class PirsWhim extends CardImpl {

    public PirsWhim(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{3}{G}");

        // For each player, choose friend or foe. Each friend searches their library for a land card, puts it on the battlefield tapped, then shuffles their library. Each foe sacrifices an artifact or enchantment they control.
        this.getSpellAbility().addEffect(new PirsWhimEffect());
    }

    private PirsWhim(final PirsWhim card) {
        super(card);
    }

    @Override
    public PirsWhim copy() {
        return new PirsWhim(this);
    }
}

class PirsWhimEffect extends OneShotEffect {

    PirsWhimEffect() {
        super(Outcome.Benefit);
        this.staticText = "For each player, choose friend or foe. "
                + "Each friend searches their library for a land card, "
                + "puts it onto the battlefield tapped, then shuffles. "
                + "Each foe sacrifices an artifact or enchantment they control.";
    }

    private PirsWhimEffect(final PirsWhimEffect effect) {
        super(effect);
    }

    @Override
    public PirsWhimEffect copy() {
        return new PirsWhimEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        ChooseFriendsAndFoes choice = new ChooseFriendsAndFoes();
        if (controller == null || !choice.chooseFriendOrFoe(controller, source, game)) {
            return false;
        }
        // search lands for friends
        Cards toBattlefield = new CardsImpl();
        for (Player player : choice.getFriends()) {
            if (player != null) {
                TargetCardInLibrary target = new TargetCardInLibrary(0, 1, StaticFilters.FILTER_CARD_LAND);
                if (player.searchLibrary(target, source, game)) {
                    toBattlefield.add(target.getFirstTarget());
                }
            }
        }
        // move all together to battlefield tapped
        MoveCardsParameters parameters = new MoveCardsParameters(toBattlefield.getCards(game), Zone.BATTLEFIELD)
                .setTapped(true)
                .setByOwner(true);
        controller.moveCards(parameters, source, game);
        // finally, shuffle all libraries of friends
        for (Player player : choice.getFriends()) {
            if (player != null) {
                player.shuffleLibrary(source, game);
            }
        }
        List<Permanent> toSacrifice = new ArrayList<>();
        for (Player player : choice.getFoes()) {
            if (player != null) {
                TargetSacrifice target = new TargetSacrifice(1, StaticFilters.FILTER_PERMANENT_ARTIFACT_OR_ENCHANTMENT);
                target.choose(Outcome.Sacrifice, player.getId(), source, game);
                for (UUID permanentId : target.getTargets()) {
                    Permanent permanent = game.getPermanent(permanentId);
                    if (permanent != null) {
                        toSacrifice.add(permanent);
                        game.informPlayers(player.getLogName() + " chooses to sacrifice " + permanent.getLogName() + CardUtil.getSourceLogName(game, source));
                    }
                }
            }
        }
        for (Permanent permanent : toSacrifice) {
            permanent.sacrifice(source, game);
        }
        return true;
    }
}
