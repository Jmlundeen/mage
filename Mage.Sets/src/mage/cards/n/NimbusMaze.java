
package mage.cards.n;

import mage.Mana;
import mage.abilities.condition.common.PermanentsOnTheBattlefieldCondition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.common.FilterControlledPermanent;

import java.util.UUID;

/**
 * @author dustinconrad
 */
public final class NimbusMaze extends CardImpl {

    private static final FilterControlledPermanent controlIsland = new FilterControlledPermanent("you control an Island");
    private static final FilterControlledPermanent controlPlains = new FilterControlledPermanent("you control a Plains");

    static {
        controlIsland.add(SubType.ISLAND.getPredicate());
        controlPlains.add(SubType.PLAINS.getPredicate());
    }

    public NimbusMaze(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add {W}. Activate this ability only if you control an Island.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.WhiteMana(1))
                .activationCondition(new PermanentsOnTheBattlefieldCondition(controlIsland))
                .ruleText("add {W}. Activate this ability only if you control an Island.")
                .build()
        );

        // {T}: Add {U}. Activate this ability only if you control a Plains.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.BlueMana(1))
                .activationCondition(new PermanentsOnTheBattlefieldCondition(controlPlains))
                .ruleText("add {U}. Activate this ability only if you control a Plains.")
                .build()
        );
    }

    private NimbusMaze(final NimbusMaze card) {
        super(card);
    }

    @Override
    public NimbusMaze copy() {
        return new NimbusMaze(this);
    }
}

