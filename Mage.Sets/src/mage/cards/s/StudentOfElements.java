package mage.cards.s;

import mage.abilities.StateTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.FlipSourceEffect;
import mage.abilities.effects.common.continuous.GainAbilityControlledEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardSetInfo;
import mage.cards.FlipCard;
import mage.constants.*;
import mage.filter.common.FilterCreaturePermanent;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class StudentOfElements extends FlipCard {

    public StudentOfElements(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new CardType[]{CardType.CREATURE}, new SubType[]{SubType.HUMAN, SubType.WIZARD}, "{1}{U}",
                "Tobita, Master of Winds",
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.HUMAN, SubType.WIZARD});

        // Student of Elements
        this.getLeftHalfCard().setPT(1, 1);

        // When Student of Elements has flying, flip it.
        this.getLeftHalfCard().addAbility(new StudentOfElementsHasFlyingAbility());

        // Tobita, Master of Winds
        this.getRightHalfCard().setPT(3, 3);

        // Creatures you control have flying.
        this.getRightHalfCard().addAbility(new SimpleStaticAbility(
                new GainAbilityControlledEffect(FlyingAbility.getInstance(), Duration.WhileOnBattlefield, new FilterCreaturePermanent())));
    }

    private StudentOfElements(final StudentOfElements card) {
        super(card);
    }

    @Override
    public StudentOfElements copy() {
        return new StudentOfElements(this);
    }
}

class StudentOfElementsHasFlyingAbility extends StateTriggeredAbility {

    public StudentOfElementsHasFlyingAbility() {
        super(Zone.BATTLEFIELD, new FlipSourceEffect());
    }

    private StudentOfElementsHasFlyingAbility(final StudentOfElementsHasFlyingAbility ability) {
        super(ability);
    }

    @Override
    public StudentOfElementsHasFlyingAbility copy() {
        return new StudentOfElementsHasFlyingAbility(this);
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        Permanent permanent = game.getPermanent(getSourceId());
        if (permanent != null && permanent.getAbilities().contains(FlyingAbility.getInstance())) {
            return true;
        }
        return false;
    }

    @Override
    public String getRule() {
        return "When {this} has flying, flip it.";
    }

}
