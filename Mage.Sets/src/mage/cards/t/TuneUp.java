package mage.cards.t;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.ReturnFromGraveyardToBattlefieldTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author Jmlundeen
 */
public final class TuneUp extends CardImpl {

    public TuneUp(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{3}{W}");
        

        // Return target artifact card from your graveyard to the battlefield. If it's a Vehicle, it becomes an artifact creature.
        this.getSpellAbility().addEffect(new ReturnFromGraveyardToBattlefieldTargetEffect());
        this.getSpellAbility().addEffect(new TuneUpEffect());
        this.getSpellAbility().addTarget(new TargetCardInYourGraveyard(StaticFilters.FILTER_CARD_ARTIFACT_FROM_YOUR_GRAVEYARD));
    }

    private TuneUp(final TuneUp card) {
        super(card);
    }

    @Override
    public TuneUp copy() {
        return new TuneUp(this);
    }
}

class TuneUpEffect extends ContinuousEffectImpl {

    TuneUpEffect() {
        super(Duration.WhileOnBattlefield, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Benefit);
        this.staticText = "If it's a Vehicle, it becomes an artifact creature";
    }

    private TuneUpEffect(final TuneUpEffect effect) {
        super(effect);
    }

    @Override
    public TuneUpEffect copy() {
        return new TuneUpEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).addCardType(CardType.CREATURE);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = game.getPermanent(source.getFirstTarget());
        if (permanent != null && permanent.hasSubtype(SubType.VEHICLE, game)) {
            affectedObjects.add(permanent);
            return true;
        }
        return false;
    }
}
