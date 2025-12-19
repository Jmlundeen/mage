package mage.cards.a;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.effects.common.continuous.GainControlTargetEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenAllEffect;
import mage.abilities.keyword.DefenderAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.token.SoldierToken;
import mage.players.Player;
import mage.target.Target;
import mage.target.common.TargetOpponent;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class AkroanHorse extends CardImpl {

    public AkroanHorse(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{4}");
        this.subtype.add(SubType.HORSE);
        this.power = new MageInt(0);
        this.toughness = new MageInt(4);

        // Defender
        this.addAbility(DefenderAbility.getInstance());

        // When Akroan Horse enters the battlefield, an opponent gains control of it.
        this.addAbility(new EntersBattlefieldTriggeredAbility(
                new AkroanHorseChangeControlEffect(), false
        ));

        // At the beginning of your upkeep, each opponent create a 1/1 white Soldier creature token.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(
                new CreateTokenAllEffect(new SoldierToken(), TargetController.OPPONENT)
        ));
    }

    private AkroanHorse(final AkroanHorse card) {
        super(card);
    }

    @Override
    public AkroanHorse copy() {
        return new AkroanHorse(this);
    }
}

class AkroanHorseChangeControlEffect extends OneShotEffect {

    AkroanHorseChangeControlEffect() {
        super(Outcome.Benefit);
        this.staticText = "an opponent gains control of it";
    }

    private AkroanHorseChangeControlEffect(final AkroanHorseChangeControlEffect effect) {
        super(effect);
    }

    @Override
    public AkroanHorseChangeControlEffect copy() {
        return new AkroanHorseChangeControlEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Target target = new TargetOpponent();
        target.withNotTarget(true);
        controller.chooseTarget(outcome, target, source, game);
        ContinuousEffect effect = new GainControlTargetEffect(Duration.Custom, target.getFirstTarget());
        effect.setTargetPointer(new FixedTarget(source.getSourceId(), game));
        game.addEffect(effect, source);
        return true;
    }
}
