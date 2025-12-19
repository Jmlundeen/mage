package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.game.Game;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.List;

/**
 * @author Viserion
 */
public class PlayAdditionalLandsControllerEffect extends ContinuousEffectImpl {

    protected int additionalCards;

    public PlayAdditionalLandsControllerEffect(int additionalCards, Duration duration) {
        super(duration, Layer.PlayerEffects, SubLayer.NA, Outcome.Benefit);
        this.additionalCards = additionalCards;
        setText();
    }

    protected PlayAdditionalLandsControllerEffect(final PlayAdditionalLandsControllerEffect effect) {
        super(effect);
        this.additionalCards = effect.additionalCards;
    }

    @Override
    public PlayAdditionalLandsControllerEffect copy() {
        return new PlayAdditionalLandsControllerEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Player) object).setLandsPerTurn(CardUtil.overflowInc(((Player) object).getLandsPerTurn(), additionalCards));
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player player = game.getPlayer(source.getControllerId());
        if (player != null) {
            affectedObjects.add(player);
            return true;
        } else {
            return false;
        }
    }

    private void setText() {
        StringBuilder sb = new StringBuilder();
        sb.append("You may play ");
        if (additionalCards == Integer.MAX_VALUE) {
            sb.append("any number of");
        } else {
            if (additionalCards > 1 && duration == Duration.EndOfTurn) {
                sb.append("up to ");
            }
            sb.append(CardUtil.numberToText(additionalCards, "an"));
        }
        sb.append(" additional land").append((additionalCards == 1 ? "" : "s"))
                .append(duration == Duration.EndOfTurn ? " this turn" : " on each of your turns");
        staticText = sb.toString();
    }

}
