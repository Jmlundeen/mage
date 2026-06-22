
package mage.cards.s;

import mage.Mana;
import mage.abilities.condition.common.PermanentsOnTheBattlefieldCondition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ComparisonType;
import mage.filter.FilterTyped;
import mage.filter.common.FilterControlledLandPermanent;
import mage.filter.predicate.typed.Spell.SpellPredicate;
import mage.filter.predicate.typed.mageObject.color.ColorlessPredicate;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class ShrineOfTheForsakenGods extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("colorless spells")
            .addAll(
                    SpellPredicate.instance,
                    ColorlessPredicate.instance
            );

    public ShrineOfTheForsakenGods(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add {C}{C}. Spend this mana only to cast colorless spells. Activate this ability only if you control seven or more lands.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.ColorlessMana(2))
                .condition(new FilteredSpellManaCondition(filter))
                .activationCondition(new PermanentsOnTheBattlefieldCondition(new FilterControlledLandPermanent("you control seven or more lands"), ComparisonType.MORE_THAN, 6))
                .ruleText("Add {C}{C}. Spend this mana only to cast colorless spells. Activate this ability only if you control seven or more lands")
                .build()
        );
    }

    private ShrineOfTheForsakenGods(final ShrineOfTheForsakenGods card) {
        super(card);
    }

    @Override
    public ShrineOfTheForsakenGods copy() {
        return new ShrineOfTheForsakenGods(this);
    }
}
