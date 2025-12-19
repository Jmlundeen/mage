package mage.cards.g;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.CreaturesDiedThisTurnCount;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.hint.common.CreaturesDiedThisTurnHint;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class GrizzlyGhoul extends CardImpl {

    public GrizzlyGhoul(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}{G}");

        this.subtype.add(SubType.ZOMBIE);
        this.subtype.add(SubType.BEAR);
        this.power = new MageInt(4);
        this.toughness = new MageInt(3);

        // Trample
        this.addAbility(TrampleAbility.getInstance());

        // Grizzly Ghoul enters the battlefield with a +1/+1 counter on it for each creature that died this turn.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.P1P1, CreaturesDiedThisTurnCount.instance))
                .addHint(CreaturesDiedThisTurnHint.instance)
        );
    }

    private GrizzlyGhoul(final GrizzlyGhoul card) {
        super(card);
    }

    @Override
    public GrizzlyGhoul copy() {
        return new GrizzlyGhoul(this);
    }
}
