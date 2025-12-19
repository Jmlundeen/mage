
package mage.cards.g;

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
 * @author Loki
 */
public final class GravetillerWurm extends CardImpl {

    public GravetillerWurm(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{5}{G}");
        this.subtype.add(SubType.WURM);

        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        this.addAbility(TrampleAbility.getInstance());
        // <i>Morbid</i> &mdash; Gravetiller Wurm enters the battlefield with four +1/+1 counters on it if a creature died this turn.
        this.addAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance(4)),
                MorbidCondition.instance)
                .setText("{this} enters with four +1/+1 counters on it if a creature died this turn"))
                .addHint(MorbidHint.instance).setAbilityWord(AbilityWord.MORBID));
    }

    private GravetillerWurm(final GravetillerWurm card) {
        super(card);
    }

    @Override
    public GravetillerWurm copy() {
        return new GravetillerWurm(this);
    }
}
