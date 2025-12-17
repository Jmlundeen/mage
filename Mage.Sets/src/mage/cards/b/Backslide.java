
package mage.cards.b;

import mage.abilities.Ability;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BecomesFaceDownCreatureEffect;
import mage.abilities.keyword.CyclingAbility;
import mage.abilities.keyword.MorphAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.ModalDoubleFacedCardHalf;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.AbilityPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.PermanentCard;
import mage.target.Target;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 *
 * @author TheElk801
 */
public final class Backslide extends CardImpl {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent("creature with a morph ability");

    static {
        filter.add(new AbilityPredicate(MorphAbility.class));
    }

    public Backslide(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{U}");

        // Turn target creature with a morph ability face down.
        this.getSpellAbility().addEffect(new BackslideEffect());
        this.getSpellAbility().addTarget(new TargetPermanent(filter));

        // Cycling {U}
        this.addAbility(new CyclingAbility(new ManaCostsImpl<>("{U}")));

    }

    private Backslide(final Backslide card) {
        super(card);
    }

    @Override
    public Backslide copy() {
        return new Backslide(this);
    }
}

class BackslideEffect extends OneShotEffect {

    BackslideEffect() {
        super(Outcome.Benefit);
        this.staticText = "Turn target creature with a morph ability face down.";
    }

    private BackslideEffect(final BackslideEffect effect) {
        super(effect);
    }

    @Override
    public BackslideEffect copy() {
        return new BackslideEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        for (Target target : source.getTargets()) {
            for (UUID targetId : target.getTargets()) {
                Permanent permanent = game.getPermanent(targetId);
                if (!permanent.isFaceDown() && !permanent.isTransformable() && !(((PermanentCard) permanent).getCard() instanceof ModalDoubleFacedCardHalf)) {
                    BecomesFaceDownCreatureEffect.FaceDownType type = BecomesFaceDownCreatureEffect.findFaceDownType(game, permanent);
                    BecomesFaceDownCreatureEffect.makeFaceDownObject(permanent, type, null);
                    permanent.setFaceDown(true);
                    permanent.getFaceDownValues().getSubtype().add(SubType.HORROR);
                }
            }
        }
        return true;
    }
}
