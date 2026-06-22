package mage.cards.j;

import mage.Mana;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.common.GetXValue;
import mage.abilities.effects.keyword.InvestigateEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredAbilityManaCondition;
import mage.cards.AdventureCard;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 * @author Susucr
 */
public final class JamesWanderingDad extends AdventureCard {

    public JamesWanderingDad(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.HUMAN, SubType.SCIENTIST}, "{2}{U}",
                "Follow Him",
                new CardType[]{CardType.INSTANT}, "{X}{U}{U}");

        // James, Wandering Dad
        this.getLeftHalfCard().setPT(2, 4);

        // {T}: Add {C}{C}. Spend this mana only to activate abilities.
        this.getLeftHalfCard().addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.ColorlessMana(2))
                .condition(new FilteredAbilityManaCondition(StaticTypedFilters.ACTIVATED_ABILITY))
                .ruleText("Add {C}{C}. Spend this mana only to activate abilities")
                .build()
        );

        // Follow Him
        // {X}{U}{U}
        // Instant — Adventure
        // Investigate X times.
        this.getRightHalfCard().getSpellAbility().addEffect(
                new InvestigateEffect(GetXValue.instance)
                        .setText("Investigate X times")
        );

        finalizeCard();
    }

    private JamesWanderingDad(final JamesWanderingDad card) {
        super(card);
    }

    @Override
    public JamesWanderingDad copy() {
        return new JamesWanderingDad(this);
    }
}
