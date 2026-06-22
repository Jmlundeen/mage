package mage.cards.i;

import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.ExileTopXMayPlayUntilEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.Spell.SpellCastFromZonePredicate;
import mage.filter.predicate.typed.Spell.SpellPredicate;

import java.util.UUID;

/**
 *
 * @author Jmlundeen
 */
public final class InterdimensionalWebWatch extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("spell from exile")
            .addAll(
                    SpellPredicate.instance,
                    SpellCastFromZonePredicate.EXILE
            );

    public InterdimensionalWebWatch(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{4}");
        

        // When this artifact enters, exile the top two cards of your library. Until the end of your next turn, you may play those cards.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new ExileTopXMayPlayUntilEffect(2, Duration.UntilEndOfYourNextTurn)));

        // {T}: Add two mana in any combination of colors. Spend this mana only to cast spells from exile.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyCombination(2)
                .condition(new FilteredSpellManaCondition(filter))
                .ruleText("Add two mana in any combination of colors. Spend this mana only to cast spells from exile")
                .build()
        );
    }

    private InterdimensionalWebWatch(final InterdimensionalWebWatch card) {
        super(card);
    }

    @Override
    public InterdimensionalWebWatch copy() {
        return new InterdimensionalWebWatch(this);
    }
}
