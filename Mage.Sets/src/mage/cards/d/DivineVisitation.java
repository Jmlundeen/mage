package mage.cards.d;

import mage.MageObject;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.replacement.ReplaceTokenEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.game.permanent.token.AngelVigilanceToken;

import java.util.UUID;

/**
 *
 * @author TheElk801
 */
public final class DivineVisitation extends CardImpl {

    public DivineVisitation(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{W}{W}");

        // If one or more creature tokens would be created under your control, 
        // that many 4/4 white Angel creature tokens with flying and 
        // vigilance are created instead.
        this.addAbility(new SimpleStaticAbility(new ReplaceTokenEffect(ReplaceTokenEffect.ModificationType.REPLACE,
                0, new AngelVigilanceToken())
                .withTokenCondition(MageObject::isCreature)
                .setText("If one or more creature tokens would be created under your control, " +
                        "that many 4/4 white Angel creature tokens with flying and vigilance are created instead.")
        ));
    }

    private DivineVisitation(final DivineVisitation card) {
        super(card);
    }

    @Override
    public DivineVisitation copy() {
        return new DivineVisitation(this);
    }
}
