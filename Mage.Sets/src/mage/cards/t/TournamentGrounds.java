package mage.cards.t;

import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ManaType;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.LogicalPredicate;
import mage.filter.predicate.typed.Spell.SpellPredicate;

import java.util.Set;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class TournamentGrounds extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("a Knight or Equipment spell")
            .addAll(
                    SpellPredicate.instance,
                    LogicalPredicate.or(
                            SubType.KNIGHT.getPredicate(),
                            SubType.EQUIPMENT.getPredicate()
                    )
            );

    public TournamentGrounds(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add {R}, {W}, or {B}. Spend this mana only to cast a Knight or Equipment spell.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addChoice(Set.of(ManaType.RED, ManaType.WHITE, ManaType.BLACK), 1)
                .condition(new FilteredSpellManaCondition(filter))
                .ruleText("Add {R}, {W}, or {B}. Spend this mana only to cast a Knight or Equipment spell")
                .build()
        );
    }

    private TournamentGrounds(final TournamentGrounds card) {
        super(card);
    }

    @Override
    public TournamentGrounds copy() {
        return new TournamentGrounds(this);
    }
}
