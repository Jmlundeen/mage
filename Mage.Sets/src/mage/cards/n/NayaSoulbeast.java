
package mage.cards.n;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CastSourceTriggeredAbility;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.counters.CounterType;
import mage.game.Game;
import mage.players.Player;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class NayaSoulbeast extends CardImpl {

    public NayaSoulbeast(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{6}{G}{G}");
        this.subtype.add(SubType.BEAST);

        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Trample
        this.addAbility(TrampleAbility.getInstance());

        // When you cast Naya Soulbeast, each player reveals the top card of their library.
        Ability ability = new CastSourceTriggeredAbility(new NayaSoulbeastCastEffect(), false);

        // Naya Soulbeast enters the battlefield with X +1/+1 counters on it, where X is the total converted mana cost of all cards revealed this way.
        ability.addEffect(new EntersWithCountersEffect(Duration.OneUse, ContinuousAffected.SOURCE, CounterType.P1P1, NayaSoulbeastValue.instance)
                        .withXText()
        );
        this.addAbility(ability);
    }

    private NayaSoulbeast(final NayaSoulbeast card) {
        super(card);
    }

    @Override
    public NayaSoulbeast copy() {
        return new NayaSoulbeast(this);
    }
}

class NayaSoulbeastCastEffect extends OneShotEffect {

    NayaSoulbeastCastEffect() {
        super(Outcome.Benefit);
        this.staticText = "each player reveals the top card of their library";
    }

    private NayaSoulbeastCastEffect(final NayaSoulbeastCastEffect effect) {
        super(effect);
    }

    @Override
    public NayaSoulbeastCastEffect copy() {
        return new NayaSoulbeastCastEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        MageObject sourceObject = source.getSourceObject(game);
        if (controller != null && sourceObject != null) {
            int cmc = 0;
            for (UUID playerId : game.getState().getPlayersInRange(controller.getId(), game)) {
                Player player = game.getPlayer(playerId);
                if (player != null) {
                    if (player.getLibrary().hasCards()) {
                        Card card = player.getLibrary().getFromTop(game);
                        cmc += card.getManaValue();
                        player.revealCards(sourceObject.getName() + " (" + player.getName() + ')', new CardsImpl(card), game);
                    }
                }
            }
            for (Effect effect : source.getEffects()) {
                if (effect instanceof EntersWithCountersEffect) {
                    effect.setValue("NayaSoulbeastCounters", cmc);
                }
            }
            return true;
        }
        return false;
    }
}

enum NayaSoulbeastValue implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        Object object = effect.getValue("NayaSoulbeastCounters");
        if (object instanceof Integer) {
            return (Integer) object;
        }
        return 0;
    }

    @Override
    public NayaSoulbeastValue copy() {
        return instance;
    }

    @Override
    public String toString() {
        return "X";
    }

    @Override
    public String getMessage() {
        return "the total mana value of all cards revealed this way";
    }
}
