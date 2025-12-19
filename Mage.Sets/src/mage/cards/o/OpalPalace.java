package mage.cards.o;

import mage.abilities.Ability;
import mage.abilities.AbilityImpl;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.CommanderColorIdentityManaAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.stack.Spell;
import mage.players.Player;
import mage.watchers.Watcher;
import mage.watchers.common.CommanderPlaysCountWatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author LevelX2
 */
public final class OpalPalace extends CardImpl {

    public OpalPalace(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());
        // {1}, {tap}: Add one mana of any color in your commander's color identity. If you spend this mana to cast your commander, it enters with a number of +1/+1 counters on it equal to the number of times it's been cast from the command zone this game.
        AbilityImpl ability = new CommanderColorIdentityManaAbility(new GenericManaCost(1));
        ability.appendToRule(" If you spend this mana to cast your commander, it enters with a number of additional +1/+1 counters on it " +
                "equal to the number of times it's been cast from the command zone this game.");
        ability.addCost(new TapSourceCost());
        this.addAbility(ability, new OpalPalaceWatcher(ability.getOriginalId().toString()));
    }

    private OpalPalace(final OpalPalace card) {
        super(card);
    }

    @Override
    public OpalPalace copy() {
        return new OpalPalace(this);
    }
}

class OpalPalaceWatcher extends Watcher {

    private final List<UUID> commanderPartsId = new ArrayList<>();
    private final String originalId;

    public OpalPalaceWatcher(String originalId) {
        super(WatcherScope.CARD);
        this.originalId = originalId;
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.MANA_PAID) {
            if (event.getData() != null && event.getData().equals(originalId)) {
                Spell spell = game.getStack().getSpell(event.getTargetId());
                if (spell != null) {
                    Card card = spell.getCard();
                    if (card != null) {
                        for (UUID playerId : game.getPlayerList()) {
                            Player player = game.getPlayer(playerId);
                            if (player != null) {
                                // need check all card parts (example: mdf cards)
                                if (game.getCommandersIds(player, CommanderCardType.COMMANDER_OR_OATHBREAKER, true).contains(card.getId())) {
                                    game.addEffect(new EntersWithCountersEffect(Duration.OneUse, ContinuousAffected.SOURCE, CounterType.P1P1, OpalPalaceValue.instance),
                                            spell.getSpellAbility());
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void reset() {
        super.reset();
        commanderPartsId.clear();
    }
}

enum OpalPalaceValue implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        CommanderPlaysCountWatcher watcher = game.getState().getWatcher(CommanderPlaysCountWatcher.class);
        return watcher.getPlaysCount(sourceAbility.getSourceId());
    }

    @Override
    public OpalPalaceValue copy() {
        return instance;
    }

    @Override
    public String getMessage() {
        return "number of times it's been cast from the command zone this game";
    }
}