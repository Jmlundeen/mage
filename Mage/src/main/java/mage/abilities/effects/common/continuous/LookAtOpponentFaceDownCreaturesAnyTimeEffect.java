package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.card.FaceDownPredicate;
import mage.game.Game;
import mage.players.Player;

import java.util.List;

/**
 * @author notgreat
 */
public class LookAtOpponentFaceDownCreaturesAnyTimeEffect extends ContinuousEffectImpl {

    private final FilterCreaturePermanent filter;

    public LookAtOpponentFaceDownCreaturesAnyTimeEffect() {
        this(Duration.WhileOnBattlefield);
    }

    public LookAtOpponentFaceDownCreaturesAnyTimeEffect(Duration duration) {
        this(duration, TargetController.NOT_YOU);
    }

    public LookAtOpponentFaceDownCreaturesAnyTimeEffect(Duration duration, TargetController targetController) {
        super(duration, Layer.PlayerEffects, SubLayer.NA, Outcome.Benefit);
        staticText = makeText(duration, targetController);
        filter = new FilterCreaturePermanent();
        filter.add(FaceDownPredicate.instance);
        filter.add(targetController.getControllerPredicate());
    }

    protected LookAtOpponentFaceDownCreaturesAnyTimeEffect(final LookAtOpponentFaceDownCreaturesAnyTimeEffect effect) {
        super(effect);
        this.filter = effect.filter.copy();
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        Cards cards = new CardsImpl(game.getBattlefield().getActivePermanents(filter, source.getControllerId(), source, game));
        for (MageItem object : affectedObjects) {
            ((Player) object).lookAtCards(source, "Face Down Creatures", cards, game);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        if (game.inCheckPlayableState()) { // Ignored - see https://github.com/magefree/mage/issues/6994
            return false;
        }
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        affectedObjects.add(controller);
        return true;
    }

    @Override
    public LookAtOpponentFaceDownCreaturesAnyTimeEffect copy() {
        return new LookAtOpponentFaceDownCreaturesAnyTimeEffect(this);
    }

    private static String makeText(Duration duration, TargetController targetController) {
        StringBuilder sb = new StringBuilder();
        if (!duration.toString().isEmpty()) {
            sb.append(duration);
            sb.append(", ");
        }
        sb.append("you may look at face-down creatures ");
        switch (targetController) {
            case NOT_YOU:
                sb.append("you don't control ");
                break;
            case OPPONENT:
                sb.append("your opponents control ");
        }
        sb.append("any time");
        return sb.toString();
    }
}
