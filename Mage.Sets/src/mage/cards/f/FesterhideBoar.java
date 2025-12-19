
package mage.cards.f;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.MorbidCondition;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.hint.common.MorbidHint;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AbilityWord;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 *
 * @author North
 */
public final class FesterhideBoar extends CardImpl {

    public FesterhideBoar(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{3}{G}");
        this.subtype.add(SubType.BOAR);

        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Trample
        this.addAbility(TrampleAbility.getInstance());

        // <i>Morbid</i> &mdash; Festerhide Boar enters the battlefield with two +1/+1 counters on it if a creature died this turn.
        this.addAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance(2)),
                MorbidCondition.instance)
                .setText("{this} enters with two +1/+1 counters on it if a creature died this turn"))
                .addHint(MorbidHint.instance)
                .setAbilityWord(AbilityWord.MORBID)
        );
    }

    private FesterhideBoar(final FesterhideBoar card) {
        super(card);
    }

    @Override
    public FesterhideBoar copy() {
        return new FesterhideBoar(this);
    }
}
