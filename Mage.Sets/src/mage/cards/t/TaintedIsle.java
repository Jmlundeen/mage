
package mage.cards.t;

import mage.abilities.condition.common.PermanentsOnTheBattlefieldCondition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ManaType;
import mage.constants.SubType;
import mage.filter.common.FilterLandPermanent;

import java.util.Set;
import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class TaintedIsle extends CardImpl {

    private static final FilterLandPermanent filter = new FilterLandPermanent("you control a Swamp");
    static {
        filter.add(SubType.SWAMP.getPredicate());
    }

    public TaintedIsle(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.LAND},"");

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add {U} or {B}. Activate this ability only if you control a Swamp.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addChoice(Set.of(ManaType.BLUE, ManaType.BLACK), 1)
                .activationCondition(new PermanentsOnTheBattlefieldCondition(filter))
                .ruleText("add {U} or {B}. Activate this ability only if you control a Swamp")
                .build()
        );

    }

    private TaintedIsle(final TaintedIsle card) {
        super(card);
    }

    @Override
    public TaintedIsle copy() {
        return new TaintedIsle(this);
    }
}
