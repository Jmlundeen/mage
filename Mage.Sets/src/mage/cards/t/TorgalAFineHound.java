package mage.cards.t;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.condition.Condition;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.PermanentsOnBattlefieldCount;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.hint.Hint;
import mage.abilities.hint.ValueHint;
import mage.abilities.mana.AnyColorManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.filter.FilterSpell;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.common.FilterCreatureSpell;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.stack.Spell;
import mage.util.CardUtil;
import mage.watchers.Watcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class TorgalAFineHound extends CardImpl {

    private static final FilterSpell filter = new FilterCreatureSpell("your first Human creature spell each turn");
    private static final FilterPermanent dogWolfFilter = new FilterControlledPermanent("Dog and/or Wolf you control");
    private static final DynamicValue xValue = new PermanentsOnBattlefieldCount(dogWolfFilter);
    private static final Hint hint = new ValueHint("Dogs and Wolves you control", xValue);

    static {
        filter.add(SubType.HUMAN.getPredicate());
        dogWolfFilter.add(Predicates.or(
                SubType.DOG.getPredicate(),
                SubType.WOLF.getPredicate()
        ));
    }

    public TorgalAFineHound(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.WOLF);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Whenever you cast your first Human creature spell each turn, that creature enters with an additional +1/+1 counter on it for each Dog and/or Wolf you control.
        this.addAbility(new SpellCastControllerTriggeredAbility(
                new EntersWithCountersEffect(Duration.EndOfTurn, ContinuousAffected.STATIC_OR_DYNAMIC, CounterType.P1P1, xValue),
                filter,
                false,
                SetTargetPointer.CARD)
                .withTriggerCondition(TorgalAFineHoundCondition.instance)
                .addHint(hint),
                new TorgalAFineHoundWatcher()
        );

        // {T}: Add one mana of any color.
        this.addAbility(new AnyColorManaAbility());
    }

    private TorgalAFineHound(final TorgalAFineHound card) {
        super(card);
    }

    @Override
    public TorgalAFineHound copy() {
        return new TorgalAFineHound(this);
    }
}

enum TorgalAFineHoundCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        return TorgalAFineHoundWatcher.check(game, source);
    }

    @Override
    public String toString() {
        return "";
    }
}

class TorgalAFineHoundWatcher extends Watcher {

    private final Map<UUID, Integer> map = new HashMap<>();

    TorgalAFineHoundWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() != GameEvent.EventType.SPELL_CAST) {
            return;
        }
        Optional.ofNullable(event)
                .map(GameEvent::getTargetId)
                .map(game::getSpell)
                .filter(spell -> spell.isCreature(game) && spell.hasSubtype(SubType.HUMAN, game))
                .map(Spell::getControllerId)
                .ifPresent(playerId -> map.compute(playerId, CardUtil::setOrIncrementValue));
    }

    @Override
    public void reset() {
        super.reset();
        map.clear();
    }

    static boolean check(Game game, Ability source) {
        return game
                .getState()
                .getWatcher(TorgalAFineHoundWatcher.class)
                .map
                .getOrDefault(source.getControllerId(), 0) < 2;
    }
}
