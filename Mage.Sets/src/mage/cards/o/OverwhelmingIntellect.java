
package mage.cards.o;

import mage.abilities.dynamicvalue.common.manavalue.CounteredManaValue;
import mage.abilities.effects.common.countered.CounterEffect;
import mage.abilities.effects.common.draw.DrawCardEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.FilterTyped;
import mage.filter.StaticTypedFilters;
import mage.target.TargetGeneric;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class OverwhelmingIntellect extends CardImpl {

    public OverwhelmingIntellect(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.INSTANT},"{4}{U}{U}");

        // Counter target creature spell. Draw cards equal to that spell's mana value.
        FilterTyped filter = StaticTypedFilters.A_CREATURE_SPELL.copy();
        filter.setMessage("creature spell");
        this.getSpellAbility().addTarget(new TargetGeneric(filter));
        this.getSpellAbility().addEffect(new CounterEffect()
                .setText("counter target creature spell")
                .setRememberManaValue(true)
        );
        this.getSpellAbility().addEffect(new DrawCardEffect(CounteredManaValue.instance)
                .setText("Draw cards equal to that spell's mana value"));

    }

    private OverwhelmingIntellect(final OverwhelmingIntellect card) {
        super(card);
    }

    @Override
    public OverwhelmingIntellect copy() {
        return new OverwhelmingIntellect(this);
    }
}
