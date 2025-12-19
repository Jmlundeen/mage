
package mage.cards.o;

import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.ChooseCreatureTypeEffect;
import mage.abilities.effects.common.continuous.BoostAllOfChosenSubtypeEffect;
import mage.abilities.keyword.ConvokeAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;

import java.util.UUID;

/**
 *
 * @author emerald000
 */
public final class ObeliskOfUrd extends CardImpl {

    public ObeliskOfUrd(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{6}");

        // Convoke
        this.addAbility(new ConvokeAbility());

        // As Obelisk of Urd enters the battlefield, choose a creature type.
        this.addAbility(new AsEntersBattlefieldAbility(new ChooseCreatureTypeEffect(Outcome.BoostCreature)));

        // Creatures you control of the chosen type get +2/+2.
        this.addAbility(new SimpleStaticAbility(new BoostAllOfChosenSubtypeEffect(2, 2, Duration.WhileOnBattlefield, false)));
    }

    private ObeliskOfUrd(final ObeliskOfUrd card) {
        super(card);
    }

    @Override
    public ObeliskOfUrd copy() {
        return new ObeliskOfUrd(this);
    }
}
