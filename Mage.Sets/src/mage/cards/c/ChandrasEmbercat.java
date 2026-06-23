package mage.cards.c;

import mage.MageInt;
import mage.Mana;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.LogicalPredicate;
import mage.filter.predicate.typed.Spell.SpellPredicate;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class ChandrasEmbercat extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("an Elemental spell or a Chandra planeswalker spell")
            .add(SpellPredicate.instance)
            .add(LogicalPredicate.or(
                    SubType.ELEMENTAL.getPredicate(),
                    LogicalPredicate.and(
                            CardType.PLANESWALKER.getPredicate(),
                            SubType.CHANDRA.getPredicate()
                    )
            ));

    public ChandrasEmbercat(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{R}");

        this.subtype.add(SubType.ELEMENTAL);
        this.subtype.add(SubType.CAT);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // {T}: Add {R}. Spend this mana only to cast an Elemental spell or a Chandra planeswalker spell.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.RedMana(1))
                .condition(new FilteredSpellManaCondition(filter))
                .ruleText("Add {R}. Spend this mana only to cast an Elemental spell or a Chandra planeswalker spell.")
                .build()
        );
    }

    private ChandrasEmbercat(final ChandrasEmbercat card) {
        super(card);
    }

    @Override
    public ChandrasEmbercat copy() {
        return new ChandrasEmbercat(this);
    }
}
