
package mage.cards.u;

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
import mage.filter.predicate.typed.ability.type.SpellAbilityPredicate;

import java.util.UUID;

/**
 *
 * @author spjspj
 */
public final class UnclaimedTerritory extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("spell ability")
            .add(SpellAbilityPredicate.instance);

    public UnclaimedTerritory(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // As Unclaimed Territory enters the battlefield, choose a creature type.
        this.addAbility(new AsEntersBattlefieldAbility(new ChooseCreatureTypeEffect(Outcome.Benefit)));

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add one mana of any color. Spend this mana only to cast a creature spell of the chosen type.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyColor(1)
                .runtimeCondition(new ChosenCreatureTypeConditionProvider(filter))
                .ruleText("Add one mana of any color. Spend this mana only to cast a creature spell of the chosen type")
                .build());
    }

    private UnclaimedTerritory(final UnclaimedTerritory card) {
        super(card);
    }

    @Override
    public UnclaimedTerritory copy() {
        return new UnclaimedTerritory(this);
    }
}
