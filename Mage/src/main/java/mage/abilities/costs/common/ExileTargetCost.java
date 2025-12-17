

package mage.abilities.costs.common;

import mage.abilities.Ability;
import mage.abilities.costs.Cost;
import mage.abilities.costs.CostImpl;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.MoveCardsParameters;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetControlledPermanent;
import mage.util.CardUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author emerald000
 */
public class ExileTargetCost extends CostImpl {

    List<Permanent> permanents = new ArrayList<>();

    public ExileTargetCost(TargetControlledPermanent target) {
        target.withNotTarget(true);
        this.addTarget(target);
        this.text = "exile " + target.getDescription();
    }

    protected ExileTargetCost(ExileTargetCost cost) {
        super(cost);
        for (Permanent permanent : cost.permanents) {
            this.permanents.add(permanent.copy());
        }
    }

    @Override
    public boolean pay(Ability ability, Game game, Ability source, UUID controllerId, boolean noMana, Cost costToPay) {
        Player player = game.getPlayer(ability.getControllerId());
        if (player == null || !this.getTargets().choose(Outcome.Exile, controllerId, source.getSourceId(), source, game)) {
            return paid;
        }
        Cards cards = new CardsImpl();
        for (UUID targetId : this.getTargets().get(0).getTargets()) {
            Permanent permanent = game.getPermanent(targetId);
            if (permanent == null) {
                return false;
            }
            cards.add(permanent);
            permanents.add(permanent.copy());
            // 117.11. The actions performed when paying a cost may be modified by effects.
            // Even if they are, meaning the actions that are performed don't match the actions
            // that are called for, the cost has still been paid.
            // so return state here is not important because the user indended to exile the target anyway
        }
        MoveCardsParameters parameters = new MoveCardsParameters(cards.getCards(game), Zone.EXILED)
                .setExileId(CardUtil.getExileZoneId(game, source))
                .setExileName(CardUtil.createObjectRelatedWindowTitle(source, game, null));
        player.moveCards(parameters, source, game);
        paid = true;
        return paid;
    }

    @Override
    public boolean canPay(Ability ability, Ability source, UUID controllerId, Game game) {
        return canChooseOrAlreadyChosen(ability, source, controllerId, game);
    }

    @Override
    public ExileTargetCost copy() {
        return new ExileTargetCost(this);
    }

    public List<Permanent> getPermanents() {
        return permanents;
    }
}
