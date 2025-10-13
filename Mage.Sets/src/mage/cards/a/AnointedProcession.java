
package mage.cards.a;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.replacement.ReplaceTokenEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;

import java.util.UUID;

/**
 *
 * @author fireshoes
 */
public final class AnointedProcession extends CardImpl {

    public AnointedProcession(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{W}");

        // If an effect would create one or more tokens under your control, it creates twice that many of those tokens instead.
        this.addAbility(new SimpleStaticAbility(new ReplaceTokenEffect(ReplaceTokenEffect.ModificationType.MULTIPLY, 2)
        .setText("if an effect would create one or more tokens under your control, " +
                        "it creates twice that many of those tokens instead")
        ));
    }

    private AnointedProcession(final AnointedProcession card) {
        super(card);
    }

    @Override
    public AnointedProcession copy() {
        return new AnointedProcession(this);
    }
}
