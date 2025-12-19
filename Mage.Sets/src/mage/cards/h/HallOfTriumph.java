
package mage.cards.h;

import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.ChooseColorEffect;
import mage.abilities.effects.common.continuous.BoostAllOfChosenColorEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class HallOfTriumph extends CardImpl {

    public HallOfTriumph(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ARTIFACT},"{3}");
        this.supertype.add(SuperType.LEGENDARY);

        // As Hall of Triumph enters the battlefield choose a color.
        this.addAbility(new AsEntersBattlefieldAbility(new ChooseColorEffect(Outcome.Neutral)));
        // Creatures you control of the chosen color get +1/+1.
        this.addAbility(new SimpleStaticAbility(new BoostAllOfChosenColorEffect(1, 1, Duration.WhileOnBattlefield,false)));
    }

    private HallOfTriumph(final HallOfTriumph card) {
        super(card);
    }

    @Override
    public HallOfTriumph copy() {
        return new HallOfTriumph(this);
    }
}
