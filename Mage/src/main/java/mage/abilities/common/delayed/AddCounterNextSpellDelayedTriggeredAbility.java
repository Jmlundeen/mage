package mage.abilities.common.delayed;

import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.constants.ContinuousAffected;
import mage.constants.Duration;
import mage.counters.CounterType;
import mage.filter.FilterSpell;
import mage.filter.StaticFilters;
import mage.game.stack.Spell;
import mage.util.CardUtil;

/**
 * @author TheElk801
 */
public class AddCounterNextSpellDelayedTriggeredAbility extends CastNextSpellDelayedTriggeredAbility {

    public AddCounterNextSpellDelayedTriggeredAbility() {
        this(StaticFilters.FILTER_SPELL_A_CREATURE);
    }

    public AddCounterNextSpellDelayedTriggeredAbility(FilterSpell filter) {
        this(1, filter);
    }

    public AddCounterNextSpellDelayedTriggeredAbility(int amount, FilterSpell filter) {
        super(new EntersWithCountersEffect(Duration.EndOfTurn, ContinuousAffected.STATIC_OR_DYNAMIC, CounterType.P1P1.createInstance(amount))
                .withEventCondition((event, source, game, effect) -> {
                    Spell spell = (Spell) effect.getValue("spellCast");
                    return spell != null && event.getTargetId().equals(spell.getCard().getId());
                })
                .setText("that creature enters with " + CardUtil.numberToText(amount, "an") +
                        " additional +1/+1 counter" + (amount > 1 ? "s" : "") + " on it"),
                filter,
                true
        );
    }

    private AddCounterNextSpellDelayedTriggeredAbility(final AddCounterNextSpellDelayedTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public AddCounterNextSpellDelayedTriggeredAbility copy() {
        return new AddCounterNextSpellDelayedTriggeredAbility(this);
    }
}
