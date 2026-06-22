
package mage.cards.p;

import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.ChooseCreatureTypeEffect;
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
 * @author TheElk801
 */
public final class PillarOfOrigins extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("spell ability")
            .add(SpellAbilityPredicate.instance);

    public PillarOfOrigins(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{2}");

        // As Pillar of Origins enters the battlefield, choose a creature type.
        this.addAbility(new AsEntersBattlefieldAbility(new ChooseCreatureTypeEffect(Outcome.Benefit)));

        // {T}: Add one mana of any color. Spend this mana only to cast a creature spell if the chosen type.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyColor(1)
                .runtimeCondition(new ChosenCreatureTypeConditionProvider(filter))
                .ruleText("Add one mana of any color. Spend this mana only to cast a creature spell if the chosen type")
                .build()
        );
    }

    private PillarOfOrigins(final PillarOfOrigins card) {
        super(card);
    }

    @Override
    public PillarOfOrigins copy() {
        return new PillarOfOrigins(this);
    }
}
