package mage.cards.h;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.ReachAbility;
import mage.abilities.keyword.TrampleAbility;
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
 * @author spjspj
 */
public final class Hydradoodle extends CardImpl {

    public Hydradoodle(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{X}{X}{G}{G}");

        this.subtype.add(SubType.HYDRA);
        this.subtype.add(SubType.DOG);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // As Hydradoodle enters the battlefield, roll X six-sided dice. Hydradoodle enters the battlefield with a number of +1/+1 counters on it equal to the total of those results.
        this.addAbility(new SimpleStaticAbility(new HydradoodleEffect()));

        // Reach
        this.addAbility(ReachAbility.getInstance());

        // Trample
        this.addAbility(TrampleAbility.getInstance());
    }

    private Hydradoodle(final Hydradoodle card) {
        super(card);
    }

    @Override
    public Hydradoodle copy() {
        return new Hydradoodle(this);
    }
}

// TODO: allow for roll dice effect -> remember result -> use dynamic value for result. So no custom effect needed.
class HydradoodleEffect extends EntersWithCountersEffect {

    HydradoodleEffect() {
        super((Counter) null);
        this.staticText = "as {this} enters, roll X six-sided dice. {this} enters with a number of +1/+1 counters on it equal to the total of those results";
    }

    private HydradoodleEffect(final HydradoodleEffect effect) {
        super(effect);
    }

    @Override
    public HydradoodleEffect copy() {
        return new HydradoodleEffect(this);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Permanent permanent = game.getPermanentEntering(source.getSourceId());
        Player controller = game.getPlayer(source.getControllerId());
        if (permanent != null && controller != null) {
            int amount = CardUtil.getSourceCostsTag(game, source, "X", 0);
            if (amount > 0) {
                int total = controller.rollDice(outcome, source, game, 6, amount, 0).stream().mapToInt(x -> x).sum();
                game.addEnterWithCounters(permanent.getId(), CounterType.P1P1.createInstance(total));
            }
        }
        return false;
    }
}
