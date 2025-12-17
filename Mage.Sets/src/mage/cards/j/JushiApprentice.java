package mage.cards.j;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.condition.common.CardsInHandCondition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.decorator.ConditionalOneShotEffect;
import mage.abilities.dynamicvalue.common.CardsInControllerHandCount;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.DrawCardTargetEffect;
import mage.abilities.effects.common.FlipSourceEffect;
import mage.cards.CardSetInfo;
import mage.cards.FlipCard;
import mage.constants.CardType;
import mage.constants.ComparisonType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.target.TargetPlayer;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class JushiApprentice extends FlipCard {

    public JushiApprentice(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new CardType[]{CardType.CREATURE}, new SubType[]{SubType.HUMAN, SubType.WIZARD}, "{1}{U}",
                "Tomoya the Revealer",
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.HUMAN, SubType.WIZARD});

        // Jushi Apprentice
        this.getLeftHalfCard().setPT(1, 2);

        // {2}{U}, {tap}: Draw a card. If you have nine or more cards in hand, flip Jushi Apprentice.
        Ability ability = new SimpleActivatedAbility(new DrawCardSourceControllerEffect(1), new ManaCostsImpl<>("{2}{U}"));
        ability.addCost(new TapSourceCost());
        ability.addEffect(new ConditionalOneShotEffect(new FlipSourceEffect(), new CardsInHandCondition(ComparisonType.MORE_THAN, 8),
                "If you have nine or more cards in hand, flip {this}"));
        this.getLeftHalfCard().addAbility(ability);

        // Tomoya the Revealer
        this.getRightHalfCard().setPT(2, 3);

        // {3}{U}{U},{T} : Target player draws X cards, where X is the number of cards in your hand.
        Ability ability2 = new SimpleActivatedAbility(new DrawCardTargetEffect(CardsInControllerHandCount.ANY), new ManaCostsImpl<>("{3}{U}{U}"));
        ability2.addCost(new TapSourceCost());
        ability2.addTarget(new TargetPlayer());
        this.getRightHalfCard().addAbility(ability2);
    }

    private JushiApprentice(final JushiApprentice card) {
        super(card);
    }

    @Override
    public JushiApprentice copy() {
        return new JushiApprentice(this);
    }
}
