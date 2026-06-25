
package mage.cards.s;

import mage.MageInt;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.SpendOrActivateManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;

import java.util.UUID;

/**
 *
 * @author North
 */
public final class Smokebraider extends CardImpl {

    static final FilterTyped filter = new FilterTyped("elemental")
            .add(IMageObjectPredicate.getOSPPredicate(SubType.ELEMENTAL.getPredicate()));

    public Smokebraider(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{1}{R}");
        this.subtype.add(SubType.ELEMENTAL);
        this.subtype.add(SubType.SHAMAN);

        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // {T}: Add two mana in any combination of colors. Spend this mana only to cast Elemental spells or activate abilities of Elementals.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addAnyCombination(2)
                .condition(new SpendOrActivateManaCondition(filter))
                .ruleText("Add two mana in any combination of colors. Spend this mana only to cast Elemental spells or activate abilities of Elementals")
                .build()
        );
    }

    private Smokebraider(final Smokebraider card) {
        super(card);
    }

    @Override
    public Smokebraider copy() {
        return new Smokebraider(this);
    }
}
