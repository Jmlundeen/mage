package mage.cards.s;

import mage.MageInt;
import mage.Mana;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.SpendOrActivateManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.LogicalPredicate;
import mage.filter.predicate.typed.Spell.SpellPredicate;
import mage.filter.predicate.typed.ability.type.ActivatedAbilityPredicate;
import mage.filter.predicate.typed.mageObject.color.ColorlessPredicate;

import java.util.UUID;

/**
 * @author Susucr
 */
public final class SageOfTheUnknowable extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("colorless spell or activated ability")
            .addAny(
                    LogicalPredicate.and(
                            ColorlessPredicate.instance,
                            SpellPredicate.instance
                    ),
                    ActivatedAbilityPredicate.instance
            );

    public SageOfTheUnknowable(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.WIZARD);
        this.power = new MageInt(0);
        this.toughness = new MageInt(4);

        // {T}: Add {C}. Spend this mana only to cast a colorless spell or to activate an ability.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.ColorlessMana(1))
                .condition(new SpendOrActivateManaCondition(filter))
                .ruleText("Add {C}. Spend this mana only to cast a colorless spell or to activate an ability")
                .build()
        );
    }

    private SageOfTheUnknowable(final SageOfTheUnknowable card) {
        super(card);
    }

    @Override
    public SageOfTheUnknowable copy() {
        return new SageOfTheUnknowable(this);
    }
}
