package mage.cards.e;

import mage.abilities.dynamicvalue.common.manavalue.CounteredManaValue;
import mage.abilities.effects.common.counter.AddCountersEffect;
import mage.abilities.effects.common.countered.CounterEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.TargetController;
import mage.counters.CounterType;
import mage.filter.StaticTypedFilters;
import mage.target.TargetGeneric;

import java.util.UUID;

/**
 * @author Cguy7777
 */
public final class Electrosiphon extends CardImpl {

    public Electrosiphon(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{U}{U}{R}");

        // Counter target spell. You get an amount of {E} equal to its mana value.
        this.getSpellAbility().addTarget(new TargetGeneric(StaticTypedFilters.SPELL));
        this.getSpellAbility().addEffect(new CounterEffect()
                .setText("counter target spell")
                .setRememberManaValue(true)
        );
        this.getSpellAbility().addEffect(new AddCountersEffect(CounterType.ENERGY, CounteredManaValue.instance, TargetController.YOU)
                .setText("You get an amount of {E} <i>(energy counters)</i> equal to its mana value"));
    }

    private Electrosiphon(final Electrosiphon card) {
        super(card);
    }

    @Override
    public Electrosiphon copy() {
        return new Electrosiphon(this);
    }
}
