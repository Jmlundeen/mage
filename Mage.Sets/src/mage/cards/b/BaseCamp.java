package mage.cards.b;

import mage.abilities.common.EntersBattlefieldTappedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredAbilityManaCondition;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.Filter;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.LogicalPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class BaseCamp extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("a Cleric, Rogue, Warrior, or Wizard")
            .add(
                    LogicalPredicate.or(
                            IMageObjectPredicate.getOSPPredicate(SubType.CLERIC.getPredicate()),
                            IMageObjectPredicate.getOSPPredicate(SubType.ROGUE.getPredicate()),
                            IMageObjectPredicate.getOSPPredicate(SubType.WARRIOR.getPredicate()),
                            IMageObjectPredicate.getOSPPredicate(SubType.WIZARD.getPredicate())
                    )
            );

    public BaseCamp(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // Base Camp enters the battlefield tapped.
        this.addAbility(new EntersBattlefieldTappedAbility());

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add one mana of any color. Spend this mana only to cast a Cleric, Rogue, Warrior, or Wizard spell or to activate an ability of a Cleric, Rogue, Warrior, or Wizard.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyColor(1)
                .condition(new FilteredSpellManaCondition(filter))
                .condition(new FilteredAbilityManaCondition(filter))
                .comparisonScope(Filter.ComparisonScope.Any)
                .ruleText("add one mana of any color. Spend this mana only to cast a Cleric, Rogue, Warrior, or Wizard spell or to activate an ability of a Cleric, Rogue, Warrior, or Wizard")
                .build()
        );
    }

    private BaseCamp(final BaseCamp card) {
        super(card);
    }

    @Override
    public BaseCamp copy() {
        return new BaseCamp(this);
    }
}

