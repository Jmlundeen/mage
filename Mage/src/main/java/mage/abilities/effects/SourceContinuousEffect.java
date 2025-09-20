package mage.abilities.effects;

import mage.MageItem;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.cards.Card;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;

public abstract class SourceContinuousEffect extends ContinuousEffectImpl {

    private boolean asCard = false;

    protected SourceContinuousEffect(Duration duration, Outcome outcome) {
        super(duration, outcome);
    }

    protected SourceContinuousEffect(Duration duration, Layer layer, SubLayer sublayer, Outcome outcome) {
        super(duration, layer, sublayer, outcome);
    }

    protected SourceContinuousEffect(SourceContinuousEffect effect) {
        super(effect);
        this.asCard = effect.asCard;
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        MageObjectReference mor = new MageObjectReference(source.getSourceId(), game);
        affectedObjectList.add(mor);
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        if (getAffectedObjectsSet()){
            for (MageObjectReference mor : affectedObjectList) {
                if (mor.refersTo(source, game) && mor.getPermanent(game) != null) {
                    affectedObjects.add(mor.getPermanent(game));
                }
            }
        } else if (!source.getAffectedObjects().isEmpty()) {
            affectedObjects.addAll(source.getAffectedObjects());
        } else {
            if (asCard) {
                Card card = source.getSourceCardIfItStillExists(game);
                if (card != null) {
                    affectedObjects.add(card);
                    source.getAffectedObjects().add(card);
                }
            }
            else {
                Permanent permanent = source.getSourcePermanentIfItStillExists(game);
                if (permanent != null) {
                    affectedObjects.add(permanent);
                    source.getAffectedObjects().add(permanent);
                }
            }
        }
        return !affectedObjects.isEmpty();
    }
}
