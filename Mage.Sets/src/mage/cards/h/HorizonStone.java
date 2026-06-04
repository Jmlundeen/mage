package mage.cards.h;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.mana.ReplaceManaEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.ManaType;
import mage.constants.Outcome;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class HorizonStone extends CardImpl {

    public HorizonStone(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{5}");

        // If you would lose unspent mana, that mana becomes colorless instead.
        this.addAbility(new SimpleStaticAbility(
                ReplaceManaEffect.unspent(Duration.WhileOnBattlefield, Outcome.Benefit, ReplaceManaEffect.changeUnspentManaToType(ManaType.COLORLESS))
                        .setText("if you would lose unspent mana, that mana becomes colorless instead")
        ));
    }

    private HorizonStone(final HorizonStone card) {
        super(card);
    }

    @Override
    public HorizonStone copy() {
        return new HorizonStone(this);
    }
}
