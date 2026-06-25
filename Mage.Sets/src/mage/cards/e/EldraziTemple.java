
package mage.cards.e;

import mage.Mana;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.SpendOrActivateManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.filter.predicate.typed.mageObject.color.ColorlessPredicate;

import java.util.UUID;

/**
 *
 * @author Loki, nantuko
 */
public final class EldraziTemple extends CardImpl {

    static final FilterTyped filter = new FilterTyped("colorless eldrazi")
            .addAll(
                    IMageObjectPredicate.getOSPPredicate(SubType.ELDRAZI.getPredicate()),
                    ColorlessPredicate.instance
            );

    public EldraziTemple(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.LAND},null);

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add {C}{C}. Spend this mana only to cast colorless Eldrazi spells or activate abilities of colorless Eldrazi.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addStatic(Mana.ColorlessMana(2))
                .condition(new SpendOrActivateManaCondition(filter))
                .ruleText("Add {C}{C}. Spend this mana only to cast colorless Eldrazi spells or activate abilities of colorless Eldrazi")
                .build()
        );
    }

    private EldraziTemple(final EldraziTemple card) {
        super(card);
    }

    @Override
    public EldraziTemple copy() {
        return new EldraziTemple(this);
    }
}
