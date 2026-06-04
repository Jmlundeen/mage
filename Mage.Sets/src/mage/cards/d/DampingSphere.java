package mage.cards.d;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.cost.SpellsCostIncreasingAllEffect;
import mage.abilities.effects.mana.ReplaceManaEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.TargetController;
import mage.filter.FilterCard;
import mage.game.Game;
import mage.util.CardUtil;
import mage.watchers.common.CastSpellLastTurnWatcher;

import java.util.UUID;

/**
 * @author L_J
 */
public final class DampingSphere extends CardImpl {

    public DampingSphere(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{2}");

        // If a land is tapped for two or more mana, it produces {C} instead of any other type and amount.
        this.addAbility(new SimpleStaticAbility(
                ReplaceManaEffect.produced(Duration.WhileOnBattlefield, Outcome.Neutral, ReplaceManaEffect.replaceAllProducedMana(Mana.ColorlessMana(1)))
                        .setProducedMatcher(context -> context.mana().count() > 1 && context.producerPermanent() != null && context.producerPermanent().isLand(context.game()))
                        .setText("If a land is tapped for two or more mana, it produces {C} instead of any other type and amount")
        ));

        // Each spell a player casts costs {1} more to cast for each other spell that player has cast this turn.
        this.addAbility(new SimpleStaticAbility(new DampingSphereIncreasementAllEffect()));
    }

    private DampingSphere(final DampingSphere card) {
        super(card);
    }

    @Override
    public DampingSphere copy() {
        return new DampingSphere(this);
    }
}

class DampingSphereIncreasementAllEffect extends SpellsCostIncreasingAllEffect {

    DampingSphereIncreasementAllEffect() {
        super(1, new FilterCard(), TargetController.ANY);
        this.staticText = "Each spell a player casts costs {1} more to cast for each other spell that player has cast this turn";
    }

    private DampingSphereIncreasementAllEffect(DampingSphereIncreasementAllEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source, Ability abilityToModify) {
        CastSpellLastTurnWatcher watcher = game.getState().getWatcher(CastSpellLastTurnWatcher.class);
        if (watcher != null) {
            int additionalCost = watcher.getAmountOfSpellsPlayerCastOnCurrentTurn(abilityToModify.getControllerId());
            CardUtil.increaseCost(abilityToModify, additionalCost);
            return true;
        }
        return false;
    }

    @Override
    public DampingSphereIncreasementAllEffect copy() {
        return new DampingSphereIncreasementAllEffect(this);
    }
}
