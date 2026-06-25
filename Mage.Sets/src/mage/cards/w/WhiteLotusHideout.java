package mage.cards.w;

import mage.abilities.Ability;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.mana.AnyColorManaAbility;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.LogicalPredicate;
import mage.filter.predicate.typed.Spell.SpellPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class WhiteLotusHideout extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("a Lesson or Shrine spell")
            .addAll(
                    SpellPredicate.instance,
                    LogicalPredicate.or(
                            IMageObjectPredicate.getOSPPredicate(SubType.LESSON.getPredicate()),
                            IMageObjectPredicate.getOSPPredicate(SubType.SHRINE.getPredicate())
                    )
            );

    public WhiteLotusHideout(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add one mana of any color. Spend this mana only to cast a Lesson or Shrine spell.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyColor(1)
                .condition(new FilteredSpellManaCondition(filter))
                .ruleText("Add one mana of any color. Spend this mana only to cast a Lesson or Shrine spell.")
                .build()
        );

        // {1}, {T}: Add one mana of any color.
        Ability ability = new AnyColorManaAbility(new GenericManaCost(1));
        ability.addCost(new TapSourceCost());
        this.addAbility(ability);
    }

    private WhiteLotusHideout(final WhiteLotusHideout card) {
        super(card);
    }

    @Override
    public WhiteLotusHideout copy() {
        return new WhiteLotusHideout(this);
    }
}
