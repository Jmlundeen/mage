package mage.cards.k;

import mage.MageInt;
import mage.Mana;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredAbilityManaCondition;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.Filter;
import mage.filter.FilterTyped;
import mage.filter.StaticTypedFilters;
import mage.filter.predicate.typed.ability.type.ForetellAbilityPredicate;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class KarfellHarbinger extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("Foretell Ability")
            .add(ForetellAbilityPredicate.instance);

    public KarfellHarbinger(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}");

        this.subtype.add(SubType.ZOMBIE);
        this.subtype.add(SubType.WIZARD);
        this.power = new MageInt(1);
        this.toughness = new MageInt(3);

        // {T}: Add {U}. Spend this mana only to foretell a card from your hand or cast an instant or sorcery spell.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.BlueMana(1))
                .condition(new FilteredSpellManaCondition(StaticTypedFilters.AN_INSTANT_OR_SORCERY_SPELL))
                .condition(new FilteredAbilityManaCondition(filter))
                .comparisonScope(Filter.ComparisonScope.Any)
                .ruleText("Add {U}. Spend this mana only to foretell a card from your hand or cast an instant or sorcery spell.")
                .build()
        );
    }

    private KarfellHarbinger(final KarfellHarbinger card) {
        super(card);
    }

    @Override
    public KarfellHarbinger copy() {
        return new KarfellHarbinger(this);
    }
}
