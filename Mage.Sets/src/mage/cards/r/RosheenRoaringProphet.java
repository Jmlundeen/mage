package mage.cards.r;

import mage.MageInt;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.MultipliedValue;
import mage.abilities.dynamicvalue.common.RevealedCardsAmount;
import mage.abilities.effects.common.MillThenPutInHandEffect;
import mage.abilities.effects.common.reveal.RevealEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.SimpleManaAbility;
import mage.abilities.mana.conditional.XCostManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.predicate.mageobject.VariableManaCostPredicate;

import java.util.UUID;

/**
 * @author Susucr
 */
public final class RosheenRoaringProphet extends CardImpl {

    private static final FilterCard filter = new FilterCard("a card with {X} in its mana cost");
    private static final DynamicValue revealedCardsValue = new MultipliedValue(new RevealedCardsAmount(filter), 2);

    static {
        filter.add(VariableManaCostPredicate.instance);
    }

    public RosheenRoaringProphet(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{R}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.GIANT);
        this.subtype.add(SubType.SHAMAN);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // When Rosheen, Roaring Prophet enters the battlefield, mill six cards. You may put a card with {X} in its mana cost from among them into your hand.
        this.addAbility(new EntersBattlefieldTriggeredAbility(
                new MillThenPutInHandEffect(6, filter), false
        ));

        // {T}: Reveal any number of cards with {X} in their mana cost in your hand. Add {C}{C} for each card revealed this way. Spend this mana only on costs that contain {X}.
        SimpleManaAbility ability = new SimpleManaAbility(
                new RevealEffect(Outcome.Benefit)
                        .setFilter(filter)
                        .setMinCardsToReveal(0)
                        .rememberRevealed()
                        .setText("reveal any number of cards with {X} in their mana cost in your hand"),
                new TapSourceCost()
        );
        ability.addEffect(new ComposedManaAbilityBuilder()
                .addDynamic(revealedCardsValue, ManaType.COLORLESS)
                .condition(new XCostManaCondition())
                .ruleText("Add {C}{C} for each card revealed this way. Spend this mana only on costs that contain {X}")
                .buildEffect()
        );
        ability.setUndoPossible(false);
        this.addAbility(ability);
    }

    private RosheenRoaringProphet(final RosheenRoaringProphet card) {
        super(card);
    }

    @Override
    public RosheenRoaringProphet copy() {
        return new RosheenRoaringProphet(this);
    }
}
