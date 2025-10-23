package mage.cards.a;

import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldControlledTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.token.ClueAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.choices.Choice;
import mage.choices.ChoiceImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Alex-Vasile
 */
public class AgentsToolkit extends CardImpl {
    public AgentsToolkit(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{1}{G}{U}");

        this.subtype.add(SubType.CLUE);

        // Agent’s Toolkit enters the battlefield with a +1/+1 counter, a flying counter, a deathtouch counter, and a shield counter on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.P1P1.createInstance())
                .withAdditionalCounters(CounterType.FLYING.createInstance())
                .withAdditionalCounters(CounterType.DEATHTOUCH.createInstance())
                .withAdditionalCounters(CounterType.SHIELD.createInstance())
        ));

        // Whenever a creature you control enters,
        // you may move a counter from Agent’s Toolkit onto that creature.
        this.addAbility(new EntersBattlefieldControlledTriggeredAbility(
                new AgentToolkitMoveCounterEffect(),
                StaticFilters.FILTER_PERMANENT_A_CREATURE
        ));

        // {2}, Sacrifice Agent’s Toolkit: Draw a card.
        this.addAbility(new ClueAbility(true));
    }

    private AgentsToolkit(final AgentsToolkit card) {
        super(card);
    }

    @Override
    public AgentsToolkit copy() {
        return new AgentsToolkit(this);
    }
}

class AgentToolkitMoveCounterEffect extends OneShotEffect {

    AgentToolkitMoveCounterEffect() {
        super(Outcome.Benefit);
        this.staticText = "you may move a counter from {this} onto that creature";
    }

    private AgentToolkitMoveCounterEffect(final AgentToolkitMoveCounterEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent agentsToolkitPermanent = game.getPermanent(source.getSourceId());
        Player controller = game.getPlayer(source.getControllerId());
        if (agentsToolkitPermanent == null || controller == null) {
            return false;
        }
        Object enteringObject = this.getValue("permanentEnteringBattlefield");
        if (!(enteringObject instanceof Permanent)) {
            return false;
        }
        Permanent enteringCreature = (Permanent) enteringObject;

        Set<String> possibleCounterNames = new LinkedHashSet<>(agentsToolkitPermanent.getCounters(game).keySet());
        if (possibleCounterNames.isEmpty()) {
            return false;
        }

        Choice moveCounterChoice = new ChoiceImpl(false);
        moveCounterChoice.setMessage("Choose counter to move");
        moveCounterChoice.setChoices(possibleCounterNames);

        if (controller.choose(outcome, moveCounterChoice, game) && possibleCounterNames.contains(moveCounterChoice.getChoice())) {
            String counterName = moveCounterChoice.getChoice();
            CounterType counterType = CounterType.findByName(counterName);
            if (counterType == null) {
                return false;
            }
            agentsToolkitPermanent.removeCounters(counterType.getName(), 1, source, game);
            enteringCreature.addCounters(counterType.createInstance(), source, game);
        }
        return true;
    }

    @Override
    public AgentToolkitMoveCounterEffect copy() {
        return new AgentToolkitMoveCounterEffect(this);
    }
}
