package mage.cards.s;

import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.ChooseCreatureTypeEffect;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.providers.ChosenCreatureTypeConditionProvider;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.LogicalPredicate;
import mage.filter.predicate.typed.ability.type.ActivatedAbilityPredicate;
import mage.filter.predicate.typed.ability.type.SpellAbilityPredicate;

import java.util.UUID;

/**
 * @author jeffwadsworth
 */
public final class SecludedCourtyard extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("spell or activated ability")
            .add(LogicalPredicate.or(
                    SpellAbilityPredicate.instance,
                    ActivatedAbilityPredicate.instance
            ));

    public SecludedCourtyard(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // As Secluded Courtyard enters the battlefield, choose a creature type.
        this.addAbility(new AsEntersBattlefieldAbility(new ChooseCreatureTypeEffect(Outcome.Benefit)));

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add one mana of any color. Spend this mana only to cast a creature spell of the chosen type or activate an ability of a creature or creature card of the chosen type.
        this.addAbility(new ComposedManaAbilityBuilder()
                .addAnyColor(1)
                .runtimeCondition(new ChosenCreatureTypeConditionProvider(filter))
                .cost(new TapSourceCost())
                .ruleText("Add one mana of any color. Spend this mana only to cast a creature spell of the chosen type or activate an ability of a creature or creature card of the chosen type")
                .build()
        );
    }

    private SecludedCourtyard(final SecludedCourtyard card) {
        super(card);
    }

    @Override
    public SecludedCourtyard copy() {
        return new SecludedCourtyard(this);
    }
}
