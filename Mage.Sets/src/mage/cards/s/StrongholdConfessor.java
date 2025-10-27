
package mage.cards.s;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.KickedCondition;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.KickerAbility;
import mage.abilities.keyword.MenaceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 *
 * @author Styxo
 */
public final class StrongholdConfessor extends CardImpl {

    public StrongholdConfessor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{B}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.CLERIC);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // Menace
        this.addAbility(new MenaceAbility());

        // Kicker {3} (You may pay an additional {3} as you cast this spell.)
        this.addAbility(new KickerAbility("{3}"));

        // If Stronghold Confessor was kicked, it enters with two +1/+1 counters on it.
        this.addAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance(2)),
                KickedCondition.ONCE)
                .setText("if {this} was kicked, it enters with two +1/+1 counters on it")
        ));
    }

    private StrongholdConfessor(final StrongholdConfessor card) {
        super(card);
    }

    @Override
    public StrongholdConfessor copy() {
        return new StrongholdConfessor(this);
    }
}
