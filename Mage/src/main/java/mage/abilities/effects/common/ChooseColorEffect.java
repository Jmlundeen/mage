
package mage.abilities.effects.common;

import mage.MageObject;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.choices.ChoiceColor;
import mage.constants.Outcome;
import mage.constants.TargetController;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.Locale;
import java.util.UUID;

/**
 * @author Plopman
 */
public class ChooseColorEffect extends OneShotEffect {

    private final String exceptColor;
    private final TargetController targetController;

    public ChooseColorEffect(Outcome outcome) {
        this(outcome, null, TargetController.YOU);
    }

    public ChooseColorEffect(Outcome outcome, TargetController targetController) {
        this(outcome, null, targetController);
    }

    public ChooseColorEffect(Outcome outcome, String exceptColor) {
        this(outcome, exceptColor, TargetController.YOU);
    }

    public ChooseColorEffect(Outcome outcome, String exceptColor, TargetController targetController) {
        super(outcome);
        this.exceptColor = exceptColor;
        this.targetController = targetController;
        staticText = "choose a color" + (exceptColor != null ? " other than " + exceptColor.toLowerCase(Locale.ENGLISH) : "");
    }

    protected ChooseColorEffect(final ChooseColorEffect effect) {
        super(effect);
        this.exceptColor = effect.exceptColor;
        this.targetController = effect.targetController;
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = getChoicePlayer(game, source);
        MageObject mageObject = game.getPermanentEntering(source.getSourceId());
        if (mageObject == null) {
            mageObject = game.getObject(source);
        }
        ChoiceColor choice = new ChoiceColor();
        if (exceptColor != null) {
            choice.removeColorFromChoices(exceptColor);
        }
        if (controller == null || mageObject == null || !controller.choose(outcome, choice, game)) {
            return false;
        }
        game.informPlayers(mageObject.getLogName() + ": " + controller.getLogName() + " has chosen " + choice.getChoice());
        game.getState().setValue(mageObject.getId() + "_color", choice.getColor());
        if (mageObject instanceof Permanent) {
            ((Permanent) mageObject).addInfo("chosen color", CardUtil.addToolTipMarkTags("Chosen color: " + choice.getChoice()), game);
        }
        return true;
    }

    private Player getChoicePlayer(Game game, Ability source) {
        switch (targetController) {
            case ACTIVE -> {
                return game.getPlayer(game.getActivePlayerId());
            }
            case YOU -> {
                return game.getPlayer(source.getControllerId());
            }
            default -> throw new IllegalStateException("Unknown target controller: " + targetController);
        }
    }

    @Override
    public ChooseColorEffect copy() {
        return new ChooseColorEffect(this);
    }

    public static ObjectColor getChosenColor(UUID objectId, Game game) {
        return getChosenColor(objectId, game, "_color");
    }

    public static ObjectColor getChosenColor(UUID objectId, Game game, String colorPostfix) {
        return (ObjectColor) game.getState().getValue(objectId + colorPostfix);
    }
}
