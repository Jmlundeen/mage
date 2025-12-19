package mage.cards.s;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.KickedCondition;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.KickerAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class ShalaisAcolyte extends CardImpl {

    public ShalaisAcolyte(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{W}");

        this.subtype.add(SubType.ANGEL);
        this.power = new MageInt(3);
        this.toughness = new MageInt(4);

        // Kicker {1}{G}
        this.addAbility(new KickerAbility("{1}{G}"));

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // If Shalai's Acolyte was kicked, it enters with two +1/+1 counters on it.
        this.addAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance(2)),
                KickedCondition.ONCE)
                .setText("if {this} was kicked, it enters the battlefield with two +1/+1 counters on it")
        ));
    }

    private ShalaisAcolyte(final ShalaisAcolyte card) {
        super(card);
    }

    @Override
    public ShalaisAcolyte copy() {
        return new ShalaisAcolyte(this);
    }
}
