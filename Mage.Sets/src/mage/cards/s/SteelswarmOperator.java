package mage.cards.s;

import mage.MageInt;
import mage.Mana;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredAbilityManaCondition;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.filter.StaticTypedFilters;
import mage.filter.predicate.typed.ability.source.AbilitySourcePredicate;
import mage.filter.predicate.typed.ability.type.ActivatedAbilityPredicate;

import java.util.UUID;

/**
 * @author Susucr
 */
public final class SteelswarmOperator extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("activated ability of artifact source")
            .addAll(ActivatedAbilityPredicate.instance,
                    new AbilitySourcePredicate(StaticTypedFilters.AN_ARTIFACT)
            );

    public SteelswarmOperator(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{1}{U}");

        this.subtype.add(SubType.ROBOT);
        this.subtype.add(SubType.SOLDIER);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // {T}: Add {U}. Spend this mana only to cast an artifact spell.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                        .cost(new TapSourceCost())
                        .addStatic(Mana.BlueMana(1))
                        .condition(new FilteredSpellManaCondition(StaticTypedFilters.AN_ARTIFACT_SPELL))
                        .ruleText("Add {U}. Spend this mana only to cast an artifact spell")
                        .build()
        );

        // {T}: Add {U}{U}. Spend this mana only to activate abilities of artifact sources.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                        .cost(new TapSourceCost())
                        .addStatic(Mana.BlueMana(2))
                        .condition(new FilteredAbilityManaCondition(filter))
                        .ruleText("Add {U}{U}. Spend this mana only to activate abilities of artifact sources")
                        .build()
        );
    }

    private SteelswarmOperator(final SteelswarmOperator card) {
        super(card);
    }

    @Override
    public SteelswarmOperator copy() {
        return new SteelswarmOperator(this);
    }
}
