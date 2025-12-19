package mage.cards.t;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.LoyaltyAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.continuous.AddCardTypeTargetEffect;
import mage.abilities.effects.common.continuous.BecomesFaceDownCreatureEffect;
import mage.abilities.effects.common.continuous.SetBasePowerToughnessTargetEffect;
import mage.cards.*;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.MoveCardsParameters;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.Target;
import mage.target.TargetPermanent;
import mage.target.common.TargetCardInHand;

import java.util.List;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class TezzeretCruelMachinist extends CardImpl {

    public TezzeretCruelMachinist(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.PLANESWALKER}, "{4}{U}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.TEZZERET);
        this.setStartingLoyalty(4);

        // +1: Draw a card.
        this.addAbility(new LoyaltyAbility(new DrawCardSourceControllerEffect(1), 1));

        // 0: Until your next turn, target artifact you control becomes a 5/5 creature in addition to its other types.
        Ability ability = new LoyaltyAbility(new AddCardTypeTargetEffect(
                Duration.UntilYourNextTurn, CardType.ARTIFACT, CardType.CREATURE
        ).setText("Until your next turn, target artifact you control becomes an artifact creature"), 0);
        ability.addEffect(new SetBasePowerToughnessTargetEffect(
                5, 5, Duration.UntilYourNextTurn
        ).setText("with base power and toughness 5/5"));
        ability.addTarget(new TargetPermanent(StaticFilters.FILTER_CONTROLLED_PERMANENT_ARTIFACT));
        this.addAbility(ability);

        // −7: Put any number of cards from your hand onto the battlefield face down. They're 5/5 artifact creatures.
        this.addAbility(new LoyaltyAbility(new TezzeretCruelMachinistEffect(), -7));
    }

    private TezzeretCruelMachinist(final TezzeretCruelMachinist card) {
        super(card);
    }

    @Override
    public TezzeretCruelMachinist copy() {
        return new TezzeretCruelMachinist(this);
    }
}

class TezzeretCruelMachinistEffect extends OneShotEffect {

    TezzeretCruelMachinistEffect() {
        super(Outcome.Benefit);
        this.staticText = "put any number of cards from your hand onto the battlefield face down. They're 5/5 artifact creatures";
    }

    private TezzeretCruelMachinistEffect(final TezzeretCruelMachinistEffect effect) {
        super(effect);
    }

    @Override
    public TezzeretCruelMachinistEffect copy() {
        return new TezzeretCruelMachinistEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        Target target = new TargetCardInHand(0, Integer.MAX_VALUE, StaticFilters.FILTER_CARD);
        player.choose(outcome, target, source, game);
        Cards cardsToMove = new CardsImpl(target.getTargets());
        if (cardsToMove.isEmpty()) {
            return false;
        }
        for (Card card : cardsToMove.getCards(game)) {
            BecomesFaceDownCreatureEffect.makeFaceDownObject(card, BecomesFaceDownCreatureEffect.FaceDownType.MANUAL, null);
            card.getFaceDownValues().getCardType().add(CardType.ARTIFACT);
            card.getFaceDownValues().setPower(new MageInt(5));
            card.getFaceDownValues().setToughness(new MageInt(5));
        }
        MoveCardsParameters parameters = new MoveCardsParameters(cardsToMove.getCards(game), Zone.BATTLEFIELD)
                .setFaceDown(true)
                .setByOwner(true);
        player.moveCards(parameters, source, game);
        return true;
    }
}

class TezzeretCruelMachinistCardTypeEffect extends ContinuousEffectImpl {

    TezzeretCruelMachinistCardTypeEffect() {
        super(Duration.Custom, Layer.CopyEffects_1, SubLayer.FaceDownEffects_1b, Outcome.Neutral);
    }

    private TezzeretCruelMachinistCardTypeEffect(final TezzeretCruelMachinistCardTypeEffect effect) {
        super(effect);
    }

    @Override
    public TezzeretCruelMachinistCardTypeEffect copy() {
        return new TezzeretCruelMachinistCardTypeEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            permanent.removeAllSuperTypes(game);
            permanent.removeAllCardTypes(game);
            permanent.removeAllSubTypes(game);
            permanent.addCardType(game, CardType.ARTIFACT, CardType.CREATURE);
            permanent.getPower().setModifiedBaseValue(5);
            permanent.getToughness().setModifiedBaseValue(5);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (UUID targetId : getTargetPointer().getTargets(game, source)) {
            Permanent target = game.getPermanent(targetId);
            if (target == null || !target.isFaceDown(game)) {
                continue;
            }
            affectedObjects.add(target);
        }
        if (affectedObjects.isEmpty()) {
            discard();
        }
        return !affectedObjects.isEmpty();
    }
}
