package mage.cards.o;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.ActivateIfConditionActivatedAbility;
import mage.abilities.common.DiesSourceTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.Condition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.TransformSourceEffect;
import mage.abilities.effects.common.replacement.ReplaceTokenEffect;
import mage.abilities.keyword.VigilanceAbility;
import mage.abilities.mana.WhiteManaAbility;
import mage.cards.Card;
import mage.cards.CardSetInfo;
import mage.cards.TransformingDoubleFacedCard;
import mage.constants.*;
import mage.game.Game;
import mage.game.MoveCardsParameters;
import mage.players.Player;
import mage.watchers.common.PlayerAttackedWatcher;

import java.util.UUID;

/**
 * @author Susucr
 */
public final class OjerTaqDeepestFoundation extends TransformingDoubleFacedCard {

    public OjerTaqDeepestFoundation(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.GOD}, "{4}{W}{W}",
                "Temple of Civilization",
                new SuperType[]{}, new CardType[]{CardType.LAND}, new SubType[]{}, "");

        // Ojer Taq, Deepest Foundation
        this.getLeftHalfCard().setPT(6, 6);

        // Vigilance
        this.getLeftHalfCard().addAbility(VigilanceAbility.getInstance());

        // If one or more creature tokens would be created under your control, three times that many of those tokens are created instead.
        this.getLeftHalfCard().addAbility(new SimpleStaticAbility(new ReplaceTokenEffect(ReplaceTokenEffect.ModificationType.MULTIPLY, 3)
                .withTokenCondition(MageObject::isCreature)
                .setText("If one or more creature tokens would be created under your control, "
                        + "three times that many of those tokens are created instead")
        ));

        // When Ojer Taq dies, return it to the battlefield tapped and transformed under its owner's control.
        this.getLeftHalfCard().addAbility(new DiesSourceTriggeredAbility(new OjerTaqDeepestFoundationTransformEffect()));

        // Temple of Civilization
        // {T}: Add {W}.
        this.getRightHalfCard().addAbility(new WhiteManaAbility());

        // {2}{W}, {T}: Transform Temple of Civilization. Activate only if you attacked with three or more creatures this turn and only as a sorcery.
        Ability ability = new ActivateIfConditionActivatedAbility(
                new TransformSourceEffect(), new ManaCostsImpl<>("{2}{W}"), TempleOfCivilizationCondition.instance
        ).setTiming(TimingRule.SORCERY);
        ability.addCost(new TapSourceCost());
        ability.addWatcher(new PlayerAttackedWatcher());
        this.getRightHalfCard().addAbility(ability);
    }

    private OjerTaqDeepestFoundation(final OjerTaqDeepestFoundation card) {
        super(card);
    }

    @Override
    public OjerTaqDeepestFoundation copy() {
        return new OjerTaqDeepestFoundation(this);
    }
}

class OjerTaqDeepestFoundationTransformEffect extends OneShotEffect {

    OjerTaqDeepestFoundationTransformEffect() {
        super(Outcome.Benefit);
        staticText = "return it to the battlefield tapped and transformed under its owner's control";
    }

    private OjerTaqDeepestFoundationTransformEffect(final OjerTaqDeepestFoundationTransformEffect effect) {
        super(effect);
    }

    @Override
    public OjerTaqDeepestFoundationTransformEffect copy() {
        return new OjerTaqDeepestFoundationTransformEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card card = source.getSourceCardIfItStillExists(game);
        if (controller == null || card == null) {
            return false;
        }
        game.getState().setValue(TransformingDoubleFacedCard.VALUE_KEY_ENTER_TRANSFORMED + source.getSourceId(), Boolean.TRUE);
        MoveCardsParameters parameters = new MoveCardsParameters(card, Zone.BATTLEFIELD)
                .setTapped(true)
                .setByOwner(true);
        controller.moveCards(parameters, source, game);
        return true;
    }
}

enum TempleOfCivilizationCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        PlayerAttackedWatcher watcher = game.getState().getWatcher(PlayerAttackedWatcher.class);
        return watcher != null && watcher.getNumberOfAttackersCurrentTurn(source.getControllerId()) >= 3;
    }

    @Override
    public String toString() {
        return "you attacked with three or more creatures this turn";
    }
}
