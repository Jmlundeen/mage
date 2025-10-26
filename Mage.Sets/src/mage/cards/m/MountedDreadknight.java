package mage.cards.m;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.OpponentsLostLifeCondition;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.hint.common.OpponentsLostLifeHint;
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
public final class MountedDreadknight extends CardImpl {

    public MountedDreadknight(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{R}");

        this.subtype.add(SubType.VAMPIRE);
        this.subtype.add(SubType.KNIGHT);
        this.power = new MageInt(5);
        this.toughness = new MageInt(4);

        // Trample
        this.addAbility(TrampleAbility.getInstance());

        // Mounted Dreadknight enters the battlefield with a +1/+1 counter on it if an opponent lost life this turn.
        this.addAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance()),
                OpponentsLostLifeCondition.instance)
                .setText("{this} enters with a +1/+1 counter on it if an opponent lost life this turn")
        ).addHint(OpponentsLostLifeHint.instance));
    }

    private MountedDreadknight(final MountedDreadknight card) {
        super(card);
    }

    @Override
    public MountedDreadknight copy() {
        return new MountedDreadknight(this);
    }
}
