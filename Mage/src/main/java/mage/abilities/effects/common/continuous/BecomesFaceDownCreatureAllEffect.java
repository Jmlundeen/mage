package mage.abilities.effects.common.continuous;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.cards.ModalDoubleFacedCardHalf;
import mage.constants.Outcome;
import mage.filter.FilterPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.PermanentCard;

/**
 *
 *
 * @author LevelX2
 */

public class BecomesFaceDownCreatureAllEffect extends OneShotEffect {

    protected FilterPermanent filter;

    public BecomesFaceDownCreatureAllEffect(FilterPermanent filter) {
        super(Outcome.Neutral);
        this.filter = filter;
        staticText = "turn all " + filter.getMessage() + " face down. (They're 2/2 creatures.)";
    }

    protected BecomesFaceDownCreatureAllEffect(final BecomesFaceDownCreatureAllEffect effect) {
        super(effect);
        this.filter = effect.filter.copy();
    }

    @Override
    public BecomesFaceDownCreatureAllEffect copy() {
        return new BecomesFaceDownCreatureAllEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        for (Permanent perm : game.getBattlefield().getActivePermanents(filter, source.getControllerId(), source, game)) {
            if (!perm.isFaceDown() && !perm.isTransformable() && !(((PermanentCard) perm).getCard() instanceof ModalDoubleFacedCardHalf)) {
                BecomesFaceDownCreatureEffect.FaceDownType type = BecomesFaceDownCreatureEffect.findFaceDownType(game, perm);
                BecomesFaceDownCreatureEffect.makeFaceDownObject(game, source.getSourceId(), perm, type, null);
                perm.setFaceDown(true);
            }
        }
        return true;
    }
}
