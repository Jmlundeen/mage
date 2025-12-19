
package mage.cards.j;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.Counter;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.UUID;

/**
 *
 * @author spjspj
 */
public final class JumboImp extends CardImpl {

    public JumboImp(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}");

        this.subtype.add(SubType.IMP);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        //  Flying
        this.addAbility(FlyingAbility.getInstance());

        // As Jumbo Imp enters the battlefield, roll a six-sided die. Jumbo Imp enters the battlefield with a number of +1/+1 counters on it equal to the result.
        this.addAbility(new SimpleStaticAbility(new JumboImpEffect(null)));

        // At the beginning of your upkeep, roll a six-sided die and put a number of +1/+1 counters on Jumbo Imp equal to the result. 
        Ability ability2 = new BeginningOfUpkeepTriggeredAbility(new JumboImpAddCountersEffect());
        this.addAbility(ability2);

        // At the beginning of your end step, roll a six-sided die and remove a number of +1/+1 counters from Jumbo Imp equal to the result.
        Ability ability3 = new BeginningOfEndStepTriggeredAbility(TargetController.YOU, new JumboImpRemoveCountersEffect(), false, null);
        this.addAbility(ability3);
    }

    private JumboImp(final JumboImp card) {
        super(card);
    }

    @Override
    public JumboImp copy() {
        return new JumboImp(this);
    }
}

// TODO: allow for roll dice effect -> remember result -> use dynamic value for result. So no custom effect needed.
class JumboImpEffect extends EntersWithCountersEffect {

    JumboImpEffect(Counter counter) {
        super(counter);
        staticText = "as {this} enters, roll a six-sided die. {this} enters with a number of +1/+1 counters on it equal to the result";
    }

    private JumboImpEffect(JumboImpEffect effect) {
        super(effect);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent permanent = game.getPermanentEntering(source.getSourceId());
        if (controller != null && permanent != null) {
            int amount = controller.rollDice(outcome, source, game, 6);
            game.addEnterWithCounters(permanent.getId(), CounterType.P1P1.createInstance(amount));
        }
        return false;
    }

    @Override
    public JumboImpEffect copy() {
        return new JumboImpEffect(this);
    }

}

class JumboImpAddCountersEffect extends OneShotEffect {

    JumboImpAddCountersEffect() {
        super(Outcome.Benefit);
        this.staticText = "roll a six-sided die and put a number of +1/+1 counters on {this} equal to the result";
    }

    private JumboImpAddCountersEffect(final JumboImpAddCountersEffect effect) {
        super(effect);
    }

    @Override
    public JumboImpAddCountersEffect copy() {
        return new JumboImpAddCountersEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent permanent = game.getPermanent(source.getSourceId());
        if (controller != null && permanent != null) {
            int amount = controller.rollDice(outcome, source, game, 6);
            permanent.addCounters(CounterType.P1P1.createInstance(amount), source.getControllerId(), source, game);
            return true;
        }
        return false;
    }
}

class JumboImpRemoveCountersEffect extends OneShotEffect {

    JumboImpRemoveCountersEffect() {
        super(Outcome.Detriment);
        this.staticText = "roll a six-sided die and remove a number of +1/+1 counters from {this} equal to the result";
    }

    private JumboImpRemoveCountersEffect(final JumboImpRemoveCountersEffect effect) {
        super(effect);
    }

    @Override
    public JumboImpRemoveCountersEffect copy() {
        return new JumboImpRemoveCountersEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent permanent = game.getPermanent(source.getSourceId());
        if (controller != null && permanent != null) {
            int amount = controller.rollDice(outcome, source, game, 6);
            permanent.removeCounters(CounterType.P1P1.createInstance(amount), source, game);
            return true;
        }
        return false;
    }
}
