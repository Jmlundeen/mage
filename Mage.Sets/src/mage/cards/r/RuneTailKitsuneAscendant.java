package mage.cards.r;

import mage.abilities.StateTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.FlipSourceEffect;
import mage.abilities.effects.common.PreventAllDamageToAllEffect;
import mage.cards.CardSetInfo;
import mage.cards.FlipCard;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.players.Player;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class RuneTailKitsuneAscendant extends FlipCard {

    public RuneTailKitsuneAscendant(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.FOX, SubType.MONK}, "{2}{W}",
                "Rune-Tail's Essence",
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.ENCHANTMENT}, new SubType[]{});

        // Rune-Tail, Kitsune Ascendant
        this.getLeftHalfCard().setPT(2, 2);

        // When you have 30 or more life, flip Rune-Tail, Kitsune Ascendant.
        this.getLeftHalfCard().addAbility(new RuneTailKitsuneAscendantFlipAbility());

        // Rune-Tail's Essence
        // Prevent all damage that would be dealt to creatures you control.
        this.getRightHalfCard().addAbility(new SimpleStaticAbility(
                new PreventAllDamageToAllEffect(Duration.WhileOnBattlefield, StaticFilters.FILTER_CONTROLLED_CREATURES)));
    }

    private RuneTailKitsuneAscendant(final RuneTailKitsuneAscendant card) {
        super(card);
    }

    @Override
    public RuneTailKitsuneAscendant copy() {
        return new RuneTailKitsuneAscendant(this);
    }
}

class RuneTailKitsuneAscendantFlipAbility extends StateTriggeredAbility {

    public RuneTailKitsuneAscendantFlipAbility() {
        super(Zone.BATTLEFIELD, new FlipSourceEffect());
    }

    private RuneTailKitsuneAscendantFlipAbility(final RuneTailKitsuneAscendantFlipAbility ability) {
        super(ability);
    }

    @Override
    public RuneTailKitsuneAscendantFlipAbility copy() {
        return new RuneTailKitsuneAscendantFlipAbility(this);
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        Player controller = game.getPlayer(this.getControllerId());
        if (controller != null) {
            return controller.getLife() >= 30;
        }
        return false;
    }

    @Override
    public String getRule() {
        return "When you have 30 or more life, flip {this}.";
    }

}
