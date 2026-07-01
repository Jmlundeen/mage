package mage.cards.c;

import mage.abilities.dynamicvalue.common.manavalue.CounteredManaValue;
import mage.abilities.effects.common.cast.PlayEffect;
import mage.abilities.effects.common.countered.CounterEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ComparisonType;
import mage.constants.Zone;
import mage.filter.FilterTyped;
import mage.filter.StaticTypedFilters;
import mage.filter.predicate.typed.LogicalPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.filter.predicate.typed.mageObject.value.ManaValuePredicate;
import mage.target.TargetGeneric;

import java.util.Set;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class Counterpoint extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("a creature, instant, sorcery, or planeswalker spell with mana value less than countered spell")
            .addAll(
                    LogicalPredicate.or(
                            IMageObjectPredicate.getOSPPredicate(CardType.CREATURE.getPredicate()),
                            IMageObjectPredicate.getOSPPredicate(CardType.INSTANT.getPredicate()),
                            IMageObjectPredicate.getOSPPredicate(CardType.SORCERY.getPredicate()),
                            IMageObjectPredicate.getOSPPredicate(CardType.PLANESWALKER.getPredicate())
                    ),
                    new ManaValuePredicate(ComparisonType.OR_LESS, CounteredManaValue.instance)
            );

    public Counterpoint(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{3}{U}{B}");

        // Counter target spell. You may cast a creature, instant, sorcery, or planeswalker spell from your graveyard with mana value less than or equal to that spell's mana value without paying its mana cost.
        this.getSpellAbility().addEffect(new CounterEffect()
                .setText("counter target spell")
                .setRememberManaValue(true));
        this.getSpellAbility().addEffect(new PlayEffect(filter, Set.of(Zone.GRAVEYARD))
                .setOptional(true)
                .setText("you may cast a creature, instant, sorcery, or planeswalker spell from your graveyard with mana value less than or equal to that spell's mana value without paying its mana cost"));
        this.getSpellAbility().addTarget(new TargetGeneric(StaticTypedFilters.SPELL));
    }

    private Counterpoint(final Counterpoint card) {
        super(card);
    }

    @Override
    public Counterpoint copy() {
        return new Counterpoint(this);
    }
}
