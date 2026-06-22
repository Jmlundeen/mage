package mage.cards.s;

import mage.MageInt;
import mage.Mana;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredAbilityManaCondition;
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
 * @author hanasu
 */
public final class SoldeviMachinist extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("activated ability of an artifact")
            .addAll(new AbilitySourcePredicate(StaticTypedFilters.AN_ARTIFACT),
                    ActivatedAbilityPredicate.instance);

    public SoldeviMachinist(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}");
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.WIZARD);
        this.subtype.add(SubType.ARTIFICER);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // {T}: Add {C}{C}. Spend this mana only to activate abilities of artifacts.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.ColorlessMana(2))
                .condition(new FilteredAbilityManaCondition(filter))
                .ruleText("Add {C}{C}. Spend this mana only to activate abilities")
                .build()
        );
    }

    private SoldeviMachinist(final SoldeviMachinist card) {
        super(card);
    }

    @Override
    public SoldeviMachinist copy() {
        return new SoldeviMachinist(this);
    }
}
