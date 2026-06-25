package mage.cards.b;

import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.keyword.FreerunningAbility;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.SpendOrActivateManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.Filter;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.LogicalPredicate;
import mage.filter.predicate.typed.Spell.ability.SpellHasAbilityPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;

import java.util.UUID;

/**
 * @author unknown
 */
public class BrotherhoodHeadquarters extends CardImpl {

    private static final FilterTyped spellFilter = new FilterTyped("an Assassin spell, activated ability of an Assassin, or a spell that has freerunning")
            .add(LogicalPredicate.or(
                    IMageObjectPredicate.getOSPPredicate(SubType.ASSASSIN.getPredicate()),
                    new SpellHasAbilityPredicate(FreerunningAbility.class)
            ));

    public BrotherhoodHeadquarters(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // {T}: Add {C}
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add one mana of any color. Spend this mana only to cast an Assassin spell or a spell that has freerunning, or to activate an ability of an Assassin source.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyColor(1)
                .condition(new SpendOrActivateManaCondition(spellFilter))
                .comparisonScope(Filter.ComparisonScope.All)
                .ruleText("Add one mana of any color. Spend this mana only to cast an Assassin spell or a spell that has freerunning, or to activate an ability of an Assassin source")
                .build()
        );
    }

    public BrotherhoodHeadquarters(BrotherhoodHeadquarters card) {
        super(card);
    }

    @Override
    public BrotherhoodHeadquarters copy() {
        return new BrotherhoodHeadquarters(this);
    }
}
