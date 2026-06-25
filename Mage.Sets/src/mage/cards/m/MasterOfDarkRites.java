package mage.cards.m;

import mage.MageInt;
import mage.Mana;
import mage.abilities.costs.common.SacrificeTargetCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.filter.StaticFilters;
import mage.filter.predicate.typed.LogicalPredicate;
import mage.filter.predicate.typed.Spell.SpellPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;

import java.util.UUID;

/**
 * @author arcox
 */
public final class MasterOfDarkRites extends CardImpl {
    private static final FilterTyped filter = new FilterTyped("Vampire, Cleric, and/or Demon spells")
            .addAll(
                    SpellPredicate.instance,
                    LogicalPredicate.or(
                    IMageObjectPredicate.getOSPPredicate(SubType.VAMPIRE.getPredicate()),
                    IMageObjectPredicate.getOSPPredicate(SubType.CLERIC.getPredicate()),
                    IMageObjectPredicate.getOSPPredicate(SubType.DEMON.getPredicate())
            ));

    public MasterOfDarkRites(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{B}");

        this.subtype.add(SubType.VAMPIRE);
        this.subtype.add(SubType.CLERIC);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // {T}, Sacrifice another creature: Add {B}{B}{B}. Spend this mana only to cast Vampire, Cleric, and/or Demon spells.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .cost(new SacrificeTargetCost(StaticFilters.FILTER_CONTROLLED_ANOTHER_CREATURE))
                .addStatic(Mana.BlackMana(3))
                .condition(new FilteredSpellManaCondition(filter))
                .ruleText("Add {B}{B}{B}. Spend this mana only to cast Vampire, Cleric, and/or Demon spells.")
                .build()
        );
    }

    private MasterOfDarkRites(final MasterOfDarkRites card) {
        super(card);
    }

    @Override
    public MasterOfDarkRites copy() {
        return new MasterOfDarkRites(this);
    }
}
