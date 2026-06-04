
package mage.cards.p;

import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.costs.common.RevealTargetFromHandCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.TapSourceUnlessPaysEffect;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.SpendOrActivateManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterCard;
import mage.filter.FilterTyped;
import mage.target.common.TargetCardInHand;

import java.util.UUID;

/**
 *
 * @author TGower
 */
public final class PrimalBeyond extends CardImpl {

    private static final FilterCard filter = new FilterCard("an Elemental card from your hand");
    static final FilterTyped filterElementalObject = new FilterTyped("elemental")
            .add(SubType.ELEMENTAL.getPredicate());

    static {
        filter.add(SubType.ELEMENTAL.getPredicate());
    }

    public PrimalBeyond(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.LAND},"");

        // As Primal Beyond enters the battlefield, you may reveal an Elemental card from your hand. If you don't, Primal Beyond enters the battlefield tapped.
        this.addAbility(new AsEntersBattlefieldAbility(new TapSourceUnlessPaysEffect(new RevealTargetFromHandCost(new TargetCardInHand(filter))), "you may reveal an Elemental card from your hand. If you don't, {this} enters tapped"));

        // {tap}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {tap}: Add one mana of any color. Spend this mana only to cast an Elemental spell or activate an ability of an Elemental.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addAnyColor(1)
                .condition(new SpendOrActivateManaCondition(filterElementalObject))
                .ruleText("Add one mana of any color. Spend this mana only to cast an Elemental spell or activate an ability of an Elemental")
                .build()
        );
    }

    private PrimalBeyond(final PrimalBeyond card) {
        super(card);
    }

    @Override
    public PrimalBeyond copy() {
        return new PrimalBeyond(this);
    }
}
