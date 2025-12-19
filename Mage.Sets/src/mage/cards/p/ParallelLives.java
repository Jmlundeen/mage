
package mage.cards.p;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.replacement.ReplaceTokenEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;

import java.util.UUID;

/**
 *
 * @author BetaSteward
 */
public final class ParallelLives extends CardImpl {

    public ParallelLives(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{G}");

        // If an effect would create one or more tokens under your control, it creates twice that many of those tokens instead.
        this.addAbility(new SimpleStaticAbility(new ReplaceTokenEffect(ReplaceTokenEffect.ModificationType.MULTIPLY, 2)
                .setText("If an effect would create one or more tokens under your control, it creates twice that many of those tokens instead")
        ));
    }

    private ParallelLives(final ParallelLives card) {
        super(card);
    }

    @Override
    public ParallelLives copy() {
        return new ParallelLives(this);
    }
}
