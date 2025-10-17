package mage.cards.b;

import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.hint.Hint;
import mage.abilities.hint.ValueHint;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ContinuousAffected;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.LanderToken;
import mage.watchers.common.PermanentsEnteredBattlefieldWatcher;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class BioengineeredFuture extends CardImpl {

    public BioengineeredFuture(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{1}{G}{G}");

        // When this enchantment enters, create a Lander token.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new CreateTokenEffect(new LanderToken())));

        // Each creature you control enters with an additional +1/+1 counter on it for each land that entered the battlefield under your control this turn.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(ContinuousAffected.STATIC_OR_DYNAMIC, CounterType.P1P1, BioengineeredFutureValue.instance)
                .setFilter(StaticFilters.FILTER_CONTROLLED_CREATURE))
                .addHint(BioengineeredFutureValue.getHint()), new PermanentsEnteredBattlefieldWatcher());
    }

    private BioengineeredFuture(final BioengineeredFuture card) {
        super(card);
    }

    @Override
    public BioengineeredFuture copy() {
        return new BioengineeredFuture(this);
    }
}

enum BioengineeredFutureValue implements DynamicValue {
    instance;

    private static final Hint hint = new ValueHint("Lands entered this turn", instance);

    public static Hint getHint() {
        return hint;
    }

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        PermanentsEnteredBattlefieldWatcher watcher = game.getState().getWatcher(PermanentsEnteredBattlefieldWatcher.class);
        if (watcher != null) {
            return (int) watcher.getThisTurnEnteringPermanents(sourceAbility.getControllerId()).stream()
                    .filter(Permanent::isLand).count();
        }
        return 0;
    }

    @Override
    public BioengineeredFutureValue copy() {
        return instance;
    }

    @Override
    public String getMessage() {
        return "land that entered the battlefield under your control this turn";
    }
}