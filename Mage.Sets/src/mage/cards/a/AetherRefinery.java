package mage.cards.a;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.common.PayEnergyCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.counter.GetEnergyCountersControllerEffect;
import mage.abilities.effects.common.replacement.ReplaceCounterEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.permanent.token.AetherbornToken;
import mage.players.Player;

import java.util.UUID;

/**
 *
 * @author grimreap124
 */
public final class AetherRefinery extends CardImpl {

    public AetherRefinery(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[] { CardType.ARTIFACT }, "{4}{R}{R}");

        // If you would get one or more {E}, you get twice that many {E} instead.
        this.addAbility(new SimpleStaticAbility(new ReplaceCounterEffect(ReplaceCounterEffect.ModificationType.MULTIPLY, 2)
                .addValidCounterTypes(CounterType.ENERGY)
                .setText("If you would get one or more {E}, you get twice that many {E} instead")
        ));

        // {T}: You get {E}, then you may pay one or more {E}. If you do, create an X/X black Aetherborn creature token, where X is the amount of {E} paid this way.
        Ability ability = new SimpleActivatedAbility(new GetEnergyCountersControllerEffect(1), new TapSourceCost());
        ability.addEffect(new AetherRefineryTokenEffect());
        this.addAbility(ability);
    }

    private AetherRefinery(final AetherRefinery card) {
        super(card);
    }

    @Override
    public AetherRefinery copy() {
        return new AetherRefinery(this);
    }
}

class AetherRefineryTokenEffect extends OneShotEffect {

    AetherRefineryTokenEffect() {
        super(Outcome.Benefit);
        this.staticText = ", then you may pay one or more {E}. If you do, create an X/X black Aetherborn creature token, where X is the amount of {E} paid this way";
    }

    private AetherRefineryTokenEffect(final AetherRefineryTokenEffect effect) {
        super(effect);
    }

    @Override
    public AetherRefineryTokenEffect copy() {
        return new AetherRefineryTokenEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller != null) {
            int totalEnergy = controller.getCountersCount(CounterType.ENERGY);

            if (totalEnergy == 0) {
                return false;
            }

            if (!controller.chooseUse(this.getOutcome(),
                    "Pay 1 or more {E} to create X/X black Aetherborn creature token", source, game)) {
                return true;
            }
            int numberToPay = controller.getAmount(1, totalEnergy,
                    "Pay one or more {E}", source, game);

            Cost cost = new PayEnergyCost(numberToPay);
            if (cost.pay(source, game, source, source.getControllerId(), true)) {
                new CreateTokenEffect(new AetherbornToken(numberToPay, numberToPay)).apply(game, source);
            }
            return true;
        }
        return false;
    }
}
