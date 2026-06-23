package mage.cards.n;

import mage.MageInt;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.keyword.DoctorsCompanionAbility;
import mage.abilities.keyword.UndyingAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ManaType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 * @author Cguy7777
 */
public final class NardoleResourcefulCyborg extends CardImpl {

    public NardoleResourcefulCyborg(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{1}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SCIENTIST);
        this.power = new MageInt(1);
        this.toughness = new MageInt(2);

        // {T}: Add {U} for each counter on Nardole. Spend this mana only to cast noncreature spells.
        this.addAbility(new ComposedManaAbilityBuilder()
                .addDynamic(CountersSourceCount.ANY, ManaType.BLUE)
                .condition(new FilteredSpellManaCondition(StaticTypedFilters.A_NON_CREATURE_SPELL))
                .cost(new TapSourceCost())
                .ruleText("Add {U} for each counter on {this}. Spend this mana only to cast noncreature spells.")
                .build()
        );

        // Undying
        this.addAbility(new UndyingAbility());

        // Doctor's companion
        this.addAbility(DoctorsCompanionAbility.getInstance());
    }

    private NardoleResourcefulCyborg(final NardoleResourcefulCyborg card) {
        super(card);
    }

    @Override
    public NardoleResourcefulCyborg copy() {
        return new NardoleResourcefulCyborg(this);
    }
}
