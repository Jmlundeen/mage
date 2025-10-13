package mage.cards.p;

import mage.MageInt;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.SacrificeTargetCost;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.replacement.ReplaceTokenEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.common.FilterControlledPermanent;
import mage.game.permanent.token.FoodToken;

import java.util.UUID;

/**
 *
 * @author Grath
 */
public final class PeregrinTook extends CardImpl {

    private static final FilterControlledPermanent filter = new FilterControlledPermanent(SubType.FOOD, "Foods");

    public PeregrinTook(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}");
        
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HALFLING);
        this.subtype.add(SubType.CITIZEN);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // If one or more tokens would be created under your control, those tokens plus an additional Food token are created instead.
        this.addAbility(new SimpleStaticAbility(new ReplaceTokenEffect(ReplaceTokenEffect.ModificationType.ADD, 1, new FoodToken())
                .setText("If one or more tokens would be created under your control, those tokens plus an additional Food token are created instead")
        ));

        // Sacrifice three Foods: Draw a card.
        this.addAbility(new SimpleActivatedAbility(
                new DrawCardSourceControllerEffect(1),
                new SacrificeTargetCost(3, filter)
        ));
    }

    private PeregrinTook(final PeregrinTook card) {
        super(card);
    }

    @Override
    public PeregrinTook copy() {
        return new PeregrinTook(this);
    }
}
