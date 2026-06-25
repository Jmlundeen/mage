package mage.cards.n;

import mage.MageInt;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.continuous.generic.GenericContinuousEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.XCostManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class Nexos extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("basic land")
            .addAll(
                    IMageObjectPredicate.getOSPPredicate(SuperType.BASIC.getPredicate()),
                    IMageObjectPredicate.getOSPPredicate(CardType.LAND.getPredicate())
            );

    public Nexos(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.TYRANID);
        this.subtype.add(SubType.ADVISOR);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Strategic Coordinator -- Basic lands you control have "{T}: Add {C}{C}. Spend this mana only on costs that contain {X}."
        Ability manaAbility = new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addStatic(Mana.ColorlessMana(2))
                .condition(new XCostManaCondition())
                .ruleText("Add {C}{C}. Spend this mana only on costs that contain {X}")
                .build();
        this.addAbility(new SimpleStaticAbility(new GenericContinuousEffect(Duration.WhileOnBattlefield, Outcome.AddAbility, filter, Zone.BATTLEFIELD)
                .withGainedAbilities(manaAbility)
                .setText("Basic lands you control have \"" + manaAbility.getRule() + "\"")
        ).withFlavorWord("Strategic Coordinator"));
    }

    private Nexos(final Nexos card) {
        super(card);
    }

    @Override
    public Nexos copy() {
        return new Nexos(this);
    }
}
