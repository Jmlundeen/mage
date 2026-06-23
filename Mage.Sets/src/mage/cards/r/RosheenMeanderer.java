package mage.cards.r;

import mage.MageInt;
import mage.Mana;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.XCostManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;

import java.util.UUID;

/**
 * @author jeffwadsworth
 */
public final class RosheenMeanderer extends CardImpl {

    public RosheenMeanderer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R/G}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.GIANT);
        this.subtype.add(SubType.SHAMAN);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // {T}: Add {C}{C}{C}{C}. Spend this mana only on costs that contain {X}.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addStatic(Mana.ColorlessMana(4))
                .condition(new XCostManaCondition())
                .ruleText("Add {C}{C}{C}{C}. Spend this mana only on costs that contain {X}")
                .build()
        );
    }

    private RosheenMeanderer(final RosheenMeanderer card) {
        super(card);
    }

    @Override
    public RosheenMeanderer copy() {
        return new RosheenMeanderer(this);
    }
}
