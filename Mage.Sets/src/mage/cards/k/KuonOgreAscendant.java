package mage.cards.k;

import mage.abilities.Ability;
import mage.abilities.condition.Condition;
import mage.abilities.effects.common.FlipSourceEffect;
import mage.abilities.effects.common.SacrificeEffect;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.CardSetInfo;
import mage.cards.FlipCard;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.TargetController;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.watchers.common.CreaturesDiedWatcher;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class KuonOgreAscendant extends FlipCard {

    public KuonOgreAscendant(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.OGRE, SubType.MONK}, "{B}{B}{B}",
                "Kuon's Essence",
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.ENCHANTMENT}, new SubType[]{});

        // Kuon, Ogre Ascendant
        this.getLeftHalfCard().setPT(2, 4);

        // At the beginning of the end step, if three or more creatures died this turn, flip Kuon, Ogre Ascendant.
        this.getLeftHalfCard().addAbility(new BeginningOfEndStepTriggeredAbility(
                TargetController.NEXT, new FlipSourceEffect(),
                false, KuonOgreAscendantCondition.instance));

        // Kuon's Essence
        // At the beginning of each player's upkeep, that player sacrifices a creature.
        this.getRightHalfCard().addAbility(new BeginningOfUpkeepTriggeredAbility(
                TargetController.ANY, new SacrificeEffect(StaticFilters.FILTER_PERMANENT_CREATURE, 1, "that player"),
                false));
    }

    private KuonOgreAscendant(final KuonOgreAscendant card) {
        super(card);
    }

    @Override
    public KuonOgreAscendant copy() {
        return new KuonOgreAscendant(this);
    }
}

enum KuonOgreAscendantCondition implements Condition {

    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        CreaturesDiedWatcher watcher = game.getState().getWatcher(CreaturesDiedWatcher.class);
        if (watcher != null) {
            return watcher.getAmountOfCreaturesDiedThisTurn() > 2;
        }
        return false;
    }

    @Override
    public String toString() {
        return "if three or more creatures died this turn";
    }

}
