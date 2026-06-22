
package mage.cards.t;

import mage.Mana;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.PermanentsOnTheBattlefieldCondition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ComparisonType;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledLandPermanent;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class TempleOfTheFalseGod extends CardImpl {

    private static final FilterPermanent filter
            = new FilterControlledLandPermanent("you control five or more lands");
    private static final Condition condition
            = new PermanentsOnTheBattlefieldCondition(filter, ComparisonType.MORE_THAN, 4);

    public TempleOfTheFalseGod(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // {T}: Add {C}{C}. Activate this ability only if you control five or more lands.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.ColorlessMana(2))
                .activationCondition(condition)
                .ruleText("add {C}{C}. Activate this ability only if you control five or more lands")
                .build()
        );
    }

    private TempleOfTheFalseGod(final TempleOfTheFalseGod card) {
        super(card);
    }

    @Override
    public TempleOfTheFalseGod copy() {
        return new TempleOfTheFalseGod(this);
    }
}
