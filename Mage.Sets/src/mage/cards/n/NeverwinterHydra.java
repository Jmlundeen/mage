package mage.cards.n;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.TrampleAbility;
import mage.abilities.keyword.WardAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.Counter;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class NeverwinterHydra extends CardImpl {

    public NeverwinterHydra(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{X}{X}{G}{G}");

        this.subtype.add(SubType.HYDRA);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // As Neverwinter Hydra enters the battlefield, roll X d6. It enters with a number of +1/+1 counters on it equal to the total of those results.
        this.addAbility(new SimpleStaticAbility(new NeverwinterHydraEffect()));

        // Trample
        this.addAbility(TrampleAbility.getInstance());

        // Ward {4}
        this.addAbility(new WardAbility(new ManaCostsImpl<>("{4}")));
    }

    private NeverwinterHydra(final NeverwinterHydra card) {
        super(card);
    }

    @Override
    public NeverwinterHydra copy() {
        return new NeverwinterHydra(this);
    }
}

// TODO: allow for roll dice effect -> remember result -> use dynamic value for result. So no custom effect needed.
class NeverwinterHydraEffect extends EntersWithCountersEffect {

    NeverwinterHydraEffect() {
        super((Counter) null);
        staticText = "as {this} enters, roll X d6. It enters with a number of +1/+1 counters on it equal to the total of those results";
    }

    private NeverwinterHydraEffect(final NeverwinterHydraEffect effect) {
        super(effect);
    }

    @Override
    public NeverwinterHydraEffect copy() {
        return new NeverwinterHydraEffect(this);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Permanent permanent = game.getPermanentEntering(source.getSourceId());
        Player player = game.getPlayer(source.getControllerId());
        if (permanent != null && player != null) {
            int xValue = CardUtil.getSourceCostsTag(game, source, "X", 0);
            if (xValue > 0) {
                int amount = player.rollDice(outcome, source, game, 6, xValue, 0).stream().mapToInt(x -> x).sum();
                game.addEnterWithCounters(permanent.getId(), CounterType.P1P1.createInstance(amount));
            }
        }
        return false;
    }
}
