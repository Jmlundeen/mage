package mage.cards.g;

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
import mage.filter.FilterAbility;
import mage.filter.StaticTypedFilters;
import mage.filter.predicate.ability.ActivatedAbilityPredicate;

import java.util.UUID;

/**
 *
 * @author Jmlundeen
 */
public final class GuidelightOptimizer extends CardImpl {

    private static final FilterAbility activatedAbilityFilter = new FilterAbility("activated ability");

    static {
        activatedAbilityFilter.add(ActivatedAbilityPredicate.instance);
    }
    public GuidelightOptimizer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{1}{U}");
        
        this.subtype.add(SubType.ROBOT);
        this.power = new MageInt(2);
        this.toughness = new MageInt(1);

        // {T}: Add {U}. Spend this mana only to cast an artifact spell or activate an ability.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addStatic(Mana.BlueMana(1))
                .condition(new FilteredSpellManaCondition(StaticTypedFilters.AN_ARTIFACT_SPELL))
                .condition(new FilteredAbilityManaCondition(StaticTypedFilters.ACTIVATED_ABILITY))
                .comparisonScope(Filter.ComparisonScope.Any)
                .ruleText("Add {U}. Spend this mana only to cast an artifact spell or activate an ability")
                .build()
        );
    }

    private GuidelightOptimizer(final GuidelightOptimizer card) {
        super(card);
    }

    @Override
    public GuidelightOptimizer copy() {
        return new GuidelightOptimizer(this);
    }
}
