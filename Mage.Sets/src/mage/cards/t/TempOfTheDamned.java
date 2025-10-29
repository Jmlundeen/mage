
package mage.cards.t;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DoIfCostPaid;
import mage.abilities.effects.common.SacrificeSourceEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.UUID;

/**
 *
 * @author L_J
 */
public final class TempOfTheDamned extends CardImpl {

    public TempOfTheDamned(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{2}{B}");
        this.subtype.add(SubType.ZOMBIE);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // As Temp of the Damned enters the battlefield, roll a six-sided die. Temp of the Damned enters the battlefield with a number of funk counters on it equal to the result.
        this.addAbility(new AsEntersBattlefieldAbility(new TempOfTheDamnedEffect()));
        
        // At the beginning of your upkeep, remove a funk counter from Temp of the Damned. If you can't, sacrifice it.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(new DoIfCostPaid(
                null,
                new SacrificeSourceEffect(),
                new RemoveCountersSourceCost(CounterType.FUNK.createInstance()),
                "Remove a funk counter from {this}? If you can't, sacrifice it.",
                false)
                .setText("remove a funk counter from {this}. If you can't, sacrifice it")
        ));
    }

    private TempOfTheDamned(final TempOfTheDamned card) {
        super(card);
    }

    @Override
    public TempOfTheDamned copy() {
        return new TempOfTheDamned(this);
    }
}

// TODO: allow for roll dice effect -> remember result -> use dynamic value for result. So no custom effect needed.
class TempOfTheDamnedEffect extends OneShotEffect {

    TempOfTheDamnedEffect() {
        super(Outcome.Neutral);
        staticText = "roll a six-sided die. {this} enters with a number of funk counters on it equal to the result";
    }

    private TempOfTheDamnedEffect(final TempOfTheDamnedEffect effect) {
        super(effect);
    }

    @Override
    public TempOfTheDamnedEffect copy() {
        return new TempOfTheDamnedEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanentEntering(source.getSourceId());
        Player controller = game.getPlayer(source.getControllerId());
        if (permanent != null && controller != null) {
            int amount = controller.rollDice(outcome, source, game, 6);
            game.addEnterWithCounters(permanent.getId(), CounterType.FUNK.createInstance(amount));
        }
        return false;
    }
}
