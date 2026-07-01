
package mage.cards.i;

import mage.abilities.condition.common.ManaWasSpentCondition;
import mage.abilities.decorator.ConditionalOneShotEffect;
import mage.abilities.dynamicvalue.common.manavalue.CounteredManaValue;
import mage.abilities.effects.common.countered.CounterEffect;
import mage.abilities.effects.common.mill.MillCardsEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.StaticTypedFilters;
import mage.target.TargetGeneric;

import java.util.UUID;

/**
 *
 * @author escplan9 (Derek Monturo - dmontur1 at gmail dot com)
 */
public final class InduceParanoia extends CardImpl {

    public InduceParanoia(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.INSTANT},"{2}{U}{U}");
        
        // Counter target spell. If {B} was spent to cast Induce Paranoia, that spell's controller puts the top X cards of their library into their graveyard, where X is the spell's converted mana cost.
        this.getSpellAbility().addEffect(new CounterEffect()
                .setText("counter target spell")
                .setRememberController(true)
                .setRememberManaValue(true));
        this.getSpellAbility().addEffect(new ConditionalOneShotEffect(
                new MillCardsEffect(CounteredManaValue.instance),
                null,
                ManaWasSpentCondition.BLACK,
                "If {B} was spent to cast this spell, that spell's controller mills X cards, where X is the spell's mana value.")
        );
        this.getSpellAbility().addTarget(new TargetGeneric(StaticTypedFilters.SPELL));
    }

    private InduceParanoia(final InduceParanoia card) {
        super(card);
    }

    @Override
    public InduceParanoia copy() {
        return new InduceParanoia(this);
    }
}
