package mage.cards.n;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.condition.Condition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.decorator.ConditionalOneShotEffect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.FlipSourceEffect;
import mage.abilities.effects.common.discard.DiscardTargetEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.CardSetInfo;
import mage.cards.FlipCard;
import mage.constants.*;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetOpponent;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class NezumiShortfang extends FlipCard {

    public NezumiShortfang(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new CardType[]{CardType.CREATURE}, new SubType[]{SubType.RAT, SubType.ROGUE}, "{1}{B}",
                "Stabwhisker the Odious",
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.RAT, SubType.SHAMAN});

        // Nezumi Shortfang
        this.getLeftHalfCard().setPT(1, 1);

        // {1}{B}, {tap}: Target opponent discards a card. Then if that player has no cards in hand, flip Nezumi Shortfang.
        Ability ability = new SimpleActivatedAbility(new DiscardTargetEffect(1), new ManaCostsImpl<>("{1}{B}"));
        ability.addCost(new TapSourceCost());
        ability.addTarget(new TargetOpponent());
        ability.addEffect(new ConditionalOneShotEffect(
                new FlipSourceEffect(),
                new CardsInTargetOpponentHandCondition(ComparisonType.FEWER_THAN, 1),
                "Then if that player has no cards in hand, flip {this}"));
        this.getLeftHalfCard().addAbility(ability);

        // Stabwhisker the Odious
        this.getRightHalfCard().setPT(3, 3);

        // At the beginning of each opponent's upkeep, that player loses 1 life for each card fewer than three in their hand.
        this.getRightHalfCard().addAbility(new BeginningOfUpkeepTriggeredAbility(
                TargetController.OPPONENT, new StabwhiskerLoseLifeEffect(), false));
    }

    private NezumiShortfang(final NezumiShortfang card) {
        super(card);
    }

    @Override
    public NezumiShortfang copy() {
        return new NezumiShortfang(this);
    }
}

class StabwhiskerLoseLifeEffect extends OneShotEffect {

    StabwhiskerLoseLifeEffect() {
        super(Outcome.LoseLife);
        this.staticText = "that player loses 1 life for each card fewer than three in their hand";
    }

    private StabwhiskerLoseLifeEffect(final StabwhiskerLoseLifeEffect effect) {
        super(effect);
    }

    @Override
    public StabwhiskerLoseLifeEffect copy() {
        return new StabwhiskerLoseLifeEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player opponent = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (opponent != null) {
            int lifeLose = 3 - opponent.getHand().size();
            if (lifeLose > 0) {
                opponent.loseLife(lifeLose, game, source, false);
            }
            return true;
        }
        return false;
    }
}

class CardsInTargetOpponentHandCondition implements Condition {

    private Condition condition;
    private ComparisonType type;
    private int count;

    public CardsInTargetOpponentHandCondition() {
        this(ComparisonType.EQUAL_TO, 0);
    }

    public CardsInTargetOpponentHandCondition(ComparisonType type, int count) {
        this.type = type;
        this.count = count;
    }

    public CardsInTargetOpponentHandCondition(ComparisonType type, int count, Condition conditionToDecorate) {
        this(type, count);
        this.condition = conditionToDecorate;
    }

    @Override
    public boolean apply(Game game, Ability source) {
        boolean conditionApplies = false;
        Player opponent = game.getPlayer(source.getFirstTarget());
        if (opponent == null) {
            return false;
        }
        conditionApplies = ComparisonType.compare(opponent.getHand().size(), type, count);

        //If a decorated condition exists, check it as well and apply them together.
        if (this.condition != null) {
            conditionApplies = conditionApplies && this.condition.apply(game, source);
        }

        return conditionApplies;
    }
}
