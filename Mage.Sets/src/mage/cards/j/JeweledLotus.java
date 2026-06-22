package mage.cards.j;

import mage.abilities.costs.common.SacrificeSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.TargetController;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.mageObject.object.CommanderPredicate;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class JeweledLotus extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("your commander")
            .addAll(CommanderPredicate.instance,
                    TargetController.YOU.getOwnerPredicate()
            );

    public JeweledLotus(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{0}");

        // {T}, Sacrifice Jeweled Lotus: Add three mana of any one color. Spend this mana only to cast your commander.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .cost(new SacrificeSourceCost())
                .addAnyColor(3)
                .condition(new FilteredSpellManaCondition(filter))
                .ruleText("Add three mana of any one color. Spend this mana only to cast your commander")
                .build()
        );
    }

    private JeweledLotus(final JeweledLotus card) {
        super(card);
    }

    @Override
    public JeweledLotus copy() {
        return new JeweledLotus(this);
    }
}
