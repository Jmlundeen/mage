package mage.cards.b;

import mage.abilities.common.DealtDamageAndDiedTriggeredAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.FlipSourceEffect;
import mage.abilities.keyword.BushidoAbility;
import mage.abilities.keyword.DoubleStrikeAbility;
import mage.cards.CardSetInfo;
import mage.cards.FlipCard;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;

import java.util.UUID;

/**
 * @author LevelX
 */
public final class BushiTenderfoot extends FlipCard {

    public BushiTenderfoot(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new CardType[]{CardType.CREATURE}, new SubType[]{SubType.HUMAN, SubType.SOLDIER}, "{W}",
                "Kenzo the Hardhearted",
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.HUMAN, SubType.SAMURAI});

        // Bushi Tenderfoot
        this.getLeftHalfCard().setPT(1, 1);

        // When a creature dealt damage by Bushi Tenderfoot this turn dies, flip Bushi Tenderfoot.
        Effect effect = new FlipSourceEffect();
        effect.setText("flip {this}");
        this.getLeftHalfCard().addAbility(new DealtDamageAndDiedTriggeredAbility(effect));

        // Kenzo the Hardhearted
        this.getRightHalfCard().setPT(3, 4);

        // Double strike; bushido 2 (When this blocks or becomes blocked, it gets +2/+2 until end of turn.)
        this.getRightHalfCard().addAbility(DoubleStrikeAbility.getInstance());
        this.getRightHalfCard().addAbility(new BushidoAbility(2));
    }

    private BushiTenderfoot(final BushiTenderfoot card) {
        super(card);
    }

    @Override
    public BushiTenderfoot copy() {
        return new BushiTenderfoot(this);
    }
}
