package mage.cards.g;

import mage.Mana;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.PermanentsOnTheBattlefieldCondition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.hint.ConditionHint;
import mage.abilities.hint.Hint;
import mage.abilities.mana.BlueManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterPermanent;
import mage.filter.predicate.Predicates;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class GloomlakeVerge extends CardImpl {

    private static final FilterPermanent filter = new FilterPermanent("you control an Island or a Swamp");

    static {
        filter.add(Predicates.or(
                SubType.ISLAND.getPredicate(),
                SubType.SWAMP.getPredicate()
        ));
    }

    private static final Condition condition = new PermanentsOnTheBattlefieldCondition(filter);
    private static final Hint hint = new ConditionHint(condition, "You control an Island or a Swamp");

    public GloomlakeVerge(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // {T}: Add {U}.
        this.addAbility(new BlueManaAbility());

        // {T}: Add {B}. Activate only if you control an Island or a Swamp.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.BlackMana(1))
                .activationCondition(condition)
                .ruleText("add {B}. Activate only if you control an Island or a Swamp")
                .build()
                .addHint(hint)
        );
    }

    private GloomlakeVerge(final GloomlakeVerge card) {
        super(card);
    }

    @Override
    public GloomlakeVerge copy() {
        return new GloomlakeVerge(this);
    }
}
