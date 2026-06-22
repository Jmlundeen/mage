package mage.cards.t;

import mage.MageInt;
import mage.abilities.costs.common.PayLifeCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.keyword.DeathtouchAbility;
import mage.abilities.keyword.LifelinkAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.TargetController;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.Spell.SpellPredicate;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class ThievingVarmint extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("spells you don't own")
            .addAll(
                    SpellPredicate.instance,
                    TargetController.NOT_YOU.getOwnerPredicate()
            );

    public ThievingVarmint(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{B}");

        this.subtype.add(SubType.VARMINT);
        this.power = new MageInt(2);
        this.toughness = new MageInt(1);

        // Deathtouch
        this.addAbility(DeathtouchAbility.getInstance());

        // Lifelink
        this.addAbility(LifelinkAbility.getInstance());

        // {T}, Pay 1 life: Add two mana of any one color. Spend this mana only to cast spells you don't own.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .cost(new PayLifeCost(1))
                .addAnyColor(2)
                .condition(new FilteredSpellManaCondition(filter))
                .ruleText("{T}, Pay 1 life: Add two mana of any one color. Spend this mana only to cast spells you don't own.")
                .build()

        );
    }

    private ThievingVarmint(final ThievingVarmint card) {
        super(card);
    }

    @Override
    public ThievingVarmint copy() {
        return new ThievingVarmint(this);
    }
}
