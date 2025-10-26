
package mage.cards.p;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.KickedCondition;
import mage.abilities.decorator.ConditionalAsThoughEffect;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.combat.CanAttackAsThoughItDidntHaveDefenderSourceEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.DefenderAbility;
import mage.abilities.keyword.KickerAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 *
 * @author LoneFox
 *
 */
public final class PrisonBarricade extends CardImpl {

    public PrisonBarricade(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}");
        this.subtype.add(SubType.WALL);
        this.power = new MageInt(1);
        this.toughness = new MageInt(3);

        // Kicker {1}{W}
        this.addAbility(new KickerAbility("{1}{W}"));

        // Defender
        this.addAbility(DefenderAbility.getInstance());

        // If Prison Barricade was kicked, it enters with a +1/+1 counter on it and with "Prison Barricade can attack as though it didn't have defender."
        Ability ability = new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance()),
                KickedCondition.ONCE)
                .setText("if {this} was kicked, it enters with a +1/+1 counter on it"));
        ability.addEffect(new ConditionalAsThoughEffect(
                new CanAttackAsThoughItDidntHaveDefenderSourceEffect(Duration.WhileOnBattlefield),
                KickedCondition.ONCE)
                .setText("and with \"This creature can attack as though it didn't have defender.\"")
        );
        this.addAbility(ability);
    }

    private PrisonBarricade(final PrisonBarricade card) {
        super(card);
    }

    @Override
    public PrisonBarricade copy() {
        return new PrisonBarricade(this);
    }
}
