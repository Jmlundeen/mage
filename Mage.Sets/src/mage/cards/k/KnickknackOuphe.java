package mage.cards.k;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.GetXValue;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.*;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.FilterCard;
import mage.filter.common.FilterEnchantmentCard;
import mage.filter.predicate.mageobject.ManaValuePredicate;
import mage.game.Game;
import mage.players.Player;
import mage.target.TargetCard;

import java.util.UUID;

/**
 *
 * @author Xanderhall
 */
public final class KnickknackOuphe extends CardImpl {

    public KnickknackOuphe(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{X}{G}");
        
        this.subtype.add(SubType.OUPHE);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // Knickknack Ouphe enters the battlefield with X +1/+1 counters on it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.P1P1, GetXValue.instance)));

        // When Knickknack Ouphe enters the battlefield, reveal the top X cards of your library. You may put any number of Aura cards with mana value X or less from among them onto the battlefield. Then put all cards revealed this way that weren't put onto the battlefield on the bottom of your library in a random order.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new KnickknackOuphePutOntoBattlefieldEffect()));

    }

    private KnickknackOuphe(final KnickknackOuphe card) {
        super(card);
    }

    @Override
    public KnickknackOuphe copy() {
        return new KnickknackOuphe(this);
    }
}

class KnickknackOuphePutOntoBattlefieldEffect extends OneShotEffect {

    KnickknackOuphePutOntoBattlefieldEffect() {
        super(Outcome.PutCardInPlay);
        staticText = "reveal the top X cards of your library. " + 
        "You may put any number of Aura cards with mana value X or less from among them onto the battlefield. " + 
        "Then put all cards revealed this way that weren't put onto the battlefield on the bottom of your library in a random order";
    }

    private KnickknackOuphePutOntoBattlefieldEffect(final KnickknackOuphePutOntoBattlefieldEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller != null) {
            int count = GetXValue.instance.calculate(game, source, null);
            if (count > 0) {
                Cards cards = new CardsImpl(controller.getLibrary().getTopCards(game, count));
                controller.revealCards(source, cards, game);

                FilterCard filter = new FilterEnchantmentCard("Aura cards with mana value " + count + " or less to put onto the battlefield");
                filter.add(SubType.AURA.getPredicate());
                filter.add(new ManaValuePredicate(ComparisonType.OR_LESS, count));
                
                if (cards.count(filter, controller.getId(), source, game) > 0) {
                    TargetCard targetAuras = new TargetCard(0, count, Zone.LIBRARY, filter);

                    if (controller.choose(Outcome.PutCardInPlay, cards, targetAuras, source, game)) {
                        targetAuras.getTargets().stream().forEach(t -> {
                            Card card = cards.get(t, game);
                            if (card != null) {
                                cards.remove(card);
                                controller.moveCards(card, Zone.BATTLEFIELD, source, game);
                            }
                        });

                        targetAuras.clearChosen();
                    } else {
                        game.informPlayers(controller.getLogName() + " didn't choose anything");
                    }
                } else {
                    game.informPlayers("No Aura cards with mana value " + count + " or less to choose.");
                }

                if (!cards.isEmpty()) {
                    PutCards.BOTTOM_RANDOM.moveCards(controller, cards, source, game);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public KnickknackOuphePutOntoBattlefieldEffect copy() {
        return new KnickknackOuphePutOntoBattlefieldEffect(this);
    }

}
