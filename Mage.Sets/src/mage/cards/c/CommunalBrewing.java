package mage.cards.c;

import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SetTargetPointer;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetOpponent;

import java.util.UUID;

/**
 * @author PurpleCrowbar
 */
public final class CommunalBrewing extends CardImpl {

    private static final DynamicValue xvalue = new CountersSourceCount(CounterType.INGREDIENT);

    public CommunalBrewing(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{G}");

        // When Communal Brewing enters, any number of target opponents each draw a card. Put an ingredient
        // counter on Communal Brewing, then put an ingredient counter on it for each card drawn this way.
        Ability ability = new EntersBattlefieldTriggeredAbility(new CommunalBrewingEffect());
        ability.addTarget(new TargetOpponent(0, Integer.MAX_VALUE, false));
        this.addAbility(ability);

        // Whenever you cast a creature spell, that creature enters with X additional +1/+1
        // counters on it, where X is the number of ingredient counters on Communal Brewing.
        this.addAbility(new SpellCastControllerTriggeredAbility(
                new EntersWithCountersEffect(CounterType.P1P1, xvalue)
                        .setText("that creature enters with X additional +1/+1 counters on it, " +
                                "where X is the number of ingredient counters on {this}"),
                StaticFilters.FILTER_SPELL_A_CREATURE,
                false, SetTargetPointer.SPELL
        ));
    }

    private CommunalBrewing(final CommunalBrewing card) {
        super(card);
    }

    @Override
    public CommunalBrewing copy() {
        return new CommunalBrewing(this);
    }
}

class CommunalBrewingEffect extends OneShotEffect {

    CommunalBrewingEffect() {
        super(Outcome.Benefit);
        staticText = "any number of target opponents each draw a card. Put an ingredient counter " +
                "on {this}, then put an ingredient counter on it for each card drawn this way";
    }

    private CommunalBrewingEffect(final CommunalBrewingEffect effect) {
        super(effect);
    }

    @Override
    public CommunalBrewingEffect copy() {
        return new CommunalBrewingEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        int count = 0;
        for (UUID targetId : getTargetPointer().getTargets(game, source)) {
            Player opponent = game.getPlayer(targetId);
            if (opponent == null) {
                continue;
            }
            count += opponent.drawCards(1, source, game); // Known issue with Teferi's Ageless Insight. See #12616
        }
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent == null) {
            return false;
        }
        return permanent.addCounters(CounterType.INGREDIENT.createInstance(count + 1), source, game);
    }
}
