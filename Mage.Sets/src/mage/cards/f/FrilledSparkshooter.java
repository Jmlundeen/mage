package mage.cards.f;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.OpponentsLostLifeCondition;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.hint.common.OpponentsLostLifeHint;
import mage.abilities.keyword.MenaceAbility;
import mage.abilities.keyword.ReachAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class FrilledSparkshooter extends CardImpl {

    public FrilledSparkshooter(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R}");

        this.subtype.add(SubType.LIZARD);
        this.subtype.add(SubType.ARCHER);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Menace
        this.addAbility(new MenaceAbility(false));

        // Reach
        this.addAbility(ReachAbility.getInstance());

        // Frilled Sparkshooter enters with a +1/+1 counter on it if an opponent lost life this turn.
        this.addAbility(new SimpleStaticAbility(
                new ConditionalReplacementEffect(
                        new EntersWithCountersEffect(CounterType.P1P1.createInstance()),
                        OpponentsLostLifeCondition.instance)
                        .setText("{this} enters with a +1/+1 counter on it if an opponent lost life this turn")
        ).addHint(OpponentsLostLifeHint.instance));
    }

    private FrilledSparkshooter(final FrilledSparkshooter card) {
        super(card);
    }

    @Override
    public FrilledSparkshooter copy() {
        return new FrilledSparkshooter(this);
    }
}
